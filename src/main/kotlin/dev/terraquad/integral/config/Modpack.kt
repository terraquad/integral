package dev.terraquad.integral.config

import dev.terraquad.integral.Entries
import kotlinx.serialization.Serializable

@Serializable
data class Modpack(val mods: Entries? = null, val resourcePacks: Entries? = null)
