package dev.terraquad.integral.client

import dev.terraquad.integral.Entries
import dev.terraquad.integral.Integral
import dev.terraquad.integral.componentTranslatable
import dev.terraquad.integral.config.Config
import dev.terraquad.integral.networking.*
import dev.terraquad.integral.send
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.server.packs.repository.Pack
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
            Minecraft.getInstance()
                .resourcePackRepository
                .selectedPacks
                .filter(IntegralClient::packShouldBeReported)
                .associate { it.title.string to "" }
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

        fun packShouldBeReported(pack: Pack): Boolean {
            return !pack.isRequired && !(pack.location().knownPackInfo().getOrNull()?.isVanilla ?: false)
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

            val serverInfo = context.client().currentServer
            if (serverInfo != null && serverInfo.ip !in Config.prefs.knownServers) {
                context.client().player!!.displayClientMessage(
                    componentTranslatable("integral.privacy_message")
                        .withStyle(ChatFormatting.YELLOW), false
                )
                context.client().player!!.displayClientMessage(
                    componentTranslatable(
                        "integral.privacy_message.dismissed", serverInfo.ip
                    )
                        .withStyle(ChatFormatting.YELLOW)
                        .withStyle(ChatFormatting.ITALIC), false
                )
                Config.prefs = Config.prefs.copy(knownServers = Config.prefs.knownServers + serverInfo.ip)
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
            if (!Config.prefs.enableModInSingleplayer && (client.isLocalServer || client.singleplayerServer?.isPublished == false)) {
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
