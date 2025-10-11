@file:OptIn(ExperimentalSerializationApi::class)

package dev.terraquad.integral.config

import dev.terraquad.integral.Integral
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import net.fabricmc.loader.api.FabricLoader
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

object Config {
    private val prefsFile
        get() = FabricLoader.getInstance().configDir.resolve("integral.json")
    private val modpackFile = FabricLoader.getInstance().configDir.resolve("integral_modpack.json")
    private val configFormat = Json {
        prettyPrint = true
        encodeDefaults = true
        allowTrailingComma = true
        decodeEnumsCaseInsensitive = true
    }
    private var _prefs: ConfigPrefs? = null
    private var _modpack: Modpack? = null

    var prefs: ConfigPrefs
        get() = _prefs ?: loadPrefs()
        set(value) {
            _prefs = value
            savePrefs(value)
        }

    var modpack: Modpack
        get() = _modpack ?: loadModpack()
        set(value) {
            _modpack = value
            saveModpack(value)
        }

    fun loadPrefs(): ConfigPrefs {
        if (prefsFile.exists()) {
            try {
                _prefs = configFormat.decodeFromStream<ConfigPrefs>(prefsFile.inputStream())
            } catch (e: Exception) {
                Integral.logger.error("Couldn't read config file, it has been reset to default values", e)
                _prefs = ConfigPrefs()
                savePrefs()
            }
        } else {
            Integral.logger.info("Config file doesn't exist yet, creating it now")
            _prefs = ConfigPrefs()
            savePrefs()
        }

        Integral.logger.info("Config is ready!")
        return _prefs!!
    }

    fun loadModpack(): Modpack {
        if (prefsFile.exists()) {
            try {
                _modpack = configFormat.decodeFromStream<Modpack>(modpackFile.inputStream())
            } catch (e: Exception) {
                Integral.logger.error("Couldn't read modpack file, it has been reset to default values", e)
                _modpack = Modpack()
                saveModpack()
            }
        } else {
            Integral.logger.info("Modpack file doesn't exist yet, creating it now")
            _modpack = Modpack()
            saveModpack()
        }

        Integral.logger.info("Modpack is ready!")
        return _modpack!!
    }

    fun savePrefs() = savePrefs(_prefs ?: ConfigPrefs())

    fun savePrefs(cp: ConfigPrefs) {
        try {
            configFormat.encodeToStream(cp, prefsFile.outputStream())
            Integral.logger.info("Saved config successfully!")
        } catch (e: Exception) {
            Integral.logger.error("Couldn't save config", e)
        }
    }

    fun saveModpack() = saveModpack(_modpack ?: Modpack())

    fun saveModpack(mp: Modpack) {
        try {
            configFormat.encodeToStream(mp, modpackFile.outputStream())
            Integral.logger.info("Saved modpack successfully!")
        } catch (e: Exception) {
            Integral.logger.error("Couldn't save modpack", e)
        }
    }
}