package fr.cinevogue.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.cinevogue.data.MyLine
import fr.cinevogue.data.Studio

/** Onglet « Mes répliques » : les répliques notées en regardant des films en VO. */
@Composable
fun MyLinesScreen(
    lines: List<MyLine>,
    onAdd: () -> Unit,
    onPlay: (MyLine) -> Unit,
    onDelete: (MyLine) -> Unit,
    onReview: () -> Unit,
    onRepeat: (String) -> Unit = {},
) {
    var toDelete by remember { mutableStateOf<MyLine?>(null) }
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("MES RÉPLIQUES", fontFamily = Bebas, fontSize = 40.sp, letterSpacing = 3.sp, color = Cv.Gold)
        Text(
            "Tu regardes un film ou une série en VO ? Note les répliques qui te plaisent : l'appli te les lit et elles reviennent dans tes scènes en « Prise 2 ».",
            fontSize = 14.sp, color = Cv.Muted, lineHeight = 20.sp,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GoldButton("Ajouter", onClick = onAdd, fontSize = 20, modifier = Modifier.weight(1f))
            GoldButton("Réviser", onClick = onReview, enabled = lines.isNotEmpty(), fontSize = 20, modifier = Modifier.weight(1f))
        }
        if (lines.isEmpty()) {
            Card {
                Text("Ton carnet est vide", fontWeight = FontWeight.Bold)
                Text(
                    "Astuce : active les sous-titres anglais, mets sur pause quand une phrase te plaît, et note-la ici avec sa traduction.",
                    fontSize = 13.sp, color = Cv.Muted,
                )
            }
        }
        lines.asReversed().forEach { line ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Cv.Card)
                    .padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (line.film.isNotBlank()) {
                            Text(line.film.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Cv.Gold)
                        }
                        Text("« ${line.en} »", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(line.fr, fontSize = 14.sp, fontStyle = FontStyle.Italic, color = Cv.Muted)
                    }
                    IconButton(onClick = { onPlay(line) }) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = "Écouter", tint = Cv.Gold)
                    }
                    IconButton(onClick = { toDelete = line }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Supprimer", tint = Cv.Dim)
                    }
                }
                Studio.knownWords(line.en)?.let {
                    Text("Mots connus : $it", fontSize = 13.sp, color = Cv.Muted, modifier = Modifier.padding(end = 12.dp))
                }
                LineButton("Répète après moi", onClick = { onRepeat(line.en) }, color = Cv.Gold, border = Cv.Gold, modifier = Modifier.padding(end = 12.dp))
            }
        }
    }

    toDelete?.let { line ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Supprimer cette réplique ?") },
            text = { Text("« ${line.en} »") },
            confirmButton = {
                TextButton(onClick = { toDelete = null; onDelete(line) }) { Text("Supprimer", color = Cv.Bad) }
            },
            dismissButton = {
                TextButton(onClick = { toDelete = null }) { Text("Annuler", color = Cv.Cream) }
            },
            containerColor = Cv.Card,
            titleContentColor = Cv.Cream,
            textContentColor = Cv.Muted,
        )
    }
}

/** Formulaire pour noter une réplique. */
@Composable
fun MyLineFormScreen(
    films: List<String>,
    onListen: (String) -> Unit,
    onSave: (en: String, fr: String, film: String) -> Unit,
    onCancel: () -> Unit,
) {
    var en by remember { mutableStateOf("") }
    var fr by remember { mutableStateOf("") }
    var film by remember { mutableStateOf("") }
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Cv.Gold,
        unfocusedBorderColor = Cv.Track,
        focusedLabelColor = Cv.Gold,
        unfocusedLabelColor = Cv.Muted,
        cursorColor = Cv.Gold,
        focusedTextColor = Cv.Cream,
        unfocusedTextColor = Cv.Cream,
    )

    Column(Modifier.fillMaxSize().background(Cv.Bg)) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(Cv.Header)
                .padding(start = 8.dp, end = 22.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onCancel) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Retour", tint = Cv.Cream)
            }
            Text("NOUVELLE RÉPLIQUE", fontFamily = Bebas, fontSize = 32.sp, letterSpacing = 2.sp, color = Cv.Gold)
        }
        Box(Modifier.fillMaxWidth().height(3.dp).background(Cv.Gold))

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            OutlinedTextField(
                value = en,
                onValueChange = { if (it.length <= 200) en = it },
                label = { Text("La réplique en anglais") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LineButton("Écouter", onClick = { onListen(en) }, enabled = en.isNotBlank(), color = Cv.Gold, border = Cv.Gold)
            }
            Studio.knownWords(en)?.let {
                Text("Mots connus : $it", fontSize = 13.sp, color = Cv.Muted)
            }
            OutlinedTextField(
                value = fr,
                onValueChange = { if (it.length <= 200) fr = it },
                label = { Text("Sa traduction en français") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                "Pas sûr de la traduction ? Regarde les sous-titres français ou demande à quelqu'un.",
                fontSize = 13.sp, color = Cv.Muted,
            )
            OutlinedTextField(
                value = film,
                onValueChange = { if (it.length <= 40) film = it },
                label = { Text("Le film ou la série (facultatif)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth(),
            )
            if (films.isNotEmpty()) {
                Text("Raccourcis :", fontSize = 13.sp, color = Cv.Muted)
                films.chunked(2).forEach { pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        pair.forEach { name ->
                            LineButton(
                                name, onClick = { film = name },
                                color = if (film == name) Cv.Gold else Cv.Cream,
                                border = if (film == name) Cv.Gold else Cv.Track,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }

        Box(Modifier.padding(start = 22.dp, end = 22.dp, top = 12.dp, bottom = 20.dp)) {
            GoldButton(
                "Ajouter à mon carnet",
                onClick = { onSave(en.trim(), fr.trim(), film.trim()) },
                enabled = en.isNotBlank() && fr.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(58.dp),
            )
        }
    }
}
