package dev.terraquad.integral.networking

import dev.terraquad.integral.Entries
import dev.terraquad.integral.Integral
import dev.terraquad.integral.enumCodec
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

data class SendListC2SPayload(val type: ListType, val reason: ListReason, val entries: Entries) : CustomPacketPayload {
    companion object {
        val registry_id = ResourceLocation.fromNamespaceAndPath(Integral.MOD_ID, "send_list")!!
        val id = CustomPacketPayload.Type<SendListC2SPayload>(registry_id)
        val codec = StreamCodec.composite(
            enumCodec<ListType>(),
            SendListC2SPayload::type,
            enumCodec<ListReason>(),
            SendListC2SPayload::reason,
            ByteBufCodecs
                .map(::HashMap, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8),
            SendListC2SPayload::entries,
            ::SendListC2SPayload,
        )!!
    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload?> {
        return id
    }
}
