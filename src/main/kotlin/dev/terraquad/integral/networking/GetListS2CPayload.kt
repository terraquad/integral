package dev.terraquad.integral.networking

import dev.terraquad.integral.Integral
import dev.terraquad.integral.enumCodec
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

data class GetListS2CPayload(val type: ListType, val reason: ListReason) : CustomPayload {
    companion object {
        val registry_id = Identifier.of(Integral.MOD_ID, "get_list")!!
        val id = CustomPayload.Id<GetListS2CPayload>(registry_id)
        val codec = PacketCodec.tuple(
            enumCodec<ListType>(),
            GetListS2CPayload::type,
            enumCodec<ListReason>(),
            GetListS2CPayload::reason,
            ::GetListS2CPayload,
        )!!
    }

    override fun getId(): CustomPayload.Id<out CustomPayload?> {
        return GetListS2CPayload.id
    }
}
