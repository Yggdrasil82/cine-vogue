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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.cinevogue.VoiceOption
import fr.cinevogue.data.Gender
import fr.cinevogue.data.Level
import fr.cinevogue.data.Profile
import fr.cinevogue.data.filmsOfLevel

const val MAX_PROFILES = 6

/** Une ligne du choix de profil. */
data class ProfileCard(val profile: Profile, val rank: String, val points: Int)

@Composable
fun PickerScreen(cards: List<ProfileCard>, onPick: (Profile) -> Unit, onAdd: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Cv.Bg)) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(Cv.Header)
                .padding(horizontal = 22.dp, vertical = 26.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("CINÉ VOGUE", fontFamily = Bebas, fontSize = 44.sp, letterSpacing = 3.sp, color = Cv.Gold)
            Text("Qui tourne aujourd'hui ?", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Box(Modifier.fillMaxWidth().height(3.dp).background(Cv.Gold))
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            cards.forEach { c ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Cv.Card)
                        .clickable { onPick(c.profile) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    ProfileAvatar(c.profile.name, c.profile.color, dp = 60.dp)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(c.profile.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(c.rank, fontSize = 14.sp, color = Cv.Gold)
                        Text("${c.profile.level.label} · ${c.points} ★", fontSize = 13.sp, color = Cv.Muted)
                    }
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Jouer", tint = Cv.Gold)
                }
            }
            if (cards.size < MAX_PROFILES) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .border(2.dp, Cv.Track, RoundedCornerShape(18.dp))
                        .clickable(onClick = onAdd)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Box(
                        Modifier.size(60.dp).clip(CircleShape).background(Cv.Locked),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = Cv.Gold)
                    }
                    Text("Nouveau profil", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }
    }
}

/** Création ou modification d'un profil. */
@Composable
fun ProfileFormScreen(
    initial: Profile?,
    legacyNote: Boolean,
    onSave: (name: String, gender: Gender, level: Level, color: Int, films: List<String>) -> Unit,
    onCancel: (() -> Unit)?,
    onDelete: (() -> Unit)?,
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var gender by remember { mutableStateOf(initial?.gender ?: Gender.FILLE) }
    var level by remember { mutableStateOf(initial?.level ?: Level.DEBUTANT) }
    var color by remember { mutableIntStateOf(initial?.color ?: 0) }
    var confirmDelete by remember { mutableStateOf(false) }
    var films by remember {
        mutableStateOf(initial?.films?.takeIf { it.isNotEmpty() } ?: filmsOfLevel(initial?.level ?: Level.DEBUTANT).map { it.id })
    }

    Column(Modifier.fillMaxSize().background(Cv.Bg)) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(Cv.Header)
                .padding(start = 8.dp, end = 22.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onCancel != null) {
                IconButton(onClick = onCancel) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Retour", tint = Cv.Cream)
                }
            } else {
                Spacer(Modifier.size(14.dp))
            }
            Text(
                if (initial == null) "NOUVEAU PROFIL" else "MODIFIER LE PROFIL",
                fontFamily = Bebas, fontSize = 32.sp, letterSpacing = 2.sp, color = Cv.Gold,
            )
        }
        Box(Modifier.fillMaxWidth().height(3.dp).background(Cv.Gold))

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (initial == null && legacyNote) {
                Text(
                    "Ta progression de la première version sera rattachée à ce profil.",
                    fontSize = 14.sp, color = Cv.Muted,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                ProfileAvatar(name, color, dp = 64.dp)
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 20) name = it },
                    label = { Text("Ton prénom") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Cv.Gold,
                        unfocusedBorderColor = Cv.Track,
                        focusedLabelColor = Cv.Gold,
                        unfocusedLabelColor = Cv.Muted,
                        cursorColor = Cv.Gold,
                        focusedTextColor = Cv.Cream,
                        unfocusedTextColor = Cv.Cream,
                    ),
                    modifier = Modifier.weight(1f),
                )
            }

            SectionTitle("Ta couleur", 22)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AVATAR_COLORS.forEachIndexed { i, c ->
                    Box(
                        Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(c)
                            .then(if (i == color) Modifier.border(3.dp, Cv.Cream, CircleShape) else Modifier)
                            .clickable(onClickLabel = "Couleur ${i + 1}") { color = i },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (i == color) Icon(Icons.Filled.Check, contentDescription = "Choisie", tint = Cv.Bg)
                    }
                }
            }

            SectionTitle("Ton rôle", 22)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Choice("Actrice", "Figurante → Star", gender == Gender.FILLE, Modifier.weight(1f)) { gender = Gender.FILLE }
                Choice("Acteur", "Figurant → Star", gender == Gender.GARCON, Modifier.weight(1f)) { gender = Gender.GARCON }
            }

            SectionTitle("Ton niveau", 22)
            Level.entries.forEach { lv ->
                Choice(lv.label, lv.detail, level == lv, Modifier.fillMaxWidth()) {
                    if (lv != level) {
                        level = lv
                        films = filmsOfLevel(lv).map { it.id }
                    }
                }
            }
            if (initial != null && initial.level != level) {
                Text(
                    "Changer de niveau garde tes étoiles. Les scènes de l'autre niveau restent enregistrées.",
                    fontSize = 13.sp, color = Cv.Muted,
                )
            }

            SectionTitle("Tes films", 22)
            Text("Touche un film pour l'ajouter ou le retirer.", fontSize = 13.sp, color = Cv.Muted)
            filmsOfLevel(level).chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    pair.forEach { f ->
                        val on = f.id in films
                        Choice(f.title, if (on) "Choisi ✓" else "Ajouter", on, Modifier.weight(1f)) {
                            films = if (on) films - f.id else films + f.id
                        }
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }

            if (onDelete != null) {
                Spacer(Modifier.height(8.dp))
                LineButton(
                    "Supprimer ce profil", onClick = { confirmDelete = true },
                    color = Cv.Bad, border = Cv.Bad, modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Box(Modifier.padding(start = 22.dp, end = 22.dp, top = 12.dp, bottom = 20.dp)) {
            GoldButton(
                if (initial == null) "Créer mon profil" else "Enregistrer",
                onClick = { onSave(name.trim(), gender, level, color, films) },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(58.dp),
            )
        }
    }

    if (confirmDelete && onDelete != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Supprimer le profil de ${initial?.name ?: ""} ?") },
            text = { Text("Toute sa progression sera effacée. On ne pourra pas revenir en arrière.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) { Text("Supprimer", color = Cv.Bad) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Annuler", color = Cv.Cream) }
            },
            containerColor = Cv.Card,
            titleContentColor = Cv.Cream,
            textContentColor = Cv.Muted,
        )
    }
}

@Composable
private fun Choice(title: String, detail: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier
            .heightIn(min = 56.dp)
            .clip(shape)
            .background(if (selected) Cv.Selected else Cv.Locked)
            .border(if (selected) 3.dp else 2.dp, if (selected) Cv.Gold else Cv.Line, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(detail, fontSize = 13.sp, color = Cv.Muted)
    }
}

/** Choix de la voix anglaise du profil. */
@Composable
fun VoiceScreen(
    voices: List<VoiceOption>,
    selected: String?,
    onPick: (String?) -> Unit,
    onPlay: (String?) -> Unit,
    onInstall: () -> Unit,
    onBack: () -> Unit,
) {
    Column(Modifier.fillMaxSize().background(Cv.Bg)) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(Cv.Header)
                .padding(start = 8.dp, end = 22.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Retour", tint = Cv.Cream)
            }
            Text("CHOISIR UNE VOIX", fontFamily = Bebas, fontSize = 32.sp, letterSpacing = 2.sp, color = Cv.Gold)
        }
        Box(Modifier.fillMaxWidth().height(3.dp).background(Cv.Gold))
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "Touche une voix pour l'écouter et la choisir. Le téléphone ne dit pas toujours si c'est une voix d'homme ou de femme : fie-toi à ton oreille !",
                fontSize = 14.sp, color = Cv.Muted,
            )
            VoiceRow("Automatique", "Accent britannique, choisi par le téléphone", selected == null, { onPick(null); onPlay(null) }, { onPlay(null) })
            voices.forEach { v ->
                VoiceRow(v.label, v.detail, selected == v.name, { onPick(v.name); onPlay(v.name) }, { onPlay(v.name) })
            }
            if (voices.isEmpty()) {
                Text("Aucune autre voix anglaise n'est installée sur ce téléphone.", fontSize = 14.sp, color = Cv.Bad)
            }
            Spacer(Modifier.height(6.dp))
            Card {
                Text("Envie de voix plus naturelles ?", fontWeight = FontWeight.Bold)
                Text(
                    "Dans les réglages : Moteur Google › Installer les données vocales › Anglais (Royaume-Uni) ou (États-Unis). Choisis les voix « haute qualité », puis reviens ici.",
                    fontSize = 13.sp, color = Cv.Muted, textAlign = TextAlign.Start,
                )
                GoldButton("Installer d'autres voix", onClick = onInstall, fontSize = 18, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun VoiceRow(label: String, detail: String, selected: Boolean, onSelect: () -> Unit, onPlay: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .clip(shape)
            .background(if (selected) Cv.Selected else Cv.Locked)
            .border(if (selected) 3.dp else 2.dp, if (selected) Cv.Gold else Cv.Line, shape)
            .clickable(onClick = onSelect)
            .padding(start = 16.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, fontWeight = FontWeight.Bold)
            Text(detail, fontSize = 13.sp, color = Cv.Muted)
        }
        if (selected) Icon(Icons.Filled.Check, contentDescription = "Choisie", tint = Cv.Gold)
        IconButton(onClick = onPlay) {
            Icon(Icons.Filled.PlayArrow, contentDescription = "Écouter $label", tint = Cv.Gold)
        }
    }
}
