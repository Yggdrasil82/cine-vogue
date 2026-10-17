package fr.cinevogue.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.cinevogue.data.Gender
import fr.cinevogue.data.Genre
import fr.cinevogue.data.Profile
import fr.cinevogue.data.Progress
import fr.cinevogue.data.RANKS
import fr.cinevogue.data.Settings
import fr.cinevogue.data.currentStreak
import fr.cinevogue.data.rankFor
import fr.cinevogue.data.scenesDone
import fr.cinevogue.data.trophies

fun formatTime(hour: Int, minute: Int): String = "$hour h " + minute.toString().padStart(2, '0')

@Composable
private fun ScreenTitle(text: String) {
    Text(text, fontFamily = Bebas, fontSize = 40.sp, letterSpacing = 3.sp, color = Cv.Gold)
}

@Composable
fun TrophiesScreen(genres: List<Genre>, p: Progress, gender: Gender, films: List<Genre> = emptyList()) {
    val rank = rankFor(p.points)
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ScreenTitle("TROPHÉES")
        SectionTitle("Ta carrière", 24)
        RANKS.forEach { r ->
            val reached = p.points >= r.min
            val isCurrent = r == rank
            val shape = RoundedCornerShape(14.dp)
            var m = Modifier.fillMaxWidth().clip(shape).background(if (isCurrent) Cv.Card else Cv.Locked)
            if (isCurrent) m = m.border(2.dp, Cv.Gold, shape)
            Row(
                m.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StarMark(filled = reached, dp = 24.dp, color = if (reached) Cv.Gold else Cv.Dim)
                Text(
                    r.title(gender),
                    fontWeight = FontWeight.Bold,
                    color = if (reached) Cv.Cream else Cv.Dim,
                    modifier = Modifier.weight(1f),
                )
                if (isCurrent) Pill("TOI") else Text("${r.min} ★", fontSize = 13.sp, color = Cv.Dim)
            }
        }

        SectionTitle("Récompenses", 24)
        trophies(genres, p, gender, films).forEach { t ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (t.earned) Cv.Card else Cv.Locked)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                StarMark(filled = t.earned, dp = 34.dp, color = if (t.earned) Cv.Gold else Cv.Track)
                Column(Modifier.weight(1f)) {
                    Text(t.title, fontWeight = FontWeight.Bold, color = if (t.earned) Cv.Cream else Cv.Dim)
                    Text(t.description, fontSize = 13.sp, color = if (t.earned) Cv.Muted else Cv.Dim)
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    profile: Profile,
    genres: List<Genre>,
    p: Progress,
    settings: Settings,
    today: Long,
    voiceLabel: String,
    voiceOk: Boolean,
    onEdit: () -> Unit,
    onSwitch: () -> Unit,
    onReminder: (Boolean) -> Unit,
    onPickTime: () -> Unit,
    onChooseVoice: () -> Unit,
    onTestVoice: () -> Unit,
) {
    val totalScenes = genres.sumOf { it.scenes.size }
    val done = genres.sumOf { scenesDone(it, p) }
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ScreenTitle("PROFIL")
        Card {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                ProfileAvatar(profile.name, profile.color, dp = 56.dp)
                Column(Modifier.weight(1f)) {
                    Text(profile.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("${rankFor(p.points).title(profile.gender)} · ${profile.level.label}", fontSize = 14.sp, color = Cv.Muted)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LineButton("Modifier", onClick = onEdit, color = Cv.Gold, border = Cv.Gold, modifier = Modifier.weight(1f))
                LineButton("Changer de profil", onClick = onSwitch, modifier = Modifier.weight(1f))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Stat("${p.points} ★", "étoiles gagnées", Modifier.weight(1f))
            Stat("$done / $totalScenes", "scènes tournées", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Stat("${currentStreak(p, today)} j", "série actuelle", Modifier.weight(1f))
            Stat("${p.bestStreak} j", "meilleure série", Modifier.weight(1f))
        }
        Text(
            if (p.missed.isEmpty()) "Aucune réplique à revoir : bravo !"
            else "${p.missed.size} réplique${if (p.missed.size > 1) "s" else ""} à revoir : elles reviendront en « Prise 2 ».",
            fontSize = 14.sp, color = Cv.Muted,
        )

        SectionTitle("Ta voix", 24)
        Card {
            Text(voiceLabel, fontWeight = FontWeight.Bold)
            if (!voiceOk) {
                Text(
                    "Voix anglaise introuvable : installe-la avec « Choisir une voix » › « Installer d'autres voix ».",
                    fontSize = 13.sp, color = Cv.Bad,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GoldButton("Choisir une voix", onClick = onChooseVoice, fontSize = 18, modifier = Modifier.weight(1f))
                LineButton("Tester", onClick = onTestVoice, color = Cv.Gold, border = Cv.Gold, modifier = Modifier.weight(0.6f))
            }
        }

        SectionTitle("Rappel quotidien", 24)
        Card {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Le tournage t'attend !", fontWeight = FontWeight.Bold)
                    Text("Commun à tous les profils : prévient ceux qui n'ont pas encore joué", fontSize = 13.sp, color = Cv.Muted)
                }
                Switch(
                    checked = settings.reminderOn,
                    onCheckedChange = onReminder,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Cv.Bg,
                        checkedTrackColor = Cv.Gold,
                        uncheckedThumbColor = Cv.Dim,
                        uncheckedTrackColor = Cv.Locked,
                        uncheckedBorderColor = Cv.Track,
                    ),
                )
            }
            if (settings.reminderOn) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Heure du rappel", modifier = Modifier.weight(1f))
                    LineButton(formatTime(settings.reminderHour, settings.reminderMinute), onClick = onPickTime, color = Cv.Gold, border = Cv.Gold)
                }
            }
        }
    }
}

@Composable
private fun Stat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Cv.Card)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(value, fontFamily = Bebas, fontSize = 30.sp, color = Cv.Gold)
        Text(label, fontSize = 13.sp, color = Cv.Muted)
    }
}

@Composable
fun Card(content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Cv.Card)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        content()
    }
}
