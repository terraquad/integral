package dev.terraquad.integral.command

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import dev.terraquad.integral.textTranslatable
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.server.command.CommandManager

object IntegralCommand {
    val unsupportedDataType =
        SimpleCommandExceptionType(textTranslatable("integral.command.error.unsupported_type"))
    val missingMod = DynamicCommandExceptionType { textTranslatable("integral.command.error.missing_mod", it) }

    private val subcommands = listOf(
        SetModpackCommand, ReloadCommand, GetCommand, ConfigCommand
    )

    fun register() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            var builder = CommandManager.literal("integral").requires { source ->
                source.hasPermissionLevel(2)
            }
            for (cmd in subcommands) {
                builder = builder.then(cmd.getBuilder())
            }
            dispatcher.register(builder)
        }
    }
}