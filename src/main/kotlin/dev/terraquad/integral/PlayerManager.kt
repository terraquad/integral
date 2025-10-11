package dev.terraquad.integral

import dev.terraquad.integral.config.Config
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.server.MinecraftServer
import org.geysermc.geyser.api.GeyserApi
import java.util.*

object PlayerManager {
    private const val MOD_CHECK_TIMEOUT = 2 * 20
    private val modCheckJoinTicks = hashMapOf<UUID, Int>()
    val enabledPlayers = hashSetOf<UUID>()

    fun checkModPresence(server: MinecraftServer) {
        modCheckJoinTicks.onEach { (uuid, tick) ->
            if (server.ticks - tick > MOD_CHECK_TIMEOUT) {
                val player = server.playerManager.getPlayer(uuid)!!
                val isGeyser = runCatching { GeyserApi.api().isBedrockPlayer(uuid) }.getOrDefault(false)
                if (isGeyser && Config.prefs.reportGeyserPlayers) {
                    Integral.logList("${player.name.string} is connected through Geyser, list requests won't be sent")
                } else if (!isGeyser) {
                    Integral.logList("${player.name.string} doesn't have the mod installed client-side, list requests won't be sent")
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