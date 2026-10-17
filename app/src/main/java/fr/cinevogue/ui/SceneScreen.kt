package fr.cinevogue.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.cinevogue.data.Game
import fr.cinevogue.data.Question
import fr.cinevogue.data.Studio
import kotlinx.coroutines.delay

data class QuestionUi(
    val q: Question,
    val number: Int,
    val total: Int,
    val selected: Int?,
    val checked: Boolean,
    val eliminated: Set<Int>,
    val secondsLeft: Int?,
    /** En-tête : « Scène 2 » ou « Mes répliques ». */
    val header: String,
    val genreTitle: String,
    val feminine: Boolean = true,
)

private val PRAISES_F = listOf("Bien joué !", "Parfait !", "Une vraie pro !", "Magnifique !", "Dans la boîte !", "Superbe prise !")
private val PRAISES_M = listOf("Bien joué !", "Parfait !", "Un vrai pro !", "Magnifique !", "Dans la boîte !", "Superbe prise !")

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SceneScreen(
    ui: QuestionUi,
    onSelect: (Int) -> Unit,
    onAction: () -> Unit,
    onHint: () -> Unit,
    onListen: (Boolean) -> Unit,
    onQuit: () -> Unit,
) {
    val q = ui.q
    Column(Modifier.fillMaxSize().background(Cv.Bg)) {
        Column(
            Modifier.padding(start = 12.dp, end = 22.dp, top = 12.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onQuit) {
                    Icon(Icons.Filled.Close, contentDescription = "Quitter la scène", tint = Cv.Cream)
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${ui.header} · ${q.game.label}", fontFamily = Bebas, fontSize = 22.sp, letterSpacing = 2.sp, color = Cv.Gold)
                    Text(ui.genreTitle, fontSize = 12.sp, color = Cv.Muted)
                }
                Text(
                    "${ui.number}/${ui.total}",
                    fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.End,
                    modifier = Modifier.width(48.dp),
                )
            }
            val doneCount = ui.number - if (ui.checked) 0 else 1
            Box(Modifier.padding(start = 10.dp)) {
                ProgressBar(doneCount.toFloat() / ui.total, height = 8.dp, track = Cv.Card)
            }
        }

        val scroll = rememberScrollState()
        val feedback = remember { BringIntoViewRequester() }
        LaunchedEffect(q.id, ui.checked) {
            if (ui.checked) {
                delay(150)
                feedback.bringIntoView()
            } else {
                scroll.scrollTo(0)
            }
        }
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(scroll)
                .padding(horizontal = 22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Stage(ui, onListen)
            Text(instruction(q), fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 6.dp))
            q.options.forEachIndexed { i, option ->
                OptionRow(option, i, ui, onSelect)
            }
            if (ui.checked) {
                Box(Modifier.bringIntoViewRequester(feedback)) { Feedback(ui) }
            }
            Spacer(Modifier.height(8.dp))
        }

        Row(
            Modifier.padding(start = 22.dp, end = 22.dp, top = 12.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LineButton(
                "Indice",
                onClick = onHint,
                enabled = !ui.checked && ui.eliminated.isEmpty(),
                modifier = Modifier.height(56.dp),
            )
            GoldButton(
                when {
                    !ui.checked -> "Action !"
                    ui.number == ui.total -> "Coupez !"
                    else -> "Suivant"
                },
                onClick = onAction,
                enabled = ui.checked || ui.selected != null,
                modifier = Modifier.weight(1f).height(56.dp),
            )
        }
    }
}

private fun instruction(q: Question): String = when (q.game) {
    Game.DOUBLAGE -> "Double ${q.speaker ?: "la réplique"} en français :"
    Game.SCRIPT -> "Complète le script :"
    Game.CASTING -> "Quelle réplique as-tu entendue ?"
    Game.CLAP -> "Vite ! Que veut dire ce mot ?"
}

@Composable
private fun Stage(ui: QuestionUi, onListen: (Boolean) -> Unit) {
    val q = ui.q
    val shape = RoundedCornerShape(18.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Cv.Screen)
            .border(2.dp, Cv.Line, shape)
            .padding(horizontal = 18.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        val mine = Studio.isMine(q)
        when {
            q.retake && mine -> Pill("PRISE 2 · ta réplique")
            q.retake -> Pill("PRISE 2 · on la retente")
            mine -> Pill("TA RÉPLIQUE")
        }
        when (q.game) {
            Game.DOUBLAGE, Game.SCRIPT -> {
                q.speaker?.let { name ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SpeakerAvatar(name)
                        Text(name, fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    "« ${q.text} »",
                    fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White,
                    textAlign = TextAlign.Center, lineHeight = 30.sp,
                )
                q.subtitle?.let {
                    Text(it, fontSize = 15.sp, fontStyle = FontStyle.Italic, color = Cv.Muted, textAlign = TextAlign.Center)
                }
                if (q.game == Game.DOUBLAGE || ui.checked) ListenButtons(onListen)
            }
            Game.CASTING -> {
                Box(
                    Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(Cv.Header)
                        .border(3.dp, Cv.Gold, CircleShape)
                        .clickable { onListen(false) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Écouter la réplique", tint = Cv.Gold, modifier = Modifier.size(48.dp))
                }
                Text("Écoute bien la réplique…", color = Cv.Muted)
                ListenButtons(onListen)
            }
            Game.CLAP -> {
                val s = ui.secondsLeft
                if (s != null && !ui.checked) {
                    Text("$s s", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = if (s <= 3) Cv.Bad else Cv.Gold)
                }
                Text(q.text, fontFamily = Bebas, fontSize = 56.sp, letterSpacing = 2.sp, color = Cv.Gold, textAlign = TextAlign.Center)
                LineButton("Écouter", onClick = { onListen(false) }, color = Cv.Gold, border = Cv.Gold)
            }
        }
    }
}

@Composable
private fun ListenButtons(onListen: (Boolean) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        GoldButton("Écouter", onClick = { onListen(false) }, fontSize = 18)
        LineButton("Lentement", onClick = { onListen(true) }, color = Cv.Gold, border = Cv.Gold, modifier = Modifier.height(56.dp))
    }
}

@Composable
private fun OptionRow(text: String, i: Int, ui: QuestionUi, onSelect: (Int) -> Unit) {
    val q = ui.q
    val shape = RoundedCornerShape(14.dp)
    val isSelected = ui.selected == i
    val isAnswer = i == q.answer
    val out = i in ui.eliminated
    val border = when {
        ui.checked && isAnswer -> Cv.Good
        ui.checked && isSelected -> Cv.Bad
        isSelected -> Cv.Gold
        else -> Cv.Line
    }
    val bg = when {
        ui.checked && isAnswer -> Cv.GoodBg
        ui.checked && isSelected -> Cv.BadBg
        isSelected -> Cv.Selected
        else -> Cv.Locked
    }
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .alpha(if (out) 0.35f else 1f)
            .clip(shape)
            .background(bg)
            .border(if (isSelected || (ui.checked && isAnswer)) 3.dp else 2.dp, border, shape)
            .clickable(enabled = !ui.checked && !out) { onSelect(i) }
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f),
        )
        if (ui.checked && isAnswer) Icon(Icons.Filled.Check, contentDescription = "Bonne réponse", tint = Cv.Good)
    }
}

@Composable
private fun Feedback(ui: QuestionUi) {
    val q = ui.q
    val ok = ui.selected == q.answer
    val praises = if (ui.feminine) PRAISES_F else PRAISES_M
    val title = when {
        ok -> praises[Math.floorMod(q.id.hashCode(), praises.size)]
        ui.selected == null -> "Temps écoulé !"
        else -> "Presque !"
    }
    val wrongNote = ui.selected?.let { q.optionNotes.getOrNull(it) }
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (ok) Cv.GoodBg else Cv.BadBg)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = if (ok) Cv.Good else Cv.Bad)
        if (!ok) {
            FeedbackPart("La bonne réponse", q.options[q.answer])
            if (wrongNote != null) FeedbackPart("Ton choix", wrongNote)
        }
        q.why?.let { FeedbackPart(if (ok) "Pourquoi c'est juste" else "Pourquoi", it) }
        FeedbackPart("Traduction", q.translation)
        q.gloss?.let { FeedbackPart("Les mots", it.replace(" · ", "\n")) }
        if (!ok) Text("Pas de souci : cette réplique reviendra dans une prochaine scène.", fontSize = 13.sp, color = Cv.Muted)
    }
}

@Composable
private fun FeedbackPart(label: String, text: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Cv.Gold)
        Text(text, fontSize = 15.sp, lineHeight = 21.sp)
    }
}
