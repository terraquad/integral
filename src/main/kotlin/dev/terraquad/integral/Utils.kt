package dev.terraquad.integral

import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import kotlin.enums.enumEntries

typealias Entries = HashMap<String, String>

fun CustomPayload.send(sender: PacketSender) = sender.sendPacket(this)
inline fun <reified E : Enum<E>> enumCodec() = PacketCodecs.indexed(
    { i -> enumEntries<E>()[i] },
    { v -> v.ordinal },
)!!
