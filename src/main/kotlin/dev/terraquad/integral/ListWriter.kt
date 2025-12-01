package dev.terraquad.integral

import dev.terraquad.integral.config.Config
import dev.terraquad.integral.networking.ListReason
import dev.terraquad.integral.networking.ListType
import net.minecraft.text.Text

object ListWriter {
    private fun overlayLists(clientList: Entries, serverList: Entries): Triple<Entries, Entries, Entries> {
        val containBoth = Entries()
        val containClient = Entries()
        val containServer = Entries()

        clientList.keys.intersect(serverList.keys).forEach {
            val clientVer = clientList[it]!!
            val serverVer = serverList[it]!!
            if (clientVer != serverVer) {
                containBoth[it] = "$clientVer;$serverVer"
                return@forEach
            }
            containBoth[it] = serverVer
        }
        containClient.putAll(clientList.filter { it.key !in serverList })
        containServer.putAll(serverList.filter { it.key !in clientList })

        return Triple(containBoth, containClient, containServer)
    }

    private fun writeConclusion(
        playerName: String,
        type: ListType,
        conforms: Boolean,
    ): Text {
        val text =
            if (conforms) {
                textTranslatable("integral.list.conforms1", playerName)
            } else {
                textTranslatable("integral.list.empty", playerName)
            }
        text.append(type.asText())
        if (conforms) {
            text.append(textTranslatable("integral.list.conforms2"))
        }
        return text
    }

    fun writeReport(
        playerName: String,
        type: ListType,
        clientList: Entries,
        reason: ListReason? = null,
        includeOverlaps: Boolean = false,
    ): Text {
        if (clientList.count() == 0) {
            return writeConclusion(playerName, type, false)
        }
        val text = textTranslatable("integral.list.base", playerName).append(type.asText())
        if (reason != null) {
            text.append(textTranslatable("integral.list.reason"))
                .append(reason.asText())
        }
        val serverList = when (type) {
            ListType.MODS -> Config.modpack.mods
            ListType.RESOURCE_PACKS -> Config.modpack.resourcePacks
        }
        if (Config.prefs.compareLists && serverList != null) {
            text.append(textTranslatable("integral.list.report_changes")).append(":\n")
            val overlay = overlayLists(clientList, serverList)
            // Log added entries
            for ((id, ver) in overlay.second) {
                text.append("| + $id")
                if (ver != "") {
                    text.append(textTranslatable("integral.list.report_entry.client", ver))
                }
                text.append("\n")
            }
            // Log removed entries
            for ((id, ver) in overlay.third) {
                text.append("| - $id")
                if (ver != "") {
                    text.append(textTranslatable("integral.list.report_entry.server", ver))
                }
                text.append("\n")
            }
            // Log overlapping entries
            if (includeOverlaps) {
                for ((id, ver) in overlay.first) {
                    if (ver.contains(";")) {
                        val (clientVer, serverVer) = ver.split(";")
                        text.append("| ~ $id")
                        if (ver != "") {
                            text.append(
                                textTranslatable("integral.list.report_entry.client_server", clientVer, serverVer)
                            )
                        }
                    } else {
                        text.append("| ~ $id")
                        if (ver != "") {
                            text.append(textTranslatable("integral.list.report_entry", ver))
                        }
                    }
                    text.append("\n")
                }
            } else if (overlay.second.count() == 0 && overlay.third.count() == 0) {
                return writeConclusion(playerName, type, true)
            }
        } else {
            text.append(":\n")
            clientList.forEach { (id, ver) ->
                text.append("| ~ ").append(textTranslatable("integral.list.report_entry", id, ver))
            }
        }
        return text
    }

    fun writeSummary(
        playerName: String,
        type: ListType,
        clientList: Entries,
        reason: ListReason? = null,
    ): Text {
        if (clientList.count() == 0) {
            return writeConclusion(playerName, type, false)
        }
        val text = textTranslatable("integral.list.base", playerName).append(type.asText())
        if (reason != null) {
            text.append(textTranslatable("integral.list.reason"))
                .append(reason.asText())
        }
        text.append(": ")
        val serverList = when (type) {
            ListType.MODS -> Config.modpack.mods
            ListType.RESOURCE_PACKS -> Config.modpack.resourcePacks
        }
        if (Config.prefs.compareLists && serverList != null) {
            val overlay = overlayLists(clientList, serverList)
            if (overlay.second.count() == 0 && overlay.third.count() == 0) {
                return writeConclusion(playerName, type, true)
            } else {
                text.append(
                    textTranslatable(
                        "integral.list.summary_changes",
                        overlay.first.count(),
                        overlay.second.count(),
                        overlay.third.count(),
                    )
                )
            }
        } else {
            text.append(textTranslatable("integral.list.summary_total", clientList.count()))
        }
        return text
    }
}