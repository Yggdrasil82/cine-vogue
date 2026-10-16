package fr.cinevogue.data

import android.content.Context

/** Tout ce que l'appli retient, en une photo immuable. */
data class Progress(
    val points: Int = 0,
    val sceneStars: Map<String, Int> = emptyMap(),
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val lastDay: Long = -10L,
    val missed: Set<String> = emptySet(),
    val perfectCount: Int = 0,
    val reminderOn: Boolean = true,
    val reminderHour: Int = 18,
    val reminderMinute: Int = 30,
)

class ProgressStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("cinevogue", Context.MODE_PRIVATE)

    fun load(): Progress = Progress(
        points = prefs.getInt("points", 0),
        sceneStars = decode(prefs.getString("scenes", "") ?: ""),
        streak = prefs.getInt("streak", 0),
        bestStreak = prefs.getInt("bestStreak", 0),
        lastDay = prefs.getLong("lastDay", -10L),
        missed = (prefs.getStringSet("missed", emptySet()) ?: emptySet()).toSet(),
        perfectCount = prefs.getInt("perfect", 0),
        reminderOn = prefs.getBoolean("reminderOn", true),
        reminderHour = prefs.getInt("reminderHour", 18),
        reminderMinute = prefs.getInt("reminderMinute", 30),
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
            .putBoolean("reminderOn", p.reminderOn)
            .putInt("reminderHour", p.reminderHour)
            .putInt("reminderMinute", p.reminderMinute)
            .apply()
    }

    var askedNotifications: Boolean
        get() = prefs.getBoolean("askedNotif", false)
        set(value) { prefs.edit().putBoolean("askedNotif", value).apply() }

    private fun decode(s: String): Map<String, Int> =
        s.split(",").mapNotNull { part ->
            val bits = part.split(":")
            if (bits.size == 2) bits[1].toIntOrNull()?.let { bits[0] to it } else null
        }.toMap()
}
