@file:OptIn(ExperimentalTime::class)

package dev.terraquad.integral

import de.erdbeerbaerlp.dcintegration.common.DiscordIntegration
import de.erdbeerbaerlp.dcintegration.common.storage.Configuration
import dev.terraquad.integral.networking.*
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import org.geysermc.geyser.api.GeyserApi
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.time.ExperimentalTime

class Integral : ModInitializer {
    companion object {
        const val MOD_ID = "integral"
        val logger = LoggerFactory.getLogger(MOD_ID)!!

        const val INSTALL_CHECK_TIMEOUT = 2 * 20
        var installCheckJoinTicks = mutableMapOf<UUID, Int>()
    }

    override fun onInitialize() {
        PayloadTypeRegistry.playS2C().register(GetListS2CPayload.id, GetListS2CPayload.codec)
        PayloadTypeRegistry.playC2S().register(SendListC2SPayload.id, SendListC2SPayload.codec)
        PayloadTypeRegistry.playC2S().register(ClientEventC2SPayload.id, ClientEventC2SPayload.codec)

        ServerPlayNetworking.registerGlobalReceiver(SendListC2SPayload.id) { payload, context ->
            logger.debug("Received {} list from {}", payload.type, context.player().name.string)

            val headline = "${context.player().name.string} sent ${payload.type} list:"
            val list = StringBuilder().let {
                payload.entries.forEachIndexed { i, entry ->
                    it.append("- ")
                    entry.onEachIndexed { j, (prop, value) ->
                        it.append("$prop = $value")
                        if (j < entry.count() - 1) it.append(" | ")
                    }
                    if (i < payload.entries.count() - 1) it.appendLine()
                }
                it.toString()
            }

            logger.info(headline)
            for (line in list.lineSequence()) logger.info(line)

            DiscordIntegration.INSTANCE?.let {
                val message = "### Integral - $headline:\n$list"
                val logChannelID = Configuration.instance().commandLog.channelID
                if (logChannelID == "0") return@let
                it.sendMessage(message, it.getChannel(logChannelID))
            }
        }

        ServerPlayNetworking.registerGlobalReceiver(ClientEventC2SPayload.id) { payload, context ->
            logger.debug("Received client event {} from {}", payload.event, context.player().name.string)
            if (payload.event == ClientEvent.READY) {
                installCheckJoinTicks.remove(context.player().uuid)
                GetListS2CPayload(
                    ListType.MODS,
                    listOf(EntryProperty.NAME, EntryProperty.VERSION),
                ).send(context.responseSender())
            }
            GetListS2CPayload(
                ListType.RESOURCE_PACKS,
                listOf(EntryProperty.NAME),
            ).send(context.responseSender())
        }

        ServerPlayConnectionEvents.JOIN.register { handler, _, server ->
            var installCheckCandidate = true
            try {
                if (GeyserApi.api().isBedrockPlayer(handler.player.uuid)) {
                    logger.info(
                        "{} is connected through Geyser, list requests won't be sent",
                        handler.player.name.string
                    )
                    installCheckCandidate = false
                }
            } catch (_: RuntimeException) {
            }
            if (installCheckCandidate) {
                installCheckJoinTicks[handler.player.uuid] = server.ticks
            }
        }

        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            // Remove remaining references
            installCheckJoinTicks.remove(handler.player.uuid)
        }

        ServerTickEvents.END_SERVER_TICK.register { server ->
            installCheckJoinTicks.onEach { (uuid, tick) ->
                if (server.ticks - tick > INSTALL_CHECK_TIMEOUT) {
                    val player = server.playerManager.getPlayer(uuid)!!
                    logger.info(
                        "{} doesn't have Integral installed client-side, list requests won't be sent",
                        player.name.string
                    )
                    installCheckJoinTicks.remove(uuid)
                }
            }
        }
    }
}
