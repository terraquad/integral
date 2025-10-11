package dev.terraquad.integral.config

import kotlinx.serialization.Serializable

@Serializable
data class ConfigPrefs(
    val enableModInSingleplayer: Boolean = false,
    val compareLists: Boolean = true,
    val includeOverlaps: Boolean = false,
    val requestModsOnJoin: Boolean = true,
    val requestResourcePacksOnJoin: Boolean = true,
    val requestResourcePacksOnReload: Boolean = true,
    val reportConformingPlayers: Boolean = false,
    val reportPlayersWithoutMod: Boolean = true,
    val reportGeyserPlayers: Boolean = false,
    val sendListsToDiscord: Boolean = false,
)