package dev.terraquad.integral.networking

enum class ListType {
    MODS,
    RESOURCE_PACKS;

    fun friendlyString(): String = when (this) {
        MODS -> "mods"
        RESOURCE_PACKS -> "resource packs"
    }
}