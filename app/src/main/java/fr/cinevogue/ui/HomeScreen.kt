package fr.cinevogue.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.cinevogue.data.Genre
import fr.cinevogue.data.Profile
import fr.cinevogue.data.Progress
import fr.cinevogue.data.AiScenario
import fr.cinevogue.data.EVAL_OSCAR
import fr.cinevogue.data.evalPassed
import fr.cinevogue.data.scenarioFor
import fr.cinevogue.data.EVAL_PASS
import fr.cinevogue.data.currentStreak
import fr.cinevogue.data.genreDone
import fr.cinevogue.data.pendingEval
import fr.cinevogue.data.genreUnlocked
import fr.cinevogue.data.nextRank
import fr.cinevogue.data.nextToPlay
import fr.cinevogue.data.rankFor
import fr.cinevogue.data.sceneUnlocked
import fr.cinevogue.data.scenesDone

@Composable
fun HomeScreen(
    profile: Profile,
    genres: List<Genre>,
    films: List<Genre>,
    p: Progress,
    today: Long,
    onPlay: (String) -> Unit,
    onGenre: (String) -> Unit,
    onSwitch: () -> Unit,
    onEditFilms: () -> Unit,
    onEval: (String) -> Unit = {},
) {
    val rank = rankFor(p.points)
    val next = nextRank(p.points)
    val streak = currentStreak(p, today)
    val up = nextToPlay(genres, p)
    val pending = pendingEval(genres, p)
    val evalFirst = pending != null && genres.indexOf(pending) < (up?.let { genres.indexOf(it.first) } ?: Int.MAX_VALUE)

    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(Cv.Header)
                .padding(horizontal = 22.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "CINÉ VOGUE",
                    fontFamily = Bebas, fontSize = 40.sp, letterSpacing = 3.sp, color = Cv.Gold,
                    modifier = Modifier.weight(1f),
                )
                Pill(if (streak > 1) "Série : $streak jours" else "Série : $streak jour", background = Cv.Bg, color = Cv.Cream)
                Spacer(Modifier.width(10.dp))
                ProfileAvatar(
                    profile.name, profile.color, dp = 40.dp,
                    modifier = Modifier.clickable(onClickLabel = "Changer de profil", onClick = onSwitch),
                )
            }
            Row {
                Text("${profile.name}, ton rang : ", fontSize = 14.sp)
                Text(rank.title(profile.gender), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Cv.Gold)
                Spacer(Modifier.weight(1f))
                Text(if (next != null) "${p.points} / ${next.min} ★" else "${p.points} ★", fontSize = 14.sp)
            }
            val fraction = if (next == null) 1f else (p.points - rank.min).toFloat() / (next.min - rank.min)
            ProgressBar(fraction)
            Text(
                if (next != null) "Encore ${next.min - p.points} ★ pour devenir ${next.title(profile.gender)}"
                else "Tu es une star ! Continue à tourner.",
                fontSize = 13.sp, color = Cv.Muted,
            )
        }
        Box(Modifier.fillMaxWidth().height(3.dp).background(Cv.Gold))

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) { SectionTitle("Mes films") }
                Text(
                    "Modifier",
                    fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Cv.Gold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onEditFilms)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                )
            }
            if (films.isEmpty()) {
                Text(
                    "Aucun film choisi. Touche « Modifier » pour choisir tes films préférés.",
                    fontSize = 14.sp, color = Cv.Muted,
                )
            }
            films.chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    pair.forEach { f ->
                        FilmTile(f, scenesDone(f, p), Modifier.weight(1f)) { onGenre(f.id) }
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) { SectionTitle("À l'affiche") }
                Pill(profile.level.label.uppercase(), background = Cv.Card, color = Cv.Gold)
            }
            genres.forEachIndexed { i, g ->
                GenreCard(
                    genre = g,
                    unlocked = genreUnlocked(genres, i, p),
                    current = up?.first == g,
                    done = scenesDone(g, p),
                    lockHint = if (i > 0) "Réussis l'avant-première de « ${genres[i - 1].title} » ($EVAL_PASS/20)" else "",
                    evalScore = p.evalBest[g.id],
                    onClick = { onGenre(g.id) },
                )
            }
        }

        Box(Modifier.padding(horizontal = 22.dp, vertical = 14.dp)) {
            if (evalFirst && pending != null) {
                GoldButton(
                    "Avant-première : ${pending.title}",
                    onClick = { onEval(pending.id) },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    fontSize = 22,
                )
            } else if (up != null) {
                GoldButton(
                    "Tourner : ${up.second.title}",
                    onClick = { onPlay(up.second.id) },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                )
            } else {
                GoldButton(
                    "Rejouer une scène",
                    onClick = { onGenre(genres.first().id) },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                )
            }
        }
    }
}

@Composable
private fun FilmTile(film: Genre, done: Int, modifier: Modifier, onClick: () -> Unit) {
    val finished = done == film.scenes.size
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Cv.Card)
            .then(if (finished) Modifier.border(2.dp, Cv.Gold, RoundedCornerShape(14.dp)) else Modifier)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(film.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 19.sp, maxLines = 2, minLines = 2)
        Segments(done, film.scenes.size)
        Text(if (finished) "Terminé ★" else "$done / ${film.scenes.size} scènes", fontSize = 12.sp, color = if (finished) Cv.Gold else Cv.Muted)
    }
}

@Composable
private fun GenreCard(
    genre: Genre,
    unlocked: Boolean,
    current: Boolean,
    done: Int,
    lockHint: String,
    evalScore: Int?,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    var m = Modifier
        .fillMaxWidth()
        .clip(shape)
        .background(if (unlocked) Cv.Card else Cv.Locked)
    if (current) m = m.border(2.dp, Cv.Gold, shape)
    Column(
        m.clickable(enabled = unlocked, onClick = onClick).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                genre.title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = if (unlocked) Cv.Cream else Cv.Dim,
                modifier = Modifier.weight(1f),
            )
            when {
                !unlocked -> Icon(Icons.Filled.Lock, contentDescription = "Verrouillé", tint = Cv.Dim)
                (evalScore ?: 0) >= EVAL_OSCAR -> Pill("OSCAR ★")
                (evalScore ?: 0) >= EVAL_PASS -> Pill("VALIDÉ $evalScore/20", background = Cv.Track, color = Cv.Gold)
                done == genre.scenes.size -> Pill("AVANT-PREMIÈRE")
                current -> Pill("EN TOURNAGE")
            }
        }
        Text(genre.subtitle, fontSize = 14.sp, color = if (unlocked) Cv.Muted else Cv.Dim)
        if (unlocked) {
            Segments(done, genre.scenes.size)
            Text("$done scène${if (done > 1) "s" else ""} sur ${genre.scenes.size} terminée${if (done > 1) "s" else ""}", fontSize = 13.sp, color = Cv.Muted)
        } else {
            Text(lockHint, fontSize = 13.sp, color = Cv.Dim)
        }
    }
}

@Composable
fun GenreScreen(
    genre: Genre,
    p: Progress,
    onPlay: (String) -> Unit,
    onBack: () -> Unit,
    withEval: Boolean = false,
    onEval: () -> Unit = {},
    onAi: () -> Unit = {},
) {
    Column(Modifier.fillMaxSize().background(Cv.Bg)) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(Cv.Header)
                .padding(start = 8.dp, end = 22.dp, top = 12.dp, bottom = 18.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Retour", tint = Cv.Cream)
            }
            Column(Modifier.padding(start = 14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(genre.title.uppercase(), fontFamily = Bebas, fontSize = 36.sp, letterSpacing = 2.sp, color = Cv.Gold)
                Text(genre.subtitle, fontSize = 14.sp, color = Cv.Muted)
            }
        }
        Box(Modifier.fillMaxWidth().height(3.dp).background(Cv.Gold))
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            genre.scenes.forEachIndexed { i, s ->
                val open = sceneUnlocked(genre, i, p)
                val stars = p.sceneStars[s.id] ?: 0
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (open) Cv.Card else Cv.Locked)
                        .clickable(enabled = open) { onPlay(s.id) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("SCÈNE ${i + 1}", fontFamily = Bebas, fontSize = 18.sp, letterSpacing = 1.sp, color = if (open) Cv.Gold else Cv.Dim)
                        Text(s.title, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = if (open) Cv.Cream else Cv.Dim)
                        Text("Grammaire : ${s.tip.title}", fontSize = 13.sp, color = if (open) Cv.Muted else Cv.Dim)
                        if (open) StarsRow(stars, dp = 20.dp)
                    }
                    Spacer(Modifier.width(12.dp))
                    if (open) {
                        Box(
                            Modifier.size(48.dp).clip(RoundedCornerShape(50)).background(Cv.Gold),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = "Tourner", tint = Cv.Bg)
                        }
                    } else {
                        Icon(Icons.Filled.Lock, contentDescription = "Verrouillé", tint = Cv.Dim)
                    }
                }
            }
            if (withEval) EvalCard(genre, p, onEval)
            if (withEval) scenarioFor(genre.id)?.let { AiCard(it, genre, p, onAi) }
        }
    }
}

/** La scène improvisée avec l'IA, débloquée après l'avant-première réussie. */
@Composable
private fun AiCard(s: AiScenario, genre: Genre, p: Progress, onAi: () -> Unit) {
    val open = evalPassed(p, genre)
    val best = p.aiBest[genre.id]
    val shape = RoundedCornerShape(16.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (open) Cv.Card else Cv.Locked)
            .border(2.dp, if (open) Cv.Gold else Cv.Line, shape)
            .clickable(enabled = open, onClick = onAi)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AnimatedCharacter(s.character.look, "happy", speaking = false, size = 64.dp, animate = false)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("SCÈNE IMPROVISÉE", fontFamily = Bebas, fontSize = 20.sp, letterSpacing = 1.5.sp, color = if (open) Cv.Gold else Cv.Dim)
            Text(s.title, fontWeight = FontWeight.Bold, color = if (open) Cv.Cream else Cv.Dim)
            Text(
                when {
                    !open -> "Réussis l'avant-première pour parler avec ${s.character.name}."
                    best != null -> "Meilleure note : $best/20 · parle au micro avec une IA"
                    else -> "Parle en anglais au micro avec ${s.character.name}, joué par une IA."
                },
                fontSize = 13.sp, color = if (open) Cv.Muted else Cv.Dim,
            )
        }
        Icon(if (open) Icons.Filled.PlayArrow else Icons.Filled.Lock, contentDescription = null, tint = if (open) Cv.Gold else Cv.Dim)
    }
}

/** La carte de l'avant-première, en bas de la page d'un grand thème. */
@Composable
private fun EvalCard(genre: Genre, p: Progress, onEval: () -> Unit) {
    val best = p.evalBest[genre.id]
    // Thème déjà validé : l'avant-première reste ouverte, même avec les nouvelles scènes à tourner.
    val ready = genreDone(genre, p) || (best ?: -1) >= EVAL_PASS
    val shape = RoundedCornerShape(16.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (ready) Cv.Header else Cv.Locked)
            .border(2.dp, if (ready) Cv.Gold else Cv.Line, shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("AVANT-PREMIÈRE", fontFamily = Bebas, fontSize = 22.sp, letterSpacing = 2.sp, color = if (ready) Cv.Gold else Cv.Dim)
        Text(
            "L'évaluation du thème : 12 questions, sans indice, correction à la fin. Il faut $EVAL_PASS/20 pour débloquer la suite, et $EVAL_OSCAR/20 pour l'Oscar.",
            fontSize = 14.sp, lineHeight = 20.sp, color = if (ready) Cv.Cream else Cv.Dim,
        )
        when {
            !ready -> Text("Termine les ${genre.scenes.size} scènes pour la débloquer.", fontSize = 13.sp, color = Cv.Dim)
            best != null -> Text(
                "Meilleure note : $best/20" + when {
                    best >= EVAL_OSCAR -> " · Oscar obtenu ★"
                    best >= EVAL_PASS -> " · thème validé"
                    else -> " · pas encore validé"
                },
                fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (best >= EVAL_PASS) Cv.Gold else Cv.Bad,
            )
        }
        if (ready) {
            GoldButton(
                if (best == null) "Passer l'avant-première" else "Repasser l'avant-première",
                onClick = onEval, fontSize = 20, modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
