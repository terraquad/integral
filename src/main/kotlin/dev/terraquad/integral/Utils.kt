package dev.terraquad.integral

import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.text.Text
import net.minecraft.util.Language
import kotlin.enums.enumEntries

typealias Entries = HashMap<String, String>

fun CustomPayload.send(sender: PacketSender) = sender.sendPacket(this)
inline fun <reified E : Enum<E>> enumCodec() = PacketCodecs.indexed(
    { i -> enumEntries<E>()[i] },
    { v -> v.ordinal },
)!!

fun textTranslatable(key: String) = Text.translatableWithFallback(key, Language.getInstance().get(key))!!
fun textTranslatable(key: String, vararg args: Any) =
    Text.translatableWithFallback(key, Language.getInstance().get(key), args)!!
