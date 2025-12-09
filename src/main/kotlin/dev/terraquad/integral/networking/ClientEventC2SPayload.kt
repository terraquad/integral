package dev.terraquad.integral.networking

import dev.terraquad.integral.Integral
import dev.terraquad.integral.enumCodec
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

data class ClientEventC2SPayload(val event: ClientEvent) :
    CustomPacketPayload {
    companion object {
        val registry_id = ResourceLocation.fromNamespaceAndPath(Integral.MOD_ID, "client_event")!!
        val id = CustomPacketPayload.Type<ClientEventC2SPayload>(registry_id)
        val codec = StreamCodec.composite(
            enumCodec<ClientEvent>(),
            ClientEventC2SPayload::event,
            ::ClientEventC2SPayload,
        )!!
    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload?> {
        return id
    }
}
