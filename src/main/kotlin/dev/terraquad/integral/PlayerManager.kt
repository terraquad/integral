package dev.terraquad.integral

import dev.terraquad.integral.config.Config
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.text.Text
import org.geysermc.geyser.api.GeyserApi
import java.util.*

object PlayerManager {
    // 2 seconds before assuming missing mod
    private const val MOD_CHECK_TIMEOUT = 2 * 20

    // Contains the times when new players joined in ticks
    private val modCheckJoinTicks = hashMapOf<UUID, Int>()

    // All players that have the mod
    val enabledPlayers = hashSetOf<UUID>()

    private fun reportCircumstance(server: MinecraftServer, message: Text) {
        if (Config.prefs.summarizeToOperators) {
            Integral.broadcastToOps(server, message)
        }
        Integral.broadcastToConsole(message.string)
    }

    fun checkModPresence(server: MinecraftServer) {
        modCheckJoinTicks.onEach { (uuid, tick) ->
            if (server.ticks - tick > MOD_CHECK_TIMEOUT) {
                val player = server.playerManager.getPlayer(uuid)!!
                val isGeyser = runCatching { GeyserApi.api().isBedrockPlayer(uuid) }.getOrDefault(false)
                if (isGeyser && Config.prefs.reportGeyserPlayers) {
                    reportCircumstance(
                        server,
                        textTranslatable("integral.player_manager.has_geyser", player.name.string),
                    )
                } else if (!isGeyser) {
                    reportCircumstance(
                        server,
                        textTranslatable("integral.player_manager.missing_mod", player.name.string),
                    )
                }
                disablePlayer(uuid)
            }
        }
    }

    fun enablePlayer(uuid: UUID) {
        modCheckJoinTicks.remove(uuid)
        enabledPlayers.add(uuid)
    }

    fun disablePlayer(uuid: UUID) {
        modCheckJoinTicks.remove(uuid)
        enabledPlayers.remove(uuid)
    }

    fun isPlayerEnabled(uuid: UUID) = enabledPlayers.contains(uuid)

    fun register() {
        ServerPlayConnectionEvents.JOIN.register { handler, _, server ->
            modCheckJoinTicks[handler.player.uuid] = server.ticks
        }

        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            // Clean up
            disablePlayer(handler.player.uuid)
        }

        ServerTickEvents.END_SERVER_TICK.register { server ->
            checkModPresence(server)
        }
    }
}