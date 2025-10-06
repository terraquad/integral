package dev.terraquad.integral.networking

import dev.terraquad.integral.Integral
import dev.terraquad.integral.enumCodec
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

data class SendListC2SPayload(val type: ListType, val entries: List<HashMap<EntryProperty, String>>) : CustomPayload {
    companion object {
        val registry_id = Identifier.of(Integral.MOD_ID, "send_list")!!
        val id = CustomPayload.Id<SendListC2SPayload>(registry_id)
        val codec = PacketCodec.tuple(
            enumCodec<ListType>(),
            SendListC2SPayload::type,
            PacketCodecs
                .map(::HashMap, enumCodec<EntryProperty>(), PacketCodecs.STRING)
                .collect(PacketCodecs.toList()),
            SendListC2SPayload::entries,
            ::SendListC2SPayload,
        )!!
    }

    override fun getId(): CustomPayload.Id<out CustomPayload?> {
        return SendListC2SPayload.id
    }
}
