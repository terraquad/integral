package dev.terraquad.integral

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import dev.terraquad.integral.config.Config
import dev.terraquad.integral.networking.GetListS2CPayload
import dev.terraquad.integral.networking.ListReason
import dev.terraquad.integral.networking.ListType
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.command.CommandSource
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import java.util.*

object IntegralCommand {
    data class ModpackSendStatus(var mods: Boolean = false, var resourcePacks: Boolean = false)

    val invalidListTypeError = DynamicCommandExceptionType { LiteralMessage("Invalid list type: $it") }

    private val playerModpackStatuses = hashMapOf<UUID, ModpackSendStatus>()
    private val listRequestors = hashMapOf<UUID, ServerCommandSource>()

    fun register() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
                CommandManager.literal("integral").requires { source -> source.hasPermissionLevel(4) }
                    .then(
                        CommandManager.literal("set_modpack").executes(IntegralCommand::setModpack)
                    ).then(
                        CommandManager.literal("reload").executes(IntegralCommand::reloadConfig)
                    ).then(
                        CommandManager.literal("get").then(
                            CommandManager.argument("player", EntityArgumentType.player()).then(
                                CommandManager.argument("type", StringArgumentType.word()).suggests { _, builder ->
                                    CommandSource.suggestMatching(
                                        ListType.entries.map { it.toString() }, builder
                                    )
                                }.executes(IntegralCommand::getList)
                            )
                        )
                    )
            )
        }
    }

    fun setModpack(context: CommandContext<ServerCommandSource>): Int {
        if (!PlayerManager.isPlayerEnabled(context.source.playerOrThrow.uuid)) {
            context.source.sendError(textTranslatable("integral.command.set_modpack.missing_mod"))
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
            player.commandSource.sendError(textTranslatable("integral.command.set_modpack.arbitrary_send"))
            Integral.logger.warn("${player.name.string} tried to change the server modpack arbitrarily, probably by hacking.")
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
            player.commandSource.sendFeedback(
                { Text.literal("Successfully changed server modpack!") }, true
            )
        }
    }

    fun reloadConfig(context: CommandContext<ServerCommandSource>): Int {
        Config.loadPrefs()
        Config.loadModpack()
        context.source.sendFeedback({ textTranslatable("integral.command.reload.success") }, true)
        return 1
    }

    fun getList(
        context: CommandContext<ServerCommandSource>
    ): Int {
        val player = EntityArgumentType.getPlayer(context, "player")
        val typeString = StringArgumentType.getString(context, "type")
        val type =
            runCatching { ListType.valueOf(typeString) }.onFailure { throw invalidListTypeError.create(typeString) }
                .getOrThrow()

        if (!PlayerManager.isPlayerEnabled(player.uuid)) {
            context.source.sendError(
                textTranslatable("integral.command.get.missing_mod", player.name.string)
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
            Integral.logger.warn("${player.name.string} sent a list in response to a get request that nobody sent...")
            return
        }

        listRequestors[player.uuid]!!.sendFeedback({
            Text.literal(Integral.writeListAnswer(player.name.string, type, list, true))
        }, false)
        listRequestors.remove(player.uuid)
    }
}