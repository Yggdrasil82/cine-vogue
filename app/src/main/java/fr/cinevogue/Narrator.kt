package fr.cinevogue

import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

/** Une voix anglaise proposée au choix. */
data class VoiceOption(val name: String, val label: String, val detail: String)

private val ACCENTS = listOf(
    "GB" to "britannique",
    "US" to "américain",
    "AU" to "australien",
    "IE" to "irlandais",
    "CA" to "canadien",
    "IN" to "indien",
    "ZA" to "sud-africain",
    "NG" to "nigérian",
)

/** La voix du téléphone : accent britannique par défaut, ou la voix choisie par le profil. */
class Narrator(context: Context) : TextToSpeech.OnInitListener {
    private val tts = TextToSpeech(context.applicationContext, this)
    private var ready = false
    private var raw: List<Voice> = emptyList()

    var ukAvailable by mutableStateOf(true)
        private set

    var voices by mutableStateOf<List<VoiceOption>>(emptyList())
        private set

    /** Vrai pendant que la voix parle (pour animer la bouche du personnage). */
    var speaking by mutableStateOf(false)
        private set

    private val main = android.os.Handler(android.os.Looper.getMainLooper())

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            ukAvailable = false
            return
        }
        val result = tts.setLanguage(Locale.UK)
        val ok = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
        if (!ok) tts.setLanguage(Locale.US)
        ukAvailable = ok
        ready = true
        tts.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) { main.post { speaking = true } }
            override fun onDone(utteranceId: String?) { main.post { speaking = false } }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) { main.post { speaking = false } }
        })
        refresh()
    }

    /** Relit la liste des voix (utile après en avoir installé dans les réglages). */
    fun refresh() {
        if (!ready) return
        raw = try {
            tts.voices.orEmpty()
                .filter { it.locale.language == "en" }
                .filter { !it.features.orEmpty().contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED) }
                .sortedWith(
                    compareBy<Voice>(
                        { v -> ACCENTS.indexOfFirst { it.first == v.locale.country }.let { if (it < 0) 99 else it } },
                        { it.isNetworkConnectionRequired },
                        { -it.quality },
                        { it.name },
                    ),
                )
        } catch (e: Exception) {
            emptyList()
        }
        voices = raw.mapIndexed { i, v ->
            val accent = ACCENTS.firstOrNull { it.first == v.locale.country }?.second ?: "anglais"
            val gender = genderOf(v.name)
            val bits = mutableListOf("accent $accent")
            if (v.quality >= Voice.QUALITY_HIGH) bits += "haute qualité"
            bits += if (v.isNetworkConnectionRequired) "avec internet" else "hors ligne"
            VoiceOption(
                name = v.name,
                label = "Voix ${i + 1}" + (gender?.let { " · $it" } ?: ""),
                detail = bits.joinToString(" · "),
            )
        }
    }

    private fun genderOf(name: String): String? {
        val n = name.lowercase(Locale.ROOT)
        return when {
            "female" in n -> "femme"
            "male" in n -> "homme"
            else -> Regex("smt([fm])\\d").find(n)?.groupValues?.get(1)?.let { if (it == "f") "femme" else "homme" }
        }
    }

    fun label(name: String?): String =
        voices.firstOrNull { it.name == name }?.let { "${it.label} (${it.detail})" } ?: "Automatique (accent britannique)"

    fun speak(text: String, slow: Boolean = false, voiceName: String? = null, pitch: Float = 1.0f) {
        if (!ready) return
        val chosen = voiceName?.let { n -> raw.firstOrNull { it.name == n } }
        if (chosen != null) {
            try {
                tts.setVoice(chosen)
            } catch (e: Exception) {
                tts.setLanguage(Locale.UK)
            }
        } else {
            tts.setLanguage(if (ukAvailable) Locale.UK else Locale.US)
        }
        tts.setPitch(pitch)
        tts.setSpeechRate(if (slow) 0.6f else 0.95f)
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "cinevogue")
    }

    fun stop() {
        tts.stop()
        speaking = false
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }

    companion object {
        /** Ouvre les réglages de synthèse vocale d'Android pour installer d'autres voix. */
        fun openSettings(context: Context) {
            val intents = listOf(
                Intent("com.android.settings.TTS_SETTINGS"),
                Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA),
                Intent(android.provider.Settings.ACTION_SETTINGS),
            )
            for (intent in intents) {
                try {
                    context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    return
                } catch (e: Exception) {
                    // On essaie le réglage suivant.
                }
            }
        }
    }
}
