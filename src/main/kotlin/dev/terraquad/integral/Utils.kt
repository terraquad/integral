package dev.terraquad.integral

import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.locale.Language
import net.minecraft.network.chat.Component
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import kotlin.enums.enumEntries

typealias Entries = HashMap<String, String>

fun CustomPacketPayload.send(sender: PacketSender) = sender.sendPacket(this)
inline fun <reified E : Enum<E>> enumCodec() = ByteBufCodecs.idMapper(
    { i -> enumEntries<E>()[i] },
    { v -> v.ordinal },
)!!

fun componentTranslatable(key: String, vararg args: Any) =
    Component.translatableWithFallback(key, Language.getInstance().getOrDefault(key), *args)!!
