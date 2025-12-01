package dev.terraquad.integral.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.SuggestionProvider
import dev.terraquad.integral.config.Config
import dev.terraquad.integral.config.ConfigPrefs
import dev.terraquad.integral.textTranslatable
import net.minecraft.command.CommandSource
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import kotlin.reflect.full.createType
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.memberProperties

object ConfigCommand : Subcommand<ServerCommandSource> {
    val getKey = Command<ServerCommandSource> { context ->
        val keyString = StringArgumentType.getString(context, "key")
        val key = ConfigPrefs::class.memberProperties.find { it.name == keyString }
        if (key == null) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create()
        }

        val value = key.get(Config.prefs)!!
        context.source.sendFeedback(
            {
                textTranslatable("integral.command.config.get", keyString, value.toString())
            }, false
        )

        1
    }
    val setKey = Command<ServerCommandSource> { context ->
        // From https://stackoverflow.com/questions/49511098/call-data-class-copy-via-reflection
        val keyString = StringArgumentType.getString(context, "key")
        val value = BoolArgumentType.getBool(context, "value")

        val copyFunc = ConfigPrefs::copy
        val key = copyFunc.parameters.find { it.name == keyString }
        if (key == null) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create()
        }
        if (key.type != Boolean::class.createType()) {
            throw IntegralCommand.unsupportedDataType.create()
        }

        Config.prefs = copyFunc.callBy(mapOf(copyFunc.instanceParameter!! to Config.prefs, key to value))
        context.source.sendFeedback(
            {
                textTranslatable("integral.command.config.set", keyString, value.toString())
            }, true
        )

        1
    }

    private val keySuggestionProvider = SuggestionProvider<ServerCommandSource> { _, builder ->
        CommandSource.suggestMatching(
            ConfigPrefs::copy
                .parameters
                .filter { it.type == Boolean::class.createType() }
                .map { it.name }, builder
        )
    }

    override fun getBuilder(): LiteralArgumentBuilder<ServerCommandSource> = CommandManager.literal("config").then(
        CommandManager.argument("key", StringArgumentType.word())
            .suggests(keySuggestionProvider)
            .executes(getKey)
            .then(
                CommandManager.argument("value", BoolArgumentType.bool())
                    .executes(setKey)
            )
    )

}