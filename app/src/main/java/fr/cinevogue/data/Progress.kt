package fr.cinevogue.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Une réplique notée par l'utilisateur en regardant un film en VO. */
data class MyLine(val id: String, val en: String, val fr: String, val film: String)

/** La progression d'un profil, en une photo immuable. */
data class Progress(
    val points: Int = 0,
    val sceneStars: Map<String, Int> = emptyMap(),
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val lastDay: Long = -10L,
    val missed: Set<String> = emptySet(),
    val perfectCount: Int = 0,
    val myLines: List<MyLine> = emptyList(),
)

/** Un fichier de sauvegarde par profil. Le premier profil reprend le fichier de la v1.0. */
class ProgressStore(context: Context, prefsName: String) {
    private val prefs = context.applicationContext.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    fun load(): Progress = Progress(
        points = prefs.getInt("points", 0),
        sceneStars = decode(prefs.getString("scenes", "") ?: ""),
        streak = prefs.getInt("streak", 0),
        bestStreak = prefs.getInt("bestStreak", 0),
        lastDay = prefs.getLong("lastDay", -10L),
        missed = (prefs.getStringSet("missed", emptySet()) ?: emptySet()).toSet(),
        perfectCount = prefs.getInt("perfect", 0),
        myLines = decodeLines(prefs.getString("mylines", null)),
    )

    fun save(p: Progress) {
        prefs.edit()
            .putInt("points", p.points)
            .putString("scenes", p.sceneStars.entries.joinToString(",") { "${it.key}:${it.value}" })
            .putInt("streak", p.streak)
            .putInt("bestStreak", p.bestStreak)
            .putLong("lastDay", p.lastDay)
            .putStringSet("missed", p.missed.toSet())
            .putInt("perfect", p.perfectCount)
            .putString("mylines", encodeLines(p.myLines))
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private fun encodeLines(lines: List<MyLine>): String {
        val arr = JSONArray()
        lines.forEach { arr.put(JSONObject().put("id", it.id).put("en", it.en).put("fr", it.fr).put("film", it.film)) }
        return arr.toString()
    }

    private fun decodeLines(raw: String?): List<MyLine> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                MyLine(o.getString("id"), o.getString("en"), o.getString("fr"), o.optString("film", ""))
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun decode(s: String): Map<String, Int> =
        s.split(",").mapNotNull { part ->
            val bits = part.split(":")
            if (bits.size == 2) bits[1].toIntOrNull()?.let { bits[0] to it } else null
        }.toMap()
}
