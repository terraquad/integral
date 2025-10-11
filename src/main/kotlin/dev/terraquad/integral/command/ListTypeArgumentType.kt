package dev.terraquad.integral.command

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import dev.terraquad.integral.networking.ListType
import net.minecraft.command.CommandSource
import java.util.concurrent.CompletableFuture

class ListTypeArgumentType : ArgumentType<ListType> {
    companion object {
        val invalidListType = DynamicCommandExceptionType { o -> LiteralMessage("Invalid list type: $o") }

        fun <S> getListType(context: CommandContext<S>, name: String) = context.getArgument(name, ListType::class.java)
    }

    override fun parse(reader: StringReader): ListType {
        val argStart = reader.cursor
        if (!reader.canRead()) reader.skip()

        val listStr = reader.readUnquotedString()
        return runCatching { ListType.valueOf(listStr) }
            .onFailure {
                reader.cursor = argStart
                throw invalidListType.createWithContext(reader, listStr)
            }
            .getOrThrow()
    }

    override fun getExamples(): Collection<String> = ListType.entries.map { it.toString() }

    override fun <S> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> = CommandSource.suggestMatching(examples, builder)
}