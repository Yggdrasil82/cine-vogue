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
import fr.cinevogue.data.currentStreak
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
    p: Progress,
    today: Long,
    onPlay: (String) -> Unit,
    onGenre: (String) -> Unit,
    onSwitch: () -> Unit,
) {
    val rank = rankFor(p.points)
    val next = nextRank(p.points)
    val streak = currentStreak(p, today)
    val up = nextToPlay(genres, p)

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
                Box(Modifier.weight(1f)) { SectionTitle("À l'affiche") }
                Pill(profile.level.label.uppercase(), background = Cv.Card, color = Cv.Gold)
            }
            genres.forEachIndexed { i, g ->
                GenreCard(
                    genre = g,
                    unlocked = genreUnlocked(genres, i, p),
                    current = up?.first == g,
                    done = scenesDone(g, p),
                    lockHint = if (i > 0) "Termine « ${genres[i - 1].title} » pour débloquer" else "",
                    onClick = { onGenre(g.id) },
                )
            }
        }

        Box(Modifier.padding(horizontal = 22.dp, vertical = 14.dp)) {
            if (up != null) {
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
private fun GenreCard(
    genre: Genre,
    unlocked: Boolean,
    current: Boolean,
    done: Int,
    lockHint: String,
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
                done == genre.scenes.size -> Pill("TERMINÉ", background = Cv.Track, color = Cv.Gold)
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
fun GenreScreen(genre: Genre, p: Progress, onPlay: (String) -> Unit, onBack: () -> Unit) {
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
        }
    }
}
