package fr.cinevogue.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import fr.cinevogue.data.Pronunciation
import fr.cinevogue.data.PronunciationResult
import kotlinx.coroutines.launch

/** Ce dont l'atelier de prononciation a besoin du reste de l'appli. */
class RepeatTools(
    val speak: (text: String, slow: Boolean) -> Unit,
    /** Conseil de l'IA pour un mot : (mot, phrase, compris) → conseil. Null si l'IA n'est pas configurée. */
    val aiTip: (suspend (String, String, String) -> String)?,
)

/** L'atelier « Répète après moi », en plein écran par-dessus l'écran en cours. */
@Composable
fun RepeatDialog(target: String, tools: RepeatTools, onClose: () -> Unit) {
    var result by remember(target) { mutableStateOf<PronunciationResult?>(null) }
    var error by remember(target) { mutableStateOf<String?>(null) }
    var aiTips by remember(target) { mutableStateOf(mapOf<String, String>()) }
    var aiLoading by remember(target) { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val listen = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        val alts = res.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).orEmpty()
        if (alts.isNotEmpty()) {
            result = Pronunciation.check(target, alts)
            error = null
        }
    }
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        RepeatContent(
            target = target,
            result = result,
            error = error,
            aiTips = aiTips,
            aiLoading = aiLoading,
            aiAvailable = tools.aiTip != null,
            onListen = { slow -> tools.speak(target, slow) },
            onWord = { w -> tools.speak(w, true) },
            onMic = {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                    .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    .putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-GB")
                    .putExtra(RecognizerIntent.EXTRA_PROMPT, target)
                    .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                try {
                    listen.launch(intent)
                } catch (e: ActivityNotFoundException) {
                    error = "La reconnaissance vocale n'est pas disponible sur ce téléphone."
                }
            },
            onAskAi = { w ->
                val ai = tools.aiTip ?: return@RepeatContent
                aiLoading = w
                scope.launch {
                    val tip = try {
                        ai(w, target, result?.heard.orEmpty())
                    } catch (e: Exception) {
                        e.message ?: "L'IA n'a pas pu répondre."
                    }
                    aiTips = aiTips + (w to tip)
                    aiLoading = null
                }
            },
            onClose = onClose,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RepeatContent(
    target: String,
    result: PronunciationResult?,
    error: String?,
    aiTips: Map<String, String>,
    aiLoading: String?,
    aiAvailable: Boolean,
    onListen: (Boolean) -> Unit,
    onWord: (String) -> Unit,
    onMic: () -> Unit,
    onAskAi: (String) -> Unit,
    onClose: () -> Unit,
) {
    Column(Modifier.fillMaxSize().background(Cv.Bg)) {
        Row(Modifier.fillMaxWidth().background(Cv.Header).padding(start = 8.dp, end = 22.dp, top = 12.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) { Icon(Icons.Filled.Close, contentDescription = "Fermer", tint = Cv.Cream) }
            Text("RÉPÈTE APRÈS MOI", fontFamily = Bebas, fontSize = 30.sp, letterSpacing = 2.sp, color = Cv.Gold)
        }
        Box(Modifier.fillMaxWidth().height(3.dp).background(Cv.Gold))
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("« $target »", fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, lineHeight = 30.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GoldButton("Écouter", onClick = { onListen(false) }, fontSize = 18)
                LineButton("Lentement", onClick = { onListen(true) }, color = Cv.Gold, border = Cv.Gold, modifier = Modifier.height(56.dp))
            }
            Box(
                Modifier.size(84.dp).clip(CircleShape).background(Cv.Gold).clickable(onClickLabel = "Répéter au micro", onClick = onMic),
                contentAlignment = Alignment.Center,
            ) {
                Text("À TOI", fontFamily = Bebas, fontSize = 22.sp, color = Cv.Bg)
            }
            error?.let { Text(it, color = Cv.Bad, fontSize = 14.sp, textAlign = TextAlign.Center) }
            if (result == null) {
                Text("Écoute la phrase, puis touche « À toi » et répète-la clairement, près du micro.", fontSize = 14.sp, color = Cv.Muted, textAlign = TextAlign.Center)
            } else {
                val score = result.score
                Text(
                    when {
                        score == 100 -> "Parfait, le téléphone a tout compris !"
                        score >= 70 -> "Presque ! Retravaille les mots en rouge."
                        else -> "Encore une fois : écoute lentement, puis répète."
                    },
                    fontWeight = FontWeight.Bold, fontSize = 17.sp, color = if (score == 100) Cv.Good else if (score >= 70) Cv.Gold else Cv.Bad,
                    textAlign = TextAlign.Center,
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    result.words.forEach { w ->
                        Text(
                            w.word,
                            fontSize = 17.sp, fontWeight = FontWeight.Bold,
                            color = if (w.ok) Cv.Good else Cv.Bad,
                            modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(if (w.ok) Cv.GoodBg else Cv.BadBg)
                                .clickable { onWord(w.word) }.padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                    }
                }
                Text("Le téléphone a compris : « ${result.heard} »", fontSize = 13.sp, fontStyle = FontStyle.Italic, color = Cv.Muted, textAlign = TextAlign.Center)
                result.missed.distinct().take(3).forEach { word ->
                    Column(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Cv.Card).border(1.dp, Cv.Line, RoundedCornerShape(14.dp)).padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(word, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Cv.Bad, modifier = Modifier.weight(1f))
                            LineButton("Écouter lentement", onClick = { onWord(word) }, color = Cv.Gold, border = Cv.Gold)
                        }
                        Pronunciation.tips(word).forEach { Text("•  $it", fontSize = 14.sp, lineHeight = 20.sp) }
                        val ai = aiTips[word]
                        when {
                            ai != null -> Text("Conseil de l'IA : $ai", fontSize = 14.sp, color = Cv.Gold, lineHeight = 20.sp)
                            aiLoading == word -> Text("L'IA réfléchit…", fontSize = 13.sp, color = Cv.Muted, fontStyle = FontStyle.Italic)
                            aiAvailable -> LineButton("Conseil de l'IA", onClick = { onAskAi(word) }, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }
    }
}
