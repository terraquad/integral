package dev.terraquad.integral.config

import kotlinx.serialization.Serializable

@Serializable
data class ConfigPrefs(
    val knownServers: List<String> = listOf(),
    val enableModInSingleplayer: Boolean = false,
    val compareLists: Boolean = true,
    val includeOverlaps: Boolean = true,
    val requestModsOnJoin: Boolean = true,
    val requestResourcePacksOnJoin: Boolean = true,
    val requestResourcePacksOnReload: Boolean = true,
    val reportPlayersWithoutMod: Boolean = true,
    val reportGeyserPlayers: Boolean = true,
    val sendListsToDiscord: Boolean = false,
    val summarizeToOperators: Boolean = false,
    val summarizeEverywhere: Boolean = false,
)