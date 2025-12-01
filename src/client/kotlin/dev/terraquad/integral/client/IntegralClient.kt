package dev.terraquad.integral.client

import dev.terraquad.integral.Entries
import dev.terraquad.integral.Integral
import dev.terraquad.integral.config.Config
import dev.terraquad.integral.networking.*
import dev.terraquad.integral.send
import dev.terraquad.integral.textTranslatable
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import net.minecraft.client.MinecraftClient
import net.minecraft.resource.ResourcePackProfile
import net.minecraft.util.Formatting
import kotlin.jvm.optionals.getOrNull

class IntegralClient : ClientModInitializer {
    companion object {
        // Determines whether the client should respond to list requests
        @JvmStatic
        var ready = false

        fun getModList() = Entries(
            FabricLoader.getInstance()
                .allMods
                .filter(IntegralClient::modShouldBeReported)
                .associate { it.metadata.id to it.metadata.version.friendlyString }
        )

        @JvmStatic
        fun getPackList() = Entries(
            MinecraftClient.getInstance()
                .resourcePackManager
                .enabledProfiles
                .filter(IntegralClient::packShouldBeReported)
                .associate { it.displayName.string to "" }
        )

        fun modShouldBeReported(mod: ModContainer): Boolean {
            val isBuiltin = mod.metadata.type == "builtin"
            val isFabric = mod.metadata.id.startsWith("fabric")
            val hasModMenuLibraryBadge = mod.metadata
                .customValues["modmenu"]
                ?.asObject
                ?.get("badges")
                ?.asArray
                ?.find { badge -> badge.asString == "library" } != null
            // Ensures that unmarked library mods don't show up
            val isTopLevel = mod.containingMod.isEmpty

            return !isBuiltin && !isFabric && !hasModMenuLibraryBadge && isTopLevel
        }

        fun packShouldBeReported(pack: ResourcePackProfile): Boolean {
            return !pack.isRequired && !(pack.info.knownPackInfo().getOrNull()?.isVanilla ?: false)
        }
    }

    override fun onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(GetListS2CPayload.id) { payload, context ->
            if (!ready) return@registerGlobalReceiver
            Integral.logger.debug(
                "Received {} list request from server because of {}",
                payload.type,
                payload.reason,
            )

            val serverInfo = context.client().currentServerEntry
            if (serverInfo != null && serverInfo.address !in Config.prefs.knownServers) {
                context.client().player!!.sendMessage(
                    textTranslatable("integral.privacy_message")
                        .formatted(Formatting.YELLOW), false
                )
                context.client().player!!.sendMessage(
                    textTranslatable(
                        "integral.privacy_message.dismissed", serverInfo.address
                    )
                        .formatted(Formatting.YELLOW)
                        .formatted(Formatting.ITALIC), false
                )
                Config.prefs = Config.prefs.copy(knownServers = Config.prefs.knownServers + serverInfo.address)
            }

            SendListC2SPayload(
                payload.type,
                payload.reason,
                when (payload.type) {
                    ListType.MODS -> getModList()
                    ListType.RESOURCE_PACKS -> getPackList()
                },
            ).send(context.responseSender())
        }

        ClientPlayConnectionEvents.JOIN.register { _, sender, client ->
            if (!Config.prefs.enableModInSingleplayer && (client.isInSingleplayer || client.server?.isRemote == false)) {
                Integral.logger.info("Detected singleplayer, disabling until player joins another world")
                return@register
            }
            ClientEventC2SPayload(ClientEvent.READY).send(sender)
            ready = true
        }

        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            ready = false
        }
    }
}
