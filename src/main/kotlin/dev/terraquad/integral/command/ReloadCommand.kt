package dev.terraquad.integral.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.terraquad.integral.componentTranslatable
import dev.terraquad.integral.config.Config
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

object ReloadCommand : Command<CommandSourceStack>, Subcommand<CommandSourceStack> {

    override fun run(context: CommandContext<CommandSourceStack>): Int {
        Config.loadPrefs()
        Config.loadModpack()
        context.source.sendSuccess({
            componentTranslatable("integral.command.reload")
        }, true)
        return 1
    }

    override fun getBuilder(): LiteralArgumentBuilder<CommandSourceStack> =
        Commands.literal("reload").executes(ReloadCommand)
}