package dev.terraquad.integral.client

import dev.terraquad.integral.Integral
import dev.terraquad.integral.networking.*
import dev.terraquad.integral.send
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import net.minecraft.client.MinecraftClient
import net.minecraft.resource.ResourcePackProfile
import kotlin.jvm.optionals.getOrNull

class IntegralClient : ClientModInitializer {
    companion object {
        @JvmStatic
        var ready = false

        fun sendModList(sender: PacketSender, props: List<EntryProperty>) {
            if (!ready) return

            val modList = FabricLoader
                .getInstance()
                .allMods
                .filter(IntegralClient::modShouldBeReported)
                .map {
                    val modData = HashMap.newHashMap<EntryProperty, String>(props.count())
                    for (prop in props) {
                        modData[prop] = when (prop) {
                            EntryProperty.ID -> it.metadata.id
                            EntryProperty.NAME -> it.metadata.name
                            EntryProperty.VERSION -> it.metadata.version.friendlyString
                        }
                    }
                    modData
                }.toList()

            SendListC2SPayload(
                ListType.MODS,
                modList,
            ).send(sender)
        }

        @JvmStatic
        fun sendPackList(sender: PacketSender, props: List<EntryProperty>) {
            if (!ready) return

            val packList = MinecraftClient
                .getInstance()
                .resourcePackManager
                .enabledProfiles
                .filter(IntegralClient::packShouldBeReported)
                .map {
                    val packData = HashMap.newHashMap<EntryProperty, String>(props.count())
                    for (prop in props) {
                        packData[prop] = when (prop) {
                            EntryProperty.ID -> it.id
                            EntryProperty.NAME -> it.displayName.string
                            EntryProperty.VERSION -> continue
                        }
                    }
                    packData
                }.toList()

            SendListC2SPayload(
                ListType.RESOURCE_PACKS,
                packList,
            ).send(sender)
        }

        fun modShouldBeReported(mod: ModContainer): Boolean {
            val isBuiltin = mod.metadata.type == "builtin"
            val isFabric = mod.metadata.id.startsWith("fabric")
            val hasModMenuLibraryBadge = mod
                .metadata
                .customValues["modmenu"]
                ?.asObject
                ?.get("badges")
                ?.asArray
                ?.find { badge -> badge.asString == "library" } != null

            return !isBuiltin && !isFabric && !hasModMenuLibraryBadge
        }

        fun packShouldBeReported(pack: ResourcePackProfile): Boolean {
            return !pack.isRequired && !(pack.info.knownPackInfo().getOrNull()?.isVanilla ?: false)
        }
    }

    override fun onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(GetListS2CPayload.id) { payload, context ->
            Integral.logger.debug(
                "Received {} list request (properties: {}) from server",
                payload.type,
                payload.props.joinToString(", "),
            )
            when (payload.type) {
                ListType.MODS -> sendModList(context.responseSender(), payload.props)
                ListType.RESOURCE_PACKS -> sendPackList(context.responseSender(), payload.props)
            }
        }

        ClientPlayConnectionEvents.JOIN.register { _, sender, _ ->
            ClientEventC2SPayload(ClientEvent.READY).send(sender)
            ready = true
        }

        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            ready = false
        }
    }
}
