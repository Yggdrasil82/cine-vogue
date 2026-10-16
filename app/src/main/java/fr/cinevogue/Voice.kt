package fr.cinevogue

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

/** Voix anglaise du téléphone, accent britannique, hors ligne. */
class Voice(context: Context) : TextToSpeech.OnInitListener {
    private val tts = TextToSpeech(context.applicationContext, this)
    private var ready = false

    var ukAvailable by mutableStateOf(true)
        private set

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            ukAvailable = false
            return
        }
        val result = tts.setLanguage(Locale.UK)
        val ok = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
        if (!ok) tts.setLanguage(Locale.US)
        ukAvailable = ok
        tts.setPitch(1.08f)
        ready = true
    }

    fun speak(text: String, slow: Boolean = false) {
        if (!ready) return
        tts.setSpeechRate(if (slow) 0.6f else 0.95f)
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "cinevogue")
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}
