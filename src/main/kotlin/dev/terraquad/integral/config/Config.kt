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
    private const val CONFIG_FILE_NAME = "integral.json"
    private val configFile
        get() = FabricLoader.getInstance().configDir.resolve(CONFIG_FILE_NAME)
    private val configFormat = Json {
        prettyPrint = true
        encodeDefaults = true
        allowTrailingComma = true
    }
    private var _data: ConfigData? = null

    val data: ConfigData
        get() = _data ?: load()

    fun load(): ConfigData {
        if (configFile.exists()) {
            try {
                _data = configFormat.decodeFromStream<ConfigData>(configFile.inputStream())
            } catch (e: Exception) {
                Integral.logger.error("Couldn't read config file, it has been reset to default values", e)
                _data = ConfigData()
                overwriteSave()
            }
        } else {
            Integral.logger.info("Config file doesn't exist yet, creating it now")
            _data = ConfigData()
            overwriteSave()
        }

        Integral.logger.info("Config is ready!")
        return _data!!
    }

    fun save() {
        if (_data == null) {
            throw UninitializedPropertyAccessException("Tried saving unloaded config")
        }
        overwriteSave()
    }

    fun overwriteSave() {
        try {
            configFormat.encodeToStream(_data ?: ConfigData(), configFile.outputStream())
        } catch (e: Exception) {
            Integral.logger.error("Couldn't save config", e)
        }
    }
}