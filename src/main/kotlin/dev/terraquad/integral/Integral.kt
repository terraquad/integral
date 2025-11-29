package dev.terraquad.integral

import de.erdbeerbaerlp.dcintegration.common.DiscordIntegration
import de.erdbeerbaerlp.dcintegration.common.storage.Configuration
import dev.terraquad.integral.command.GetCommand
import dev.terraquad.integral.command.IntegralCommand
import dev.terraquad.integral.command.SetModpackCommand
import dev.terraquad.integral.config.Config
import dev.terraquad.integral.networking.*
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.MinecraftServer
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.slf4j.LoggerFactory

class Integral : ModInitializer {
    companion object {
        const val MOD_ID = "integral"
        val logger = LoggerFactory.getLogger(Integral::class.java)!!

        private var discordLogChannel: GuildMessageChannel? = null

        fun logList(message: String) {
            logger.info(message)
            if (Config.prefs.sendListsToDiscord) {
                DiscordIntegration.INSTANCE?.let {
                    if (discordLogChannel == null) {
                        val logChannelID = Configuration.instance().commandLog.channelID
                        if (logChannelID == "0") return@let
                        discordLogChannel = it.getChannel(logChannelID)
                        logger.info("Using Discord channel {} for Integral lists", logChannelID)
                    }
                    it.sendMessage("### Integral\n> $message", discordLogChannel)
                }
            }
        }

        fun broadcastToOps(server: MinecraftServer, message: Text) {
            server.playerManager.playerList.filter { it.hasPermissionLevel(2) }.forEach {
                it.sendMessage(
                    Text.literal("[Integral] ").formatted(Formatting.GRAY).formatted(Formatting.ITALIC).append(message)
                )
            }
        }

        fun overlayLists(clientList: Entries, serverList: Entries): Triple<Entries, Entries, Entries> {
            val containBoth = Entries()
            val containClient = Entries()
            val containServer = Entries()

            clientList.keys.intersect(serverList.keys).forEach {
                val clientVer = clientList[it]!!
                val serverVer = serverList[it]!!
                if (clientVer != serverVer) {
                    containBoth[it] = "$clientVer;$serverVer"
                    return@forEach
                }
                containBoth[it] = serverVer
            }
            containClient.putAll(clientList.filter { it.key !in serverList })
            containServer.putAll(serverList.filter { it.key !in clientList })

            return Triple(containBoth, containClient, containServer)
        }

        fun writeListAnswer(
            playerName: String,
            type: ListType,
            clientList: Entries,
            reason: ListReason? = null,
            includeOverlaps: Boolean = false
        ): String = StringBuilder("$playerName sent ${type.friendlyString()}").let {
            if (reason != null) {
                it.append(" at ${reason.friendlyString()}")
            }
            val serverList = when (type) {
                ListType.MODS -> Config.modpack.mods
                ListType.RESOURCE_PACKS -> Config.modpack.resourcePacks
            }
            if (Config.prefs.compareLists && serverList != null) {
                it.appendLine(", changes to server modpack: ")
                val overlay = overlayLists(clientList, serverList)
                // Log added entries
                for ((id, ver) in overlay.second) {
                    it.appendLine("| + $id (client: $ver)")
                }
                // Log removed entries
                for ((id, ver) in overlay.third) {
                    it.appendLine("| - $id (server: $ver")
                }
                // Log overlapping entries
                if (includeOverlaps) for ((id, ver) in overlay.first) {
                    if (ver.contains(";")) {
                        val (clientVer, serverVer) = ver.split(";")
                        it.appendLine("| ~ $id (client: $clientVer, server: $serverVer)")
                    } else {
                        it.appendLine("| ~ $id ($ver)")
                    }
                }
            } else {
                it.appendLine(":")
                clientList.forEach { (id, ver) ->
                    it.appendLine("| ~ $id ($ver)")
                }
            }
            it.toString()
        }

        fun writeListSummary(
            playerName: String, type: ListType, clientList: Entries, reason: ListReason? = null
        ): String = StringBuilder("$playerName sent ${type.friendlyString()}").let {
            if (reason != null) {
                it.append(" at ${reason.friendlyString()}")
            }
            it.append(": ")
            val serverList = when (type) {
                ListType.MODS -> Config.modpack.mods
                ListType.RESOURCE_PACKS -> Config.modpack.resourcePacks
            }
            if (Config.prefs.compareLists && serverList != null) {
                val overlay = overlayLists(clientList, serverList)
                it.append(
                    "${overlay.first.count()} kept, ${overlay.second.count()} added, ${overlay.third.count()} removed"
                )
            } else {
                it.append("${clientList.count()} total")
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
                "Received {} list from {} because of {}", payload.type, context.player().name.string, payload.reason
            )

            when (payload.reason) {
                ListReason.SET_MODPACK -> SetModpackCommand.onListArrival(
                    context.player(),
                    payload.type,
                    payload.entries,
                )

                ListReason.GET_COMMAND -> GetCommand.onListArrival(
                    context.player(), payload.type, payload.entries
                )

                else -> {
                    val summary by lazy {
                        writeListSummary(
                            context.player().name.string, payload.type, payload.entries, reason = payload.reason
                        )
                    }
                    val message by lazy {
                        writeListAnswer(
                            context.player().name.string, payload.type, payload.entries, reason = payload.reason,
                            includeOverlaps = Config.prefs.includeOverlaps
                        )
                    }

                    if (Config.prefs.summarizeToOperators) {
                        broadcastToOps(
                            context.server(),
                            Text.literal(summary)
                        )
                    }
                    if (Config.prefs.summarizeEverywhere) {
                        logList(summary)
                    } else {
                        if (message.lines().count() < 2) {
                            logList(
                                "${context.player().name.string} sent an empty ${
                                    payload.type.friendlyString().substringBeforeLast('s')
                                } list"
                            )
                        } else if (Config.prefs.compareLists && Config.prefs.reportConformingPlayers) {
                            logList(
                                "${context.player().name.string} has the same ${payload.type.friendlyString()} as the server modpack"
                            )
                        } else {
                            logList(message)
                        }
                    }
                }
            }
        }

        ServerPlayNetworking.registerGlobalReceiver(ClientEventC2SPayload.id) { payload, context ->
            logger.debug(
                "Received client event {} from {}", payload.event, context.player().name.string
            )
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

        IntegralCommand.register()
    }
}
