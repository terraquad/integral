package dev.terraquad.integral.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.terraquad.integral.*
import dev.terraquad.integral.config.Config
import dev.terraquad.integral.networking.GetListS2CPayload
import dev.terraquad.integral.networking.ListReason
import dev.terraquad.integral.networking.ListType
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.server.level.ServerPlayer
import java.util.*

object SetModpackCommand : Command<CommandSourceStack>, Subcommand<CommandSourceStack> {
    data class ModpackSendStatus(var mods: Boolean = false, var resourcePacks: Boolean = false)

    private val playerModpackStatuses = hashMapOf<UUID, ModpackSendStatus>()

    override fun run(context: CommandContext<CommandSourceStack>): Int {
        if (!PlayerManager.isPlayerEnabled(context.source.playerOrException.uuid)) {
            throw IntegralCommand.missingMod.create(context.source.playerOrException.name.string)
        }

        playerModpackStatuses[context.source.playerOrException.uuid] = ModpackSendStatus()
        val sender = ServerPlayNetworking.getSender(context.source.playerOrException)
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

    override fun getBuilder(): LiteralArgumentBuilder<CommandSourceStack> =
        Commands.literal("set_modpack").executes(SetModpackCommand)

    fun onListArrival(player: ServerPlayer, type: ListType, list: Entries) {
        if (player.uuid !in playerModpackStatuses) {
            Integral.logger.warn(
                "${player.name.string} tried to change the server modpack arbitrarily, probably by hacking."
            )
            return
        }

        when (type) {
            ListType.MODS -> {
                Config.modpack = Config.modpack.copy(mods = list)
                playerModpackStatuses[player.uuid]!!.mods = true
            }

            ListType.RESOURCE_PACKS -> {
                Config.modpack = Config.modpack.copy(resourcePacks = list)
                playerModpackStatuses[player.uuid]!!.resourcePacks = true
            }
        }

        if (playerModpackStatuses[player.uuid]!!.run { mods && resourcePacks }) {
            playerModpackStatuses.remove(player.uuid)
            player.createCommandSourceStack().sendSuccess(
                { componentTranslatable("integral.command.set_modpack") }, true
            )
        }
    }
}