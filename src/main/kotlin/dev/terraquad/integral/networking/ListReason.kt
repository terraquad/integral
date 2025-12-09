package dev.terraquad.integral.networking

import dev.terraquad.integral.componentTranslatable
import net.minecraft.network.chat.Component

enum class ListReason {
    JOIN,
    RELOAD,
    SET_MODPACK,
    GET_COMMAND;

    fun asText(): Component = when (this) {
        JOIN -> componentTranslatable("integral.list.reason.join")
        RELOAD -> componentTranslatable("integral.list.reason.reload")
        SET_MODPACK -> componentTranslatable("integral.list.reason.set_modpack")
        GET_COMMAND -> componentTranslatable("integral.list.reason.get_command")
    }
}