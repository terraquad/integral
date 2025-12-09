package dev.terraquad.integral.networking

import dev.terraquad.integral.Integral
import dev.terraquad.integral.enumCodec
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

data class GetListS2CPayload(val type: ListType, val reason: ListReason) : CustomPacketPayload {
    companion object {
        val registry_id = ResourceLocation.fromNamespaceAndPath(Integral.MOD_ID, "get_list")!!
        val id = CustomPacketPayload.Type<GetListS2CPayload>(registry_id)
        val codec = StreamCodec.composite(
            enumCodec<ListType>(),
            GetListS2CPayload::type,
            enumCodec<ListReason>(),
            GetListS2CPayload::reason,
            ::GetListS2CPayload,
        )!!
    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload?> {
        return id
    }
}
