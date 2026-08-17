package io.github.allendior.almanac.data.archive

import io.github.allendior.almanac.domain.EntryValidation
import io.github.allendior.almanac.domain.PortraitEntry
import org.json.JSONArray
import org.json.JSONObject

/**
 * The portable form of the archive: a plain ZIP containing `index.json` and an
 * `originals/` directory of untouched JPEGs.
 *
 * Deliberately boring. Anyone with a laptop and no copy of this app — including the
 * owner in 2036 — can unzip it, read the JSON in a text editor, and look at the
 * photographs. That is the point of the format.
 */
object ArchiveFormat {

    const val FORMAT = "almanac-archive"
    const val VERSION = 2
    const val INDEX_NAME = "index.json"
    const val ORIGINALS_PREFIX = "originals/"

    fun writeIndex(entries: List<PortraitEntry>, exportedAtEpochMs: Long, offsetMinutes: Int): String {
        val array = JSONArray()
        entries.sortedWith(compareBy({ it.dayId }, { it.capturedAtEpochMs })).forEach { entry ->
            array.put(
                JSONObject().apply {
                    put("id", entry.id)
                    put("dayId", entry.dayId)
                    put("capturedAtEpochMs", entry.capturedAtEpochMs)
                    put("utcOffsetMinutes", entry.utcOffsetMinutes)
                    put("fileName", entry.fileName)
                    put("sha256", entry.sha256)
                    put("cameraFacing", entry.cameraFacing.name)
                    entry.mood?.let { put("mood", it.name) }
                    entry.numberValue?.let { put("numberValue", it) }
                    entry.note?.let { put("note", it) }
                },
            )
        }
        return JSONObject().apply {
            put("format", FORMAT)
            put("version", VERSION)
            put("exportedAtEpochMs", exportedAtEpochMs)
            put("exportedAtOffsetMinutes", offsetMinutes)
            put("entryCount", entries.size)
            put("entries", array)
        }.toString(2)
    }

    sealed interface ParseResult {
        data class Parsed(val rows: List<EntryValidation.Result>) : ParseResult
        data class NotAnArchive(val reason: String) : ParseResult
    }

    fun readIndex(json: String): ParseResult {
        val root = runCatching { JSONObject(json) }.getOrElse {
            return ParseResult.NotAnArchive("index.json is not readable JSON")
        }
        if (root.optString("format") != FORMAT) {
            return ParseResult.NotAnArchive("this file is not an Almanac archive")
        }
        if (root.optInt("version", -1) > VERSION) {
            return ParseResult.NotAnArchive(
                "the archive was written by a newer version of Almanac; update the app first",
            )
        }
        val array = root.optJSONArray("entries")
            ?: return ParseResult.NotAnArchive("the archive lists no entries")

        val rows = (0 until array.length()).map { i ->
            val o = array.optJSONObject(i)
                ?: return@map EntryValidation.Result.Rejected(null, "row is not an object")
            EntryValidation.validate(
                id = o.optStringOrNull("id"),
                dayId = o.optStringOrNull("dayId"),
                capturedAtEpochMs = if (o.has("capturedAtEpochMs")) o.optLong("capturedAtEpochMs") else null,
                utcOffsetMinutes = if (o.has("utcOffsetMinutes")) o.optInt("utcOffsetMinutes", 9999) else null,
                fileName = o.optStringOrNull("fileName"),
                sha256 = o.optStringOrNull("sha256"),
                mood = o.optStringOrNull("mood"),
                numberValue = if (o.has("numberValue")) o.optDouble("numberValue") else null,
                cameraFacing = o.optStringOrNull("cameraFacing"),
                note = o.optStringOrNull("note"),
            )
        }
        return ParseResult.Parsed(rows)
    }

    private fun JSONObject.optStringOrNull(key: String): String? =
        if (has(key) && !isNull(key)) optString(key).takeIf { it.isNotEmpty() } else null
}

/** What the owner is shown after an import. Every number is accounted for. */
data class ImportReport(
    val added: Int = 0,
    val duplicate: Int = 0,
    val unreadable: Int = 0,
    val rejected: Int = 0,
    val failureReason: String? = null,
    val details: List<String> = emptyList(),
) {
    val total: Int get() = added + duplicate + unreadable + rejected
    val failed: Boolean get() = failureReason != null
}
