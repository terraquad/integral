package dev.terraquad.integral.networking

import dev.terraquad.integral.textTranslatable
import net.minecraft.text.Text

enum class ListReason {
    JOIN,
    RELOAD,
    SET_MODPACK,
    GET_COMMAND;

    fun asText(): Text = when (this) {
        JOIN -> textTranslatable("integral.list.reason.join")
        RELOAD -> textTranslatable("integral.list.reason.reload")
        SET_MODPACK -> textTranslatable("integral.list.reason.set_modpack")
        GET_COMMAND -> textTranslatable("integral.list.reason.get_command")
    }
}