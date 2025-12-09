package dev.terraquad.integral.networking

import dev.terraquad.integral.componentTranslatable
import net.minecraft.network.chat.Component

enum class ListType {
    MODS,
    RESOURCE_PACKS;

    fun asText(): Component = when (this) {
        MODS -> componentTranslatable("integral.list.type.mods")
        RESOURCE_PACKS -> componentTranslatable("integral.list.type.resource_packs")
    }
}