package dev.terraquad.integral.command

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import de.maxhenkel.admiral.argumenttype.ArgumentTypeConverter
import dev.terraquad.integral.networking.ListType
import net.minecraft.server.command.ServerCommandSource

class ListTypeArgumentTypeSupplier : ArgumentTypeConverter<ServerCommandSource, String, ListType> {
    override fun convert(context: CommandContext<ServerCommandSource>, str: String) =
        runCatching { ListType.valueOf(str) }.onFailure {
            throw SimpleCommandExceptionType(LiteralMessage("Invalid list type: $str")).create()
        }.getOrThrow()
}