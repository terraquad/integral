package dev.terraquad.integral.networking

import dev.terraquad.integral.textTranslatable
import net.minecraft.text.Text

enum class ListType {
    MODS,
    RESOURCE_PACKS;

    fun asText(): Text = when (this) {
        MODS -> textTranslatable("integral.list.type.mods")
        RESOURCE_PACKS -> textTranslatable("integral.list.type.resource_packs")
    }
}