package fr.cinevogue.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

enum class Gender { FILLE, GARCON }

/** Les films choisis par un profil, dans l'ordre du catalogue. */
fun filmsOf(p: Profile): List<Genre> = filmsOfLevel(p.level).filter { it.id in p.films }

data class Profile(
    val id: String,
    val name: String,
    val gender: Gender,
    val level: Level,
    val color: Int,
    val voice: String?,
    val prefs: String,
    /** Films choisis (identifiants). */
    val films: List<String> = emptyList(),
)

/** Réglages communs à toute la famille. */
data class Settings(
    val reminderOn: Boolean = true,
    val reminderHour: Int = 18,
    val reminderMinute: Int = 30,
)

/** Liste des profils et réglages communs (fichier « cinevogue_app »). */
class ProfileRepo(context: Context) {
    private val ctx = context.applicationContext
    private val app = ctx.getSharedPreferences("cinevogue_app", Context.MODE_PRIVATE)
    private val legacy = ctx.getSharedPreferences(LEGACY, Context.MODE_PRIVATE)

    fun profiles(): List<Profile> {
        val raw = app.getString("profiles", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Profile(
                    id = o.getString("id"),
                    name = o.getString("name"),
                    gender = runCatching { Gender.valueOf(o.getString("gender")) }.getOrDefault(Gender.FILLE),
                    level = runCatching { Level.valueOf(o.getString("level")) }.getOrDefault(Level.DEBUTANT),
                    color = o.optInt("color", 0),
                    voice = if (o.isNull("voice")) null else o.optString("voice").ifBlank { null },
                    prefs = o.getString("prefs"),
                    films = o.optJSONArray("films")?.let { a -> (0 until a.length()).map { a.getString(it) } } ?: emptyList(),
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun write(list: List<Profile>) {
        val arr = JSONArray()
        list.forEach { p ->
            arr.put(
                JSONObject()
                    .put("id", p.id)
                    .put("name", p.name)
                    .put("gender", p.gender.name)
                    .put("level", p.level.name)
                    .put("color", p.color)
                    .put("voice", p.voice ?: JSONObject.NULL)
                    .put("prefs", p.prefs)
                    .put("films", JSONArray(p.films)),
            )
        }
        app.edit().putString("profiles", arr.toString()).apply()
    }

    /** Vrai si une progression de la v1.0 attend d'être rattachée au premier profil. */
    fun hasLegacyProgress(): Boolean =
        profiles().isEmpty() && (legacy.getInt("points", 0) > 0 || !legacy.getString("scenes", "").isNullOrEmpty())

    fun create(name: String, gender: Gender, level: Level, color: Int, films: List<String>): Profile {
        val list = profiles()
        val id = "p" + System.currentTimeMillis()
        val prefs = if (list.none { it.prefs == LEGACY }) LEGACY else "cinevogue_$id"
        val profile = Profile(id, name.trim(), gender, level, color, null, prefs, films)
        write(list + profile)
        return profile
    }

    fun update(profile: Profile) {
        write(profiles().map { if (it.id == profile.id) profile else it })
    }

    fun delete(profile: Profile) {
        ProgressStore(ctx, profile.prefs).clear()
        write(profiles().filter { it.id != profile.id })
    }

    fun settings(): Settings = Settings(
        reminderOn = app.getBoolean("reminderOn", legacy.getBoolean("reminderOn", true)),
        reminderHour = app.getInt("reminderHour", legacy.getInt("reminderHour", 18)),
        reminderMinute = app.getInt("reminderMinute", legacy.getInt("reminderMinute", 30)),
    )

    fun saveSettings(s: Settings) {
        app.edit()
            .putBoolean("reminderOn", s.reminderOn)
            .putInt("reminderHour", s.reminderHour)
            .putInt("reminderMinute", s.reminderMinute)
            .apply()
    }

    /** Clé Mistral saisie par un parent (reste sur le téléphone). */
    var mistralKey: String
        get() = app.getString("mistralKey", "") ?: ""
        set(value) { app.edit().putString("mistralKey", value.trim()).apply() }

    var mistralModel: String
        get() = app.getString("mistralModel", "") ?: ""
        set(value) { app.edit().putString("mistralModel", value.trim()).apply() }

    /** Code parent, enregistré sous forme d'empreinte (jamais en clair). */
    fun hasPin(): Boolean = !app.getString("pinHash", null).isNullOrEmpty()

    fun checkPin(pin: String): Boolean = app.getString("pinHash", null) == hash(pin)

    fun setPin(pin: String) {
        app.edit().putString("pinHash", hash(pin)).apply()
    }

    private fun hash(pin: String): String =
        java.security.MessageDigest.getInstance("SHA-256")
            .digest(("cinevogue:" + pin).toByteArray())
            .joinToString("") { "%02x".format(it) }

    var askedNotifications: Boolean
        get() = app.getBoolean("askedNotif", legacy.getBoolean("askedNotif", false))
        set(value) { app.edit().putBoolean("askedNotif", value).apply() }

    companion object {
        const val LEGACY = "cinevogue"
    }
}
