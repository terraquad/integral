package dev.terraquad.integral.command

import com.mojang.brigadier.context.CommandContext
import de.maxhenkel.admiral.annotations.Command
import de.maxhenkel.admiral.annotations.Name
import de.maxhenkel.admiral.annotations.RequiresPermissionLevel
import dev.terraquad.integral.Entries
import dev.terraquad.integral.Integral
import dev.terraquad.integral.PlayerManager
import dev.terraquad.integral.config.Config
import dev.terraquad.integral.config.Modpack
import dev.terraquad.integral.networking.GetListS2CPayload
import dev.terraquad.integral.networking.ListReason
import dev.terraquad.integral.networking.ListType
import dev.terraquad.integral.send
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import java.util.*

@Command("integral")
object IntegralCommand {
    data class ModpackSendStatus(var mods: Boolean = false, var resourcePacks: Boolean = false)

    private val playerModpackStatuses = hashMapOf<UUID, ModpackSendStatus>()

    // Mapping from requestees to requestors
    private val listRequestors = hashMapOf<UUID, ServerCommandSource>()

    @Command("set_modpack")
    @RequiresPermissionLevel(4)
    fun setModpack(context: CommandContext<ServerCommandSource>): Int {
        if (!PlayerManager.isPlayerEnabled(context.source.playerOrThrow.uuid)) {
            context.source.sendError(Text.literal("You need to have Integral installed client-side to change the modpack"))
            return 0
        }

        playerModpackStatuses[context.source.playerOrThrow.uuid] = ModpackSendStatus()
        val sender = ServerPlayNetworking.getSender(context.source.playerOrThrow)
        GetListS2CPayload(
            ListType.MODS,
            ListReason.SET_MODPACK,
        ).send(sender)
        GetListS2CPayload(
            ListType.RESOURCE_PACKS,
            ListReason.SET_MODPACK,
        ).send(sender)

        return 1
    }

    fun processModpack(player: ServerPlayerEntity, type: ListType, list: Entries) {
        if (player.uuid !in playerModpackStatuses) {
            player.commandSource.sendError(Text.literal("You are not supposed to set modpacks! Oh well..."))
            Integral.logger.warn("${player.name.string} tried to change the server modpack without permission!")
            return
        }

        when (type) {
            ListType.MODS -> {
                Config.modpack = Modpack(list, Config.modpack.resourcePacks)
                playerModpackStatuses[player.uuid]!!.mods = true
            }

            ListType.RESOURCE_PACKS -> {
                Config.modpack = Modpack(Config.modpack.mods, list)
                playerModpackStatuses[player.uuid]!!.resourcePacks = true
            }
        }

        if (playerModpackStatuses[player.uuid]!!.run { mods && resourcePacks }) {
            playerModpackStatuses.remove(player.uuid)
            player.commandSource.sendFeedback(
                { Text.literal("Successfully changed server modpack!") }, true
            )
        }
    }

    @Command("reload")
    @RequiresPermissionLevel(4)
    fun reloadConfig(context: CommandContext<ServerCommandSource>): Int {
        Config.loadPrefs()
        Config.loadModpack()
        context.source.sendFeedback({ Text.literal("Successfully reloaded server-side configuration!") }, true)
        return 1
    }

    @Command("get")
    @RequiresPermissionLevel(4)
    fun getList(
        context: CommandContext<ServerCommandSource>,
        @Name("type") type: ListType,
        @Name("player") player: ServerPlayerEntity
    ): Int {
        if (!PlayerManager.isPlayerEnabled(player.uuid)) {
            context.source.sendError(
                Text.literal("${player.name.string} doesn't have Integral installed client-side, so they can't answer to list requests")
            )
            return 0
        }

        val sender = ServerPlayNetworking.getSender(player)
        GetListS2CPayload(type, ListReason.GET_LIST).send(sender)
        listRequestors[player.uuid] = context.source

        return 1
    }

    fun processList(player: ServerPlayerEntity, type: ListType, list: Entries) {
        if (player.uuid !in listRequestors) {
            Integral.logger.warn("${player.name.string} sent a list but no one asked for it")
            return
        }

        listRequestors[player.uuid]!!.sendFeedback({
            Text.literal(Integral.writeListAnswer(player.name.string, type, list))
        }, false)
        listRequestors.remove(player.uuid)
    }
}