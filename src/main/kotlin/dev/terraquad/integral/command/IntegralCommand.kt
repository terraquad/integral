package dev.terraquad.integral.command

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import dev.terraquad.integral.componentTranslatable
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.commands.Commands

object IntegralCommand {
    val unsupportedDataType =
        SimpleCommandExceptionType(componentTranslatable("integral.command.error.unsupported_type"))
    val missingMod = DynamicCommandExceptionType { componentTranslatable("integral.command.error.missing_mod", it) }

    fun register() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            val builder = Commands.literal("integral").requires { source ->
                source.hasPermission(2)
            }.then(SetModpackCommand.getBuilder())
                .then(ReloadCommand.getBuilder())
                .then(GetCommand.getBuilder())
                .then(ConfigCommand.getBuilder())
            dispatcher.register(builder)
        }
    }
}