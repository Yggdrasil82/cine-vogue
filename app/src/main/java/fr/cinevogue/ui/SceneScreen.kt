package fr.cinevogue.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.withStyle
import fr.cinevogue.data.Mode
import fr.cinevogue.data.TextResult
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
    /** Avant-première : pas d'indice, pas de correction avant la fin. */
    val exam: Boolean = false,
    /** Réponse écrite (sous-titrage, rushes). */
    val typed: String = "",
    /** Étiquettes choisies, dans l'ordre (indices dans q.tiles). */
    val chosen: List<Int> = emptyList(),
    /** Correction d'une réponse écrite ou d'un montage. */
    val result: TextResult? = null,
    /** L'aide « mots en désordre » est proposée (niveau débutant). */
    val helpAllowed: Boolean = false,
    /** L'aide « mots en désordre » est activée pour cette question. */
    val helpOn: Boolean = false,
    /** Une clé d'IA est enregistrée : on peut faire vérifier une autre traduction. */
    val aiAvailable: Boolean = false,
    val aiChecking: Boolean = false,
    val aiNote: String? = null,
    val aiAccepted: Boolean = false,
    /** Rushes : un mot raté revient tout de suite. */
    val requeue: Boolean = false,
) {
    val tilesMode: Boolean get() = q.mode == Mode.TILES || (q.mode == Mode.TYPE && helpOn)
    val answerText: String get() = if (tilesMode) chosen.mapNotNull { q.tiles.getOrNull(it) }.joinToString(" ") else typed
    val correct: Boolean get() = if (q.isText) (result?.ok == true || aiAccepted) else selected == q.answer
    val ready: Boolean get() = if (q.isText) answerText.isNotBlank() else selected != null
}

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
    onRepeat: (String) -> Unit = {},
    onType: (String) -> Unit = {},
    onTile: (Int) -> Unit = {},
    onUntile: (Int) -> Unit = {},
    onHelp: () -> Unit = {},
    onAiCheck: () -> Unit = {},
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
                    Text(if (q.game == Game.RUSHES) ui.header else "${ui.header} · ${q.game.label}", fontFamily = Bebas, fontSize = 22.sp, letterSpacing = 2.sp, color = Cv.Gold)
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
            if (q.isText) {
                TextAnswer(ui, onType, onTile, onUntile, onHelp)
            } else {
                q.options.forEachIndexed { i, option ->
                    OptionRow(option, i, ui, onSelect)
                }
            }
            if (ui.checked) {
                Box(Modifier.bringIntoViewRequester(feedback)) { Feedback(ui, onRepeat, onAiCheck) }
            }
            Spacer(Modifier.height(8.dp))
        }

        Row(
            Modifier.padding(start = 22.dp, end = 22.dp, top = 12.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!ui.exam) {
                LineButton(
                    "Indice",
                    onClick = onHint,
                    enabled = !ui.checked && ui.eliminated.isEmpty(),
                    modifier = Modifier.height(56.dp),
                )
            }
            GoldButton(
                when {
                    ui.exam && ui.number == ui.total -> "Terminer"
                    ui.exam -> "Valider"
                    !ui.checked -> "Action !"
                    ui.number == ui.total -> "Coupez !"
                    else -> "Suivant"
                },
                onClick = onAction,
                enabled = ui.checked || ui.ready,
                modifier = Modifier.weight(1f).height(56.dp),
            )
        }
    }
}

private fun instruction(q: Question): String = q.instruction ?: when (q.game) {
    Game.DOUBLAGE -> "Double ${q.speaker ?: "la réplique"} en français :"
    Game.SCRIPT -> "Complète le script :"
    Game.CASTING -> "Quelle réplique as-tu entendue ?"
    Game.CLAP -> "Vite ! Que veut dire ce mot ?"
    Game.SOUSTITRAGE -> "Écris cette réplique en anglais :"
    Game.MONTAGE -> "Remets les mots dans l'ordre :"
    Game.RUSHES -> "Révise les mots de la scène :"
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
            Game.SOUSTITRAGE, Game.MONTAGE -> {
                q.speaker?.let { name ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SpeakerAvatar(name)
                        Text(name, fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    "« ${q.text} »",
                    fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Color.White,
                    textAlign = TextAlign.Center, lineHeight = 29.sp,
                )
                Text(if (q.game == Game.MONTAGE) "Le monteur a mélangé la réplique anglaise." else "Version française · à sous-titrer en anglais", fontSize = 13.sp, fontStyle = FontStyle.Italic, color = Cv.Muted, textAlign = TextAlign.Center)
                if (ui.checked) ListenButtons(onListen)
            }
            Game.RUSHES -> {
                if (q.kind == "listen") {
                    Box(
                        Modifier
                            .size(84.dp)
                            .clip(CircleShape)
                            .background(Cv.Header)
                            .border(3.dp, Cv.Gold, CircleShape)
                            .clickable { onListen(false) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = "Écouter le mot", tint = Cv.Gold, modifier = Modifier.size(48.dp))
                    }
                    if (ui.checked) Text(q.audio.orEmpty(), fontFamily = Bebas, fontSize = 40.sp, letterSpacing = 2.sp, color = Cv.Gold, textAlign = TextAlign.Center)
                } else {
                    Text(q.text, fontFamily = Bebas, fontSize = if (q.text.length > 16) 36.sp else 48.sp, letterSpacing = 2.sp, color = Cv.Gold, textAlign = TextAlign.Center, lineHeight = 44.sp)
                    if (q.kind == "enfr" || ui.checked) LineButton("Écouter", onClick = { onListen(false) }, color = Cv.Gold, border = Cv.Gold)
                }
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
private fun Feedback(ui: QuestionUi, onRepeat: (String) -> Unit, onAiCheck: () -> Unit) {
    val q = ui.q
    val ok = ui.correct
    val praises = if (ui.feminine) PRAISES_F else PRAISES_M
    val typos = ui.result?.typos.orEmpty()
    val title = when {
        ui.aiAccepted -> "Ta version est juste aussi !"
        ok && typos.isNotEmpty() -> "Juste, à une lettre près !"
        ok -> praises[Math.floorMod(q.id.hashCode(), praises.size)]
        !q.isText && ui.selected == null -> "Temps écoulé !"
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
        if (q.isText) {
            if (typos.isNotEmpty()) {
                FeedbackPart("Attention à l'orthographe", typos.joinToString("\n") { (t, e) -> "Tu as écrit « $t » : on écrit « $e »." })
            }
            if (!ok || ui.aiAccepted) {
                FeedbackPart("Ta réponse", ui.answerText.ifBlank { "pas de réponse" })
                val words = ui.result?.words.orEmpty()
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text((if (ui.aiAccepted) "La version du script" else "La bonne réponse").uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Cv.Gold)
                    Text(
                        buildAnnotatedString {
                            if (words.isEmpty()) append(q.expected.orEmpty())
                            words.forEachIndexed { i, w ->
                                if (i > 0) append(" ")
                                withStyle(SpanStyle(color = if (w.ok || ui.aiAccepted) Cv.Cream else Cv.Bad, fontWeight = if (w.ok) FontWeight.Normal else FontWeight.Bold)) { append(w.word) }
                            }
                        },
                        fontSize = 16.sp, lineHeight = 22.sp,
                    )
                    if (!ok && words.any { !it.ok }) Text("En rouge : les mots qui manquent ou qui sont différents.", fontSize = 12.sp, color = Cv.Muted)
                }
            }
            ui.aiNote?.let { FeedbackPart("L'avis de l'IA", it) }
            if (!ok && q.game == Game.SOUSTITRAGE && !ui.helpOn && ui.aiAvailable && ui.aiNote == null) {
                LineButton(
                    if (ui.aiChecking) "L'IA vérifie ta traduction…" else "Ma traduction est peut-être juste aussi : demander à l'IA",
                    onClick = onAiCheck, enabled = !ui.aiChecking, color = Cv.Gold, border = Cv.Gold, modifier = Modifier.fillMaxWidth(),
                )
            }
        } else if (!ok) {
            FeedbackPart("La bonne réponse", q.options[q.answer])
            if (wrongNote != null) FeedbackPart("Ton choix", wrongNote)
        }
        q.why?.let { FeedbackPart(if (ok) "Pourquoi c'est juste" else "Pourquoi", it) }
        FeedbackPart("Traduction", q.translation)
        q.gloss?.let { FeedbackPart("Les mots", it.replace(" · ", "\n")) }
        if (!ok) {
            Text(
                if (ui.requeue) "Pas de souci : ce mot revient dans un instant." else "Pas de souci : cette réplique reviendra dans une prochaine scène.",
                fontSize = 13.sp, color = Cv.Muted,
            )
        }
        q.audio?.let { sentence ->
            LineButton("Répète après moi", onClick = { onRepeat(sentence) }, color = Cv.Gold, border = Cv.Gold, modifier = Modifier.fillMaxWidth())
        }
    }
}

/** Réponse écrite au clavier, ou avec des étiquettes à remettre dans l'ordre. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TextAnswer(ui: QuestionUi, onType: (String) -> Unit, onTile: (Int) -> Unit, onUntile: (Int) -> Unit, onHelp: () -> Unit) {
    val q = ui.q
    if (ui.tilesMode) {
        val shape = RoundedCornerShape(14.dp)
        val border = when {
            ui.checked && ui.correct -> Cv.Good
            ui.checked -> Cv.Bad
            else -> Cv.Gold
        }
        Box(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .clip(shape)
                .background(Cv.Locked)
                .border(2.dp, border, shape)
                .padding(10.dp),
        ) {
            if (ui.chosen.isEmpty()) {
                Text("Touche les mots dans l'ordre…", color = Cv.Dim, fontSize = 15.sp, modifier = Modifier.padding(6.dp))
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ui.chosen.forEachIndexed { pos, i ->
                    Tile(q.tiles.getOrNull(i).orEmpty(), selected = true, enabled = !ui.checked) { onUntile(pos) }
                }
            }
        }
        if (!ui.checked) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                q.tiles.forEachIndexed { i, word ->
                    if (i !in ui.chosen) Tile(word, selected = false, enabled = true) { onTile(i) }
                }
            }
            if (q.mode == Mode.TYPE) Text("Attention : deux mots en trop se sont glissés dans la liste.", fontSize = 12.sp, color = Cv.Muted)
        }
    } else {
        OutlinedTextField(
            value = ui.typed,
            onValueChange = onType,
            enabled = !ui.checked,
            label = { Text(if (q.game == Game.RUSHES) "Le mot en anglais" else "Ta réplique en anglais") },
            keyboardOptions = KeyboardOptions(
                capitalization = if (q.game == Game.RUSHES) KeyboardCapitalization.None else KeyboardCapitalization.Sentences,
                autoCorrect = false,
                imeAction = ImeAction.Done,
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Cv.Gold,
                unfocusedBorderColor = Cv.Line,
                disabledBorderColor = if (ui.correct) Cv.Good else Cv.Bad,
                focusedLabelColor = Cv.Gold,
                unfocusedLabelColor = Cv.Muted,
                disabledLabelColor = Cv.Muted,
                cursorColor = Cv.Gold,
                focusedTextColor = Cv.Cream,
                unfocusedTextColor = Cv.Cream,
                disabledTextColor = Cv.Cream,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        if (ui.helpAllowed && !ui.exam && !ui.checked && q.game == Game.SOUSTITRAGE && q.tiles.isNotEmpty()) {
            LineButton("Aide : mots en désordre", onClick = onHelp, color = Cv.Gold, border = Cv.Gold, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun Tile(word: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        Modifier
            .clip(shape)
            .background(if (selected) Cv.Selected else Cv.Card)
            .border(2.dp, if (selected) Cv.Gold else Cv.Line, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(word, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FeedbackPart(label: String, text: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Cv.Gold)
        Text(text, fontSize = 15.sp, lineHeight = 21.sp)
    }
}
