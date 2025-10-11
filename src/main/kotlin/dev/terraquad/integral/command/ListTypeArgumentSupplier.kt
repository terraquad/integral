package dev.terraquad.integral.command

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.suggestion.SuggestionProvider
import de.maxhenkel.admiral.argumenttype.ArgumentTypeSupplier
import dev.terraquad.integral.Integral
import dev.terraquad.integral.networking.ListType
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.CommandSource
import net.minecraft.command.suggestion.SuggestionProviders
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.util.Identifier

class ListTypeArgumentSupplier : ArgumentTypeSupplier<ServerCommandSource, CommandRegistryAccess, String> {
    companion object {
        val provider = SuggestionProviders.register<ServerCommandSource>(
            Identifier.of(
                Integral.MOD_ID,
                "list_type"
            )
        ) { _, builder ->
            CommandSource.suggestMatching(ListType.entries.map { it.toString() }, builder)
        }!!
    }

    override fun get(): ArgumentType<String> = StringArgumentType.word()

    override fun getSuggestionProvider(): SuggestionProvider<ServerCommandSource> = provider
}