package fr.cinevogue.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
private fun parentFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Cv.Gold,
    unfocusedBorderColor = Cv.Track,
    focusedLabelColor = Cv.Gold,
    unfocusedLabelColor = Cv.Muted,
    cursorColor = Cv.Gold,
    focusedTextColor = Cv.Cream,
    unfocusedTextColor = Cv.Cream,
)

@Composable
fun ParentHeaderPublic(title: String, onBack: () -> Unit) = ParentHeader(title, onBack)

@Composable
private fun ParentHeader(title: String, onBack: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(Cv.Header).padding(start = 8.dp, end = 22.dp, top = 14.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Retour", tint = Cv.Cream) }
        Text(title, fontFamily = Bebas, fontSize = 32.sp, letterSpacing = 2.sp, color = Cv.Gold)
    }
    Box(Modifier.fillMaxWidth().height(3.dp).background(Cv.Gold))
}

/** Saisie ou création du code parent (4 chiffres). */
@Composable
fun PinScreen(creating: Boolean, error: String?, onSubmit: (String) -> Unit, onBack: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    val ok = pin.length == 4 && (!creating || confirm == pin)
    Column(Modifier.fillMaxSize().background(Cv.Bg)) {
        ParentHeader("RÉGLAGES PARENT", onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                if (creating) "Choisis un code à 4 chiffres. Il protège la clé de l'IA et les réglages parent."
                else "Entre le code parent.",
                fontSize = 15.sp, lineHeight = 21.sp,
            )
            OutlinedTextField(
                value = pin,
                onValueChange = { v -> if (v.length <= 4 && v.all { it.isDigit() }) pin = v },
                label = { Text("Code parent") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                colors = parentFieldColors(),
                modifier = Modifier.fillMaxWidth(),
            )
            if (creating) {
                OutlinedTextField(
                    value = confirm,
                    onValueChange = { v -> if (v.length <= 4 && v.all { it.isDigit() }) confirm = v },
                    label = { Text("Confirme le code") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    colors = parentFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (confirm.length == 4 && confirm != pin) Text("Les deux codes sont différents.", color = Cv.Bad, fontSize = 13.sp)
            }
            error?.let { Text(it, color = Cv.Bad, fontSize = 14.sp) }
        }
        Box(Modifier.padding(start = 22.dp, end = 22.dp, top = 12.dp, bottom = 20.dp)) {
            GoldButton(if (creating) "Créer le code" else "Valider", onClick = { onSubmit(pin) }, enabled = ok, modifier = Modifier.fillMaxWidth().height(58.dp))
        }
    }
}

/** Réglages parent : la clé Mistral et le modèle. */
@Composable
fun ParentSettingsScreen(
    hasKey: Boolean,
    model: String,
    testMessage: String?,
    testing: Boolean,
    onSaveKey: (String) -> Unit,
    onSaveModel: (String) -> Unit,
    onTest: () -> Unit,
    onDeleteKey: () -> Unit,
    onShareKey: () -> Unit,
    onScanKey: () -> Unit,
    onChangePin: () -> Unit,
    onBack: () -> Unit,
) {
    var key by remember { mutableStateOf("") }
    var modelText by remember { mutableStateOf(model) }
    Column(Modifier.fillMaxSize().background(Cv.Bg)) {
        ParentHeader("RÉGLAGES PARENT", onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SectionTitle("L'IA des scènes improvisées", 24)
            Card {
                Text(if (hasKey) "Clé Mistral enregistrée ✓" else "Aucune clé enregistrée", fontWeight = FontWeight.Bold, color = if (hasKey) Cv.Good else Cv.Bad)
                Text(
                    "Pour obtenir une clé gratuite : sur console.mistral.ai, crée un compte, choisis l'offre gratuite « Experiment » (un numéro de téléphone est demandé), puis « API Keys » › « Create new key ». Copie la clé et colle-la ici.",
                    fontSize = 13.sp, color = Cv.Muted, lineHeight = 19.sp,
                )
                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it.trim() },
                    label = { Text(if (hasKey) "Remplacer la clé" else "Coller la clé Mistral") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = parentFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
                GoldButton("Enregistrer la clé", onClick = { onSaveKey(key); key = "" }, enabled = key.length >= 16, fontSize = 18, modifier = Modifier.fillMaxWidth())
                if (hasKey) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        LineButton(if (testing) "Test…" else "Tester la clé", onClick = onTest, enabled = !testing, color = Cv.Gold, border = Cv.Gold, modifier = Modifier.weight(1f))
                        LineButton("Supprimer", onClick = onDeleteKey, color = Cv.Bad, border = Cv.Bad, modifier = Modifier.weight(1f))
                    }
                }
                testMessage?.let { Text(it, fontSize = 14.sp, color = if (it.startsWith("✓")) Cv.Good else Cv.Bad) }
            }
            Card {
                Text("Partager entre téléphones", fontWeight = FontWeight.Bold)
                Text("Pas besoin de recopier la clé : affiche-la en QR code sur un téléphone et scanne-la avec l'autre.", fontSize = 13.sp, color = Cv.Muted, lineHeight = 19.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LineButton("Partager la clé", onClick = onShareKey, enabled = hasKey, color = Cv.Gold, border = Cv.Gold, modifier = Modifier.weight(1f))
                    LineButton("Scanner une clé", onClick = onScanKey, color = Cv.Gold, border = Cv.Gold, modifier = Modifier.weight(1f))
                }
            }
            Card {
                Text("Confidentialité", fontWeight = FontWeight.Bold)
                Text(
                    "Pendant une scène, ce qui est dit est envoyé à Mistral (entreprise française) pour que l'IA réponde. Avec l'offre gratuite, ces échanges peuvent servir à entraîner leurs modèles. L'appli demande à l'IA de ne jamais réclamer d'informations personnelles et rappelle aux élèves de ne pas en donner. La clé reste uniquement sur ce téléphone.",
                    fontSize = 13.sp, color = Cv.Muted, lineHeight = 19.sp,
                )
            }
            Card {
                Text("Modèle d'IA (avancé)", fontWeight = FontWeight.Bold)
                Text("À modifier seulement si Mistral change le nom de ses modèles.", fontSize = 13.sp, color = Cv.Muted)
                OutlinedTextField(
                    value = modelText,
                    onValueChange = { modelText = it.trim() },
                    label = { Text("Modèle (par défaut : ministral-8b-latest)") },
                    singleLine = true,
                    colors = parentFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
                LineButton("Enregistrer le modèle", onClick = { onSaveModel(modelText) }, color = Cv.Gold, border = Cv.Gold, modifier = Modifier.fillMaxWidth())
            }
            LineButton("Changer le code parent", onClick = onChangePin, modifier = Modifier.fillMaxWidth())
            Text("Ciné Vogue 1.4", fontSize = 12.sp, color = Cv.Dim, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}
