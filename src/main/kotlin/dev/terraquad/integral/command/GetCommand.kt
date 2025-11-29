package dev.terraquad.integral.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.SuggestionProvider
import dev.terraquad.integral.Entries
import dev.terraquad.integral.Integral
import dev.terraquad.integral.PlayerManager
import dev.terraquad.integral.networking.GetListS2CPayload
import dev.terraquad.integral.networking.ListReason
import dev.terraquad.integral.networking.ListType
import dev.terraquad.integral.send
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.command.CommandSource
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import java.util.*

object GetCommand : Command<ServerCommandSource>, Subcommand<ServerCommandSource> {
    data class RequestorInfo(val source: ServerCommandSource, val summary: Boolean, val overlaps: Boolean)

    private val listTypeSuggestionProvider = SuggestionProvider<ServerCommandSource> { _, builder ->
        CommandSource.suggestMatching(ListType.entries.map { it.toString() }, builder)
    }
    private val listRequestors = hashMapOf<UUID, RequestorInfo>()

    override fun run(context: CommandContext<ServerCommandSource>): Int {
        val player = EntityArgumentType.getPlayer(context, "player")
        val typeString = StringArgumentType.getString(context, "type")
        val type = runCatching { ListType.valueOf(typeString) }.onFailure {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create()
        }.getOrThrow()
        val summary = runCatching { BoolArgumentType.getBool(context, "summary") }.getOrDefault(false)
        val overlaps = runCatching { BoolArgumentType.getBool(context, "overlaps") }.getOrDefault(false)

        if (!PlayerManager.isPlayerEnabled(player.uuid)) {
            throw IntegralCommand.missingMod.create(player.name)
        }

        val sender = ServerPlayNetworking.getSender(player)
        GetListS2CPayload(type, ListReason.GET_COMMAND).send(sender)
        listRequestors[player.uuid] = RequestorInfo(context.source!!, summary, overlaps)

        return 1
    }

    override fun getBuilder(): LiteralArgumentBuilder<ServerCommandSource> = CommandManager.literal("get").then(
        CommandManager.argument("player", EntityArgumentType.player()).then(
            CommandManager.argument("type", StringArgumentType.word()).suggests(listTypeSuggestionProvider)
                .executes(GetCommand).then(
                    CommandManager.argument("summary", BoolArgumentType.bool()).executes(GetCommand)
                        .then(CommandManager.argument("overlaps", BoolArgumentType.bool()).executes(GetCommand))
                )
        )
    )

    fun onListArrival(player: ServerPlayerEntity, type: ListType, list: Entries) {
        if (player.uuid !in listRequestors) {
            Integral.logger.warn("${player.name.string} sent a list in response to a get request that nobody sent...")
            return
        }
        val info = listRequestors[player.uuid]!!

        val message = if (info.summary) {
            Integral.writeListSummary(player.name.string, type, list)
        } else {
            Integral.writeListAnswer(player.name.string, type, list, includeOverlaps = info.overlaps)
        }
        info.source.sendFeedback({ Text.literal(message) }, false)
        listRequestors.remove(player.uuid)
    }
}