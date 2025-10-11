package dev.terraquad.integral.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.terraquad.integral.config.Config
import dev.terraquad.integral.textTranslatable
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource

object ReloadCommand : Command<ServerCommandSource>, Subcommand<ServerCommandSource> {

    override fun run(context: CommandContext<ServerCommandSource>): Int {
        Config.loadPrefs()
        Config.loadModpack()
        context.source.sendFeedback({ textTranslatable("integral.command.reload") }, true)
        return 1
    }

    override fun getBuilder(): LiteralArgumentBuilder<ServerCommandSource> =
        CommandManager.literal("reload").executes(ReloadCommand)
}