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

    fun register() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            val builder = CommandManager.literal("integral").requires { source ->
                source.hasPermissionLevel(2)
            }.then(SetModpackCommand.getBuilder())
                .then(ReloadCommand.getBuilder())
                .then(GetCommand.getBuilder())
                .then(ConfigCommand.getBuilder())
            dispatcher.register(builder)
        }
    }
}