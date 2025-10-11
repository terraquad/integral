package dev.terraquad.integral

import de.erdbeerbaerlp.dcintegration.common.DiscordIntegration
import de.erdbeerbaerlp.dcintegration.common.storage.Configuration
import de.maxhenkel.admiral.MinecraftAdmiral
import dev.terraquad.integral.command.IntegralCommand
import dev.terraquad.integral.command.ListTypeArgumentSupplier
import dev.terraquad.integral.command.ListTypeArgumentTypeSupplier
import dev.terraquad.integral.config.Config
import dev.terraquad.integral.networking.*
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import org.slf4j.LoggerFactory

class Integral : ModInitializer {
    companion object {
        const val MOD_ID = "integral"
        val logger = LoggerFactory.getLogger(Integral::class.java)!!

        fun logList(message: String) {
            for (line in message.lineSequence()) {
                logger.info(line)
            }
            if (Config.prefs.sendListsToDiscord) {
                DiscordIntegration.INSTANCE?.let {
                    val logChannelID = Configuration.instance().commandLog.channelID
                    if (logChannelID == "0") return@let
                    it.sendMessage("### Integral\n> $message", it.getChannel(logChannelID))
                    it.channel
                }
            }
        }

        fun writeListAnswer(playerName: String, type: ListType, playerList: Entries): String = StringBuilder().let {
            it.append("$playerName sent ${type.friendlyString()}")
            val serverList = when (type) {
                ListType.MODS -> Config.modpack.mods
                ListType.RESOURCE_PACKS -> Config.modpack.resourcePacks
            }
            if (Config.prefs.compareLists && serverList != null) {
                it.appendLine(", changes to server modpack: ")
                // Log added entries (only client has entry)
                playerList.filter { entry -> entry.key !in serverList }.forEach { (id, version) ->
                    it.append("+ $id")
                    if (type != ListType.RESOURCE_PACKS) it.appendLine(" (client: $version)")
                    else it.appendLine()
                }
                // Log removed entries (only server has entry)
                serverList.filter { entry -> entry.key !in playerList }.forEach { (id, version) ->
                    it.append("- $id")
                    if (type != ListType.RESOURCE_PACKS) it.appendLine(" (server: $version)")
                    else it.appendLine()
                }
                // Log overlapping entries (both client and server have entry)
                if (Config.prefs.includeOverlaps) playerList.keys.intersect(serverList.keys).forEach { id ->
                    it.append("~ $id")
                    if (type != ListType.RESOURCE_PACKS) {
                        val clientVersion = playerList[id]
                        val serverVersion = serverList[id]
                        if (clientVersion != serverVersion) {
                            it.appendLine(" (client: $clientVersion, server: $serverVersion)")
                        } else {
                            it.appendLine(" ($serverVersion)")
                        }
                    } else {
                        it.appendLine()
                    }
                }
            } else {
                it.appendLine(":")
                playerList.forEach { (id, version) ->
                    it.appendLine("~ $id ($version)")
                }
            }
            it.toString()
        }
    }

    override fun onInitialize() {
        PayloadTypeRegistry.playS2C().register(GetListS2CPayload.id, GetListS2CPayload.codec)
        PayloadTypeRegistry.playC2S().register(SendListC2SPayload.id, SendListC2SPayload.codec)
        PayloadTypeRegistry.playC2S().register(ClientEventC2SPayload.id, ClientEventC2SPayload.codec)

        if (Config.prefs.reportPlayersWithoutMod) {
            PlayerManager.register()
        }

        ServerPlayNetworking.registerGlobalReceiver(SendListC2SPayload.id) { payload, context ->
            logger.debug(
                "Received {} list from {} because of {}",
                payload.type,
                context.player().name.string,
                payload.reason
            )

            when (payload.reason) {
                ListReason.SET_MODPACK -> IntegralCommand.processModpack(
                    context.player(),
                    payload.type,
                    payload.entries,
                )

                ListReason.GET_LIST -> IntegralCommand.processList(context.player(), payload.type, payload.entries)

                else -> {
                    val message = writeListAnswer(context.player().name.string, payload.type, payload.entries)
                    if (message.lines().count() > 2) {
                        logList(message)
                    } else if (Config.prefs.compareLists && Config.prefs.reportConformingPlayers) {
                        logList("${context.player().name.string} uses the same ${payload.type.friendlyString()} as the server modpack")
                    } else if (!Config.prefs.compareLists) {
                        logList(
                            "${context.player().name.string} sent an empty ${
                                payload.type.friendlyString().substringBeforeLast('s')
                            } list"
                        )
                    }
                }
            }
        }

        ServerPlayNetworking.registerGlobalReceiver(ClientEventC2SPayload.id) { payload, context ->
            logger.debug("Received client event {} from {}", payload.event, context.player().name.string)
            when (payload.event) {
                ClientEvent.READY -> {
                    PlayerManager.enablePlayer(context.player().uuid)
                    if (Config.prefs.requestModsOnJoin) {
                        GetListS2CPayload(
                            ListType.MODS,
                            ListReason.JOIN,
                        ).send(context.responseSender())
                    }
                    if (Config.prefs.requestResourcePacksOnJoin) {
                        GetListS2CPayload(
                            ListType.RESOURCE_PACKS,
                            ListReason.JOIN,
                        ).send(context.responseSender())
                    }
                }

                ClientEvent.RELOAD -> {
                    if (Config.prefs.requestResourcePacksOnReload) {
                        GetListS2CPayload(
                            ListType.RESOURCE_PACKS,
                            ListReason.RELOAD,
                        ).send(context.responseSender())
                    }
                }
            }
        }

        CommandRegistrationCallback.EVENT.register { dispatcher, registryAccess, _ ->
            MinecraftAdmiral
                .builder(dispatcher, registryAccess)
                .addCommandClasses(IntegralCommand::class.java)
                .addArgumentTypes { argumentTypeRegistry ->
                    argumentTypeRegistry.register(
                        ListType::class.java,
                        ListTypeArgumentSupplier(),
                        ListTypeArgumentTypeSupplier(),
                    )
                }
                .build()
        }
    }
}
