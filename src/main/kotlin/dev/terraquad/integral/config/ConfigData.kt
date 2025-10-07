package dev.terraquad.integral.config

import dev.terraquad.integral.networking.EntryProperty
import kotlinx.serialization.Serializable

@Serializable
data class ConfigData(
    val enableModInSingleplayer: Boolean = false,
    val requestModsOnJoin: Boolean = true,
    val requestResourcePacksOnJoin: Boolean = true,
    val requestResourcePacksOnReload: Boolean = true,
    val modProperties: List<EntryProperty> = listOf(EntryProperty.NAME, EntryProperty.VERSION),
    val resourcePackProperties: List<EntryProperty> = listOf(EntryProperty.NAME),
    val logPlayersWithoutMod: Boolean = true,
    val logGeyserPlayers: Boolean = false,
    val sendListsToDiscord: Boolean = false,
)
