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
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import org.slf4j.LoggerFactory

class Integral : ModInitializer {
    companion object {
        const val MOD_ID = "integral"
        val logger = LoggerFactory.getLogger(Integral::class.java)!!

        private var discordLogChannel: GuildMessageChannel? = null

        fun broadcastToConsole(message: String) {
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

        fun broadcastToOps(server: MinecraftServer, message: Component) {
            server.playerList.players.filter { it.hasPermissions(2) }.forEach {
                it.sendSystemMessage(
                    Component.literal("[Integral] ").append(message).withStyle(ChatFormatting.GRAY)
                )
            }
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
                        ListWriter.writeSummary(
                            context.player().name.string,
                            payload.type,
                            payload.entries,
                            reason = payload.reason,
                        )
                    }
                    val report by lazy {
                        ListWriter.writeReport(
                            context.player().name.string,
                            payload.type,
                            payload.entries,
                            reason = payload.reason,
                            includeOverlaps = Config.prefs.includeOverlaps,
                        )
                    }

                    if (Config.prefs.summarizeToOperators) {
                        broadcastToOps(context.server(), summary)
                    }
                    if (Config.prefs.summarizeEverywhere) {
                        broadcastToConsole(summary.string)
                    } else {
                        broadcastToConsole(report.string)
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
