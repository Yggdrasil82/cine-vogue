package fr.cinevogue.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.cinevogue.ai.AiReview
import fr.cinevogue.ai.ChatLine
import fr.cinevogue.data.AiScenario
import fr.cinevogue.data.EVAL_OSCAR
import fr.cinevogue.data.Look
import kotlinx.coroutines.delay

/** Le personnage dessiné joué par l'IA : il cligne des yeux, parle et change d'expression. */
@Composable
fun AnimatedCharacter(look: Look, mood: String, speaking: Boolean, size: Dp, animate: Boolean = true) {
    var blink by remember { mutableStateOf(false) }
    if (animate) {
        LaunchedEffect(Unit) {
            while (true) {
                delay(2600L + (0..1800).random())
                blink = true
                delay(140)
                blink = false
            }
        }
    }
    val talk = rememberInfiniteTransition(label = "talk")
    val mouth by talk.animateFloat(
        initialValue = 0.25f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(170), RepeatMode.Reverse), label = "mouth",
    )
    val bob by talk.animateFloat(
        initialValue = -1f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse), label = "bob",
    )
    Canvas(Modifier.size(size)) {
        drawCharacter(look, mood, if (speaking && animate) mouth else -1f, blink, if (animate) bob else 0f)
    }
}

private fun DrawScope.drawCharacter(look: Look, mood: String, mouthOpen: Float, blink: Boolean, bob: Float) {
    val s = size.minDimension
    val skin = Color(look.skin)
    val hair = Color(look.hair)
    val shirt = Color(look.shirt)
    val ink = Color(0xFF1A0A0D)
    val dy = bob * s * 0.008f

    // Fond : un projecteur.
    drawCircle(Color(0xFF3A0D14), radius = s / 2f, center = Offset(s / 2f, s / 2f))
    drawCircle(Color(0x22E2B546), radius = s * 0.36f, center = Offset(s / 2f, s * 0.4f))

    // Épaules et cou.
    drawRoundRect(shirt, topLeft = Offset(s * 0.2f, s * 0.74f + dy), size = Size(s * 0.6f, s * 0.4f), cornerRadius = CornerRadius(s * 0.18f))
    drawRect(skin, topLeft = Offset(s * 0.44f, s * 0.6f + dy), size = Size(s * 0.12f, s * 0.16f))

    val cx = s / 2f
    val cy = s * 0.44f + dy
    val headR = s * 0.22f

    if (look.longHair && !look.robot) {
        drawRoundRect(hair, topLeft = Offset(cx - headR * 1.12f, cy - headR * 0.9f), size = Size(headR * 2.24f, headR * 2.3f), cornerRadius = CornerRadius(headR))
    }
    // Tête.
    drawOval(skin, topLeft = Offset(cx - headR, cy - headR * 1.1f), size = Size(headR * 2f, headR * 2.2f))

    if (look.robot) {
        drawLine(hair, Offset(cx, cy - headR * 1.1f), Offset(cx, cy - headR * 1.45f), strokeWidth = s * 0.015f)
        drawCircle(hair, radius = s * 0.025f, center = Offset(cx, cy - headR * 1.5f))
        drawRoundRect(Color(0xFF2B3A55), topLeft = Offset(cx - headR * 0.8f, cy - headR * 0.45f), size = Size(headR * 1.6f, headR * 0.6f), cornerRadius = CornerRadius(headR * 0.3f))
    } else {
        // Cheveux sur le dessus.
        drawArc(hair, startAngle = 180f, sweepAngle = 180f, useCenter = true,
            topLeft = Offset(cx - headR * 1.05f, cy - headR * 1.25f), size = Size(headR * 2.1f, headR * 1.5f))
        if (look.cap) {
            drawArc(Color(look.shirt).copy(alpha = 1f), startAngle = 180f, sweepAngle = 180f, useCenter = true,
                topLeft = Offset(cx - headR * 1.08f, cy - headR * 1.32f), size = Size(headR * 2.16f, headR * 1.3f))
            drawRoundRect(Color(look.shirt), topLeft = Offset(cx - headR * 0.2f, cy - headR * 0.72f), size = Size(headR * 1.5f, headR * 0.18f), cornerRadius = CornerRadius(headR * 0.1f))
        }
    }

    // Yeux.
    val eyeY = cy - headR * 0.15f
    val eyeDx = headR * 0.42f
    for (side in listOf(-1f, 1f)) {
        val ex = cx + side * eyeDx
        if (look.robot) {
            val h = if (blink) headR * 0.04f else headR * 0.22f
            drawRoundRect(Color(0xFF6FD3E8), topLeft = Offset(ex - headR * 0.16f, eyeY - h / 2f), size = Size(headR * 0.32f, h), cornerRadius = CornerRadius(headR * 0.08f))
        } else if (blink) {
            drawLine(ink, Offset(ex - headR * 0.14f, eyeY), Offset(ex + headR * 0.14f, eyeY), strokeWidth = s * 0.012f)
        } else {
            val open = if (mood == "surprised") 1.3f else 1f
            drawOval(Color.White, topLeft = Offset(ex - headR * 0.16f, eyeY - headR * 0.13f * open), size = Size(headR * 0.32f, headR * 0.26f * open))
            drawCircle(ink, radius = headR * 0.085f, center = Offset(ex, eyeY + headR * 0.01f))
            drawCircle(Color.White, radius = headR * 0.03f, center = Offset(ex + headR * 0.03f, eyeY - headR * 0.03f))
        }
        // Sourcils selon l'humeur.
        if (!look.robot) {
            val raise = when (mood) {
                "surprised" -> headR * 0.12f
                "thinking" -> if (side > 0) headR * 0.1f else -headR * 0.02f
                else -> 0f
            }
            val tilt = if (mood == "happy") headR * 0.03f else 0f
            drawLine(hair, Offset(ex - headR * 0.17f, eyeY - headR * 0.27f - raise + tilt), Offset(ex + headR * 0.17f, eyeY - headR * 0.3f - raise), strokeWidth = s * 0.016f)
        }
    }
    if (look.glasses) {
        for (side in listOf(-1f, 1f)) {
            drawCircle(ink, radius = headR * 0.24f, center = Offset(cx + side * eyeDx, eyeY), style = Stroke(width = s * 0.01f))
        }
        drawLine(ink, Offset(cx - eyeDx + headR * 0.24f, eyeY), Offset(cx + eyeDx - headR * 0.24f, eyeY), strokeWidth = s * 0.01f)
    }

    // Joues.
    if (!look.robot) {
        drawCircle(Color(0x33E8735A), radius = headR * 0.14f, center = Offset(cx - headR * 0.58f, cy + headR * 0.28f))
        drawCircle(Color(0x33E8735A), radius = headR * 0.14f, center = Offset(cx + headR * 0.58f, cy + headR * 0.28f))
    }

    // Bouche.
    val my = cy + headR * 0.52f
    val mouthColor = if (look.robot) Color(0xFF6FD3E8) else Color(0xFF7A0E1C)
    when {
        mouthOpen >= 0f -> {
            val h = headR * 0.32f * mouthOpen + headR * 0.06f
            drawOval(mouthColor, topLeft = Offset(cx - headR * 0.24f, my - h / 2f), size = Size(headR * 0.48f, h))
        }
        mood == "surprised" -> drawOval(mouthColor, topLeft = Offset(cx - headR * 0.1f, my - headR * 0.12f), size = Size(headR * 0.2f, headR * 0.24f))
        mood == "thinking" -> drawLine(mouthColor, Offset(cx - headR * 0.18f, my + headR * 0.02f), Offset(cx + headR * 0.2f, my - headR * 0.04f), strokeWidth = s * 0.016f)
        else -> {
            val depth = if (mood == "happy") 0.36f else 0.2f
            drawArc(mouthColor, startAngle = 15f, sweepAngle = 150f, useCenter = mood == "happy",
                topLeft = Offset(cx - headR * 0.3f, my - headR * depth), size = Size(headR * 0.6f, headR * depth * 1.6f),
                style = if (mood == "happy") androidx.compose.ui.graphics.drawscope.Fill else Stroke(width = s * 0.016f))
        }
    }
}

/** Écran de présentation de la scène IA. */
@Composable
fun AiIntroScreen(s: AiScenario, ready: Boolean, best: Int?, onStart: () -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Cv.Bg)) {
        Row(Modifier.fillMaxWidth().background(Cv.Header).padding(start = 8.dp, end = 22.dp, top = 12.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.Close, contentDescription = "Retour", tint = Cv.Cream) }
            Text("SCÈNE IMPROVISÉE", fontFamily = Bebas, fontSize = 30.sp, letterSpacing = 2.sp, color = Cv.Gold)
        }
        Box(Modifier.fillMaxWidth().height(3.dp).background(Cv.Gold))
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AnimatedCharacter(s.character.look, "happy", speaking = false, size = 170.dp)
            Text(s.title, fontFamily = Bebas, fontSize = 32.sp, letterSpacing = 1.5.sp, color = Cv.Gold, textAlign = TextAlign.Center)
            Text("Avec ${s.character.roleFr}", fontSize = 14.sp, color = Cv.Muted)
            Text(s.setupFr, fontSize = 16.sp, lineHeight = 22.sp, textAlign = TextAlign.Center)
            Card {
                Text("TA MISSION", fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Cv.Gold)
                s.objectivesFr.forEachIndexed { i, o -> Text("${i + 1}. $o", fontSize = 15.sp) }
            }
            Card {
                Text("Comment ça marche", fontWeight = FontWeight.Bold)
                Text("Écoute le personnage, puis appuie sur le micro et réponds en anglais, à voix haute. Après chaque réponse, tu vois comment mieux le dire. Bouton « Aide » si tu bloques.", fontSize = 13.sp, color = Cv.Muted, lineHeight = 19.sp)
                Text("Ce personnage est joué par une intelligence artificielle (Mistral). Ne donne jamais d'informations personnelles (nom de famille, adresse, lycée, téléphone).", fontSize = 13.sp, color = Cv.Gold, lineHeight = 19.sp)
            }
            best?.let { Text("Meilleure note : $it/20", fontWeight = FontWeight.Bold, color = Cv.Gold) }
            if (!ready) {
                Text("L'IA n'est pas encore configurée : un parent doit saisir la clé Mistral dans Profil › Réglages parent.", color = Cv.Bad, textAlign = TextAlign.Center, fontSize = 14.sp)
            }
        }
        Box(Modifier.padding(start = 22.dp, end = 22.dp, top = 12.dp, bottom = 20.dp)) {
            GoldButton("Action !", onClick = onStart, enabled = ready, modifier = Modifier.fillMaxWidth().height(58.dp), fontSize = 26)
        }
    }
}

data class AiUi(
    val scenario: AiScenario,
    val lines: List<ChatLine>,
    val objectivesDone: Set<Int>,
    val loading: Boolean,
    val error: String?,
    val hint: String?,
    val mood: String,
    val speaking: Boolean,
    val turnsLeft: Int,
    /** Phrase comprise par le téléphone, en attente de validation par l'élève. */
    val pending: String? = null,
)

/** Le dialogue avec le personnage. */
@Composable
fun AiDialogueScreen(
    ui: AiUi,
    animate: Boolean,
    onMic: () -> Unit,
    onHint: () -> Unit,
    onReplay: (String) -> Unit,
    onRetry: () -> Unit,
    onFinish: () -> Unit,
    onQuit: () -> Unit,
    onSend: (text: String, edited: Boolean) -> Unit = { _, _ -> },
    onRedo: () -> Unit = {},
    onPractice: (String) -> Unit = {},
) {
    val s = ui.scenario
    val scroll = rememberScrollState()
    LaunchedEffect(ui.lines.size, ui.loading, ui.hint, ui.error) {
        delay(80)
        scroll.animateScrollTo(scroll.maxValue)
    }
    Column(Modifier.fillMaxSize().background(Cv.Bg)) {
        Row(Modifier.fillMaxWidth().padding(start = 8.dp, end = 16.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onQuit) { Icon(Icons.Filled.Close, contentDescription = "Quitter la scène", tint = Cv.Cream) }
            Column(Modifier.weight(1f)) {
                Text(s.title, fontFamily = Bebas, fontSize = 22.sp, letterSpacing = 1.5.sp, color = Cv.Gold)
                Text("${s.character.name} · personnage joué par une IA", fontSize = 12.sp, color = Cv.Muted)
            }
            Pill("${ui.turnsLeft} répliques", background = Cv.Card, color = Cv.Gold)
        }
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            AnimatedCharacter(s.character.look, ui.mood, ui.speaking, size = 150.dp, animate = animate)
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            s.objectivesFr.forEachIndexed { i, o ->
                val done = i in ui.objectivesDone
                Row(
                    Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(if (done) Cv.GoodBg else Cv.Card).padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (done) Icon(Icons.Filled.Check, contentDescription = "Réussi", tint = Cv.Good, modifier = Modifier.size(14.dp))
                    Text(o, fontSize = 10.sp, lineHeight = 12.sp, color = if (done) Cv.Good else Cv.Muted, maxLines = 3)
                }
            }
        }
        Column(
            Modifier.weight(1f).verticalScroll(scroll).padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ui.lines.forEachIndexed { i, line ->
                if (line.fromLearner) {
                    val reply = ui.lines.getOrNull(i + 1)?.turn
                    LearnerBubble(line.text, reply?.better.orEmpty(), reply?.correctionFr.orEmpty(), line.heard) {
                        onPractice(reply?.better?.ifBlank { null } ?: line.text)
                    }
                } else {
                    CharacterBubble(s.character.name, line.text) { onReplay(line.text) }
                }
            }
            if (ui.loading) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircularProgressIndicator(color = Cv.Gold, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    Text("${s.character.name} réfléchit…", fontSize = 13.sp, color = Cv.Muted, fontStyle = FontStyle.Italic)
                }
            }
            ui.error?.let {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Cv.BadBg).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(it, color = Cv.Bad, fontSize = 14.sp)
                    LineButton("Réessayer", onClick = onRetry, color = Cv.Bad, border = Cv.Bad)
                }
            }
            ui.hint?.let {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Cv.Cream).padding(12.dp)) {
                    Text("AIDE", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Cv.Header)
                    Text(it, fontSize = 14.sp, color = Cv.Bg, lineHeight = 20.sp)
                }
            }
        }
        ui.pending?.let { heard -> PendingPanel(heard, onSend, onRedo) }
        if (ui.pending == null) Row(
            Modifier.fillMaxWidth().background(Cv.Nav).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            LineButton("Aide", onClick = onHint, enabled = !ui.loading)
            Box(
                Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(if (ui.loading || ui.turnsLeft <= 0) Cv.Track else Cv.Gold)
                    .clickable(enabled = !ui.loading && ui.turnsLeft > 0, onClickLabel = "Parler", onClick = onMic),
                contentAlignment = Alignment.Center,
            ) {
                MicIcon(if (ui.loading) Cv.Dim else Cv.Bg)
            }
            LineButton("Terminer", onClick = onFinish, enabled = !ui.loading && ui.lines.any { it.fromLearner }, color = Cv.Gold, border = Cv.Gold)
        }
    }
}

@Composable
private fun MicIcon(color: Color) {
    Canvas(Modifier.size(30.dp)) {
        val w = size.width
        drawRoundRect(color, topLeft = Offset(w * 0.34f, w * 0.05f), size = Size(w * 0.32f, w * 0.55f), cornerRadius = CornerRadius(w * 0.16f))
        drawArc(color, startAngle = 0f, sweepAngle = 180f, useCenter = false, topLeft = Offset(w * 0.2f, w * 0.25f), size = Size(w * 0.6f, w * 0.5f), style = Stroke(width = w * 0.08f))
        drawLine(color, Offset(w * 0.5f, w * 0.75f), Offset(w * 0.5f, w * 0.92f), strokeWidth = w * 0.08f)
        drawLine(color, Offset(w * 0.35f, w * 0.93f), Offset(w * 0.65f, w * 0.93f), strokeWidth = w * 0.08f)
    }
}

@Composable
private fun CharacterBubble(name: String, text: String, onReplay: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Row(
            Modifier.widthIn(max = 300.dp).clip(RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp))
                .background(Cv.Card).padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f, fill = false)) {
                Text(name.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Cv.Gold)
                Text(text, fontSize = 15.sp, lineHeight = 21.sp)
            }
            IconButton(onClick = onReplay) { Icon(Icons.Filled.PlayArrow, contentDescription = "Réécouter", tint = Cv.Gold) }
        }
    }
}

/** La phrase comprise par le téléphone : on l'envoie, on la redit ou on la corrige. */
@Composable
private fun PendingPanel(heard: String, onSend: (String, Boolean) -> Unit, onRedo: () -> Unit) {
    var editing by remember(heard) { mutableStateOf(false) }
    var text by remember(heard) { mutableStateOf(heard) }
    Column(
        Modifier.fillMaxWidth().background(Cv.Nav).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("LE TÉLÉPHONE A COMPRIS :", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Cv.Gold)
        if (editing) {
            androidx.compose.material3.OutlinedTextField(
                value = text,
                onValueChange = { if (it.length <= 300) text = it },
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Cv.Gold, unfocusedBorderColor = Cv.Track, cursorColor = Cv.Gold,
                    focusedTextColor = Cv.Cream, unfocusedTextColor = Cv.Cream,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Text("« $heard »", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text("Si un mot est faux, c'est peut-être un mot à mieux prononcer : redis la phrase, ou corrige-la.", fontSize = 12.sp, color = Cv.Muted)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LineButton("Redire", onClick = onRedo, modifier = Modifier.weight(1f))
            if (!editing) LineButton("Corriger", onClick = { editing = true }, modifier = Modifier.weight(1f))
            GoldButton("Envoyer", onClick = { onSend(text.trim(), text.trim() != heard) }, enabled = text.isNotBlank(), fontSize = 18, modifier = Modifier.weight(1.2f))
        }
    }
}

@Composable
private fun LearnerBubble(text: String, better: String, correction: String, heard: String?, onPractice: () -> Unit) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Column(
            Modifier.widthIn(max = 290.dp).clip(RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomEnd = 16.dp, bottomStart = 16.dp))
                .background(Cv.Selected).border(1.dp, Cv.Gold, RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomEnd = 16.dp, bottomStart = 16.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text("TOI", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Cv.Gold)
            Text(text, fontSize = 15.sp, lineHeight = 21.sp)
            if (heard != null) Text("Le téléphone avait compris : « $heard »", fontSize = 12.sp, color = Cv.Muted, fontStyle = FontStyle.Italic)
        }
        if (better.isNotBlank() || correction.isNotBlank()) {
            Column(
                Modifier.widthIn(max = 290.dp).clip(RoundedCornerShape(12.dp)).background(Cv.GoodBg).padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                if (better.isNotBlank()) Text("Mieux : $better", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Cv.Good)
                if (correction.isNotBlank()) Text(correction, fontSize = 13.sp, color = Cv.Muted, lineHeight = 18.sp)
            }
        }
        if (heard != null || better.isNotBlank()) {
            Text(
                "Travailler la prononciation",
                fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Cv.Gold,
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onPractice).padding(horizontal = 6.dp, vertical = 4.dp),
            )
        }
    }
}

/** Bilan de la scène IA. */
@Composable
fun AiResultScreen(s: AiScenario, review: AiReview, objectivesDone: Int, best: Int, onRetry: () -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Cv.Bg)) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Column(
                Modifier.fillMaxWidth().background(Cv.Header).padding(horizontal = 22.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AnimatedCharacter(s.character.look, if (review.score >= 10) "happy" else "thinking", speaking = false, size = 110.dp, animate = false)
                Text("COUPEZ ! · ${s.title.uppercase()}", fontFamily = Bebas, fontSize = 22.sp, letterSpacing = 2.sp, color = Cv.Muted, textAlign = TextAlign.Center)
                Text("${review.score}/20", fontFamily = Bebas, fontSize = 68.sp, color = if (review.score >= 10) Cv.Gold else Cv.Bad)
                Text("Mission : $objectivesDone objectif${if (objectivesDone > 1) "s" else ""} sur ${s.objectivesFr.size} · meilleure note : $best/20", fontSize = 13.sp, color = Cv.Muted)
                if (review.score >= EVAL_OSCAR) Pill("Trophée Improvisation !")
            }
            Box(Modifier.fillMaxWidth().height(3.dp).background(Cv.Gold))
            Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (review.praiseFr.isNotBlank()) {
                    SectionTitle("Bravo pour", 24)
                    Text(review.praiseFr, fontSize = 15.sp, lineHeight = 21.sp)
                }
                if (review.improveFr.isNotEmpty()) {
                    SectionTitle("À travailler", 24)
                    review.improveFr.forEach { Text("•  $it", fontSize = 15.sp, lineHeight = 21.sp) }
                }
                if (review.pronunciationFr.isNotBlank()) {
                    SectionTitle("Prononciation", 24)
                    Text(review.pronunciationFr, fontSize = 15.sp, lineHeight = 21.sp)
                }
                if (review.tipFr.isNotBlank()) {
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Cv.Cream).padding(16.dp)) {
                        Text("L'EXPRESSION À RETENIR", fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Cv.Header)
                        Text(review.tipFr, fontSize = 15.sp, color = Cv.Bg, lineHeight = 21.sp)
                    }
                }
                Text("Bilan rédigé par l'IA : il peut parfois se tromper.", fontSize = 12.sp, color = Cv.Dim)
                Spacer(Modifier.height(4.dp))
            }
        }
        Column(Modifier.padding(start = 22.dp, end = 22.dp, top = 12.dp, bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            GoldButton("Rejouer la scène", onClick = onRetry, modifier = Modifier.fillMaxWidth().height(58.dp), fontSize = 22)
            LineButton("Retour au thème", onClick = onBack, modifier = Modifier.fillMaxWidth())
        }
    }
}
