package dev.terraquad.integral.networking

import dev.terraquad.integral.Integral
import dev.terraquad.integral.enumCodec
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

data class ClientEventC2SPayload(val event: ClientEvent) :
    CustomPayload {
    companion object {
        val registry_id = Identifier.of(Integral.MOD_ID, "client_event")!!
        val id = CustomPayload.Id<ClientEventC2SPayload>(registry_id)
        val codec = PacketCodec.tuple(
            enumCodec<ClientEvent>(),
            ClientEventC2SPayload::event,
            ::ClientEventC2SPayload,
        )!!
    }

    override fun getId(): CustomPayload.Id<out CustomPayload?> {
        return ClientEventC2SPayload.id
    }
}
