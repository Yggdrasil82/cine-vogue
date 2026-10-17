package fr.cinevogue.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter

/** Préfixe des QR codes de Ciné Vogue, pour ne pas accepter n'importe quel QR code. */
const val KEY_QR_PREFIX = "cinevogue-key:v1:"

fun keyToQr(key: String): String = KEY_QR_PREFIX + key.trim()

/** Renvoie la clé contenue dans un QR code de Ciné Vogue, ou null. */
fun keyFromQr(raw: String?): String? =
    raw?.trim()?.takeIf { it.startsWith(KEY_QR_PREFIX) }?.removePrefix(KEY_QR_PREFIX)?.trim()?.takeIf { it.length >= 16 }

private fun qrMatrix(content: String): BitMatrix =
    QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 0, 0, mapOf(EncodeHintType.MARGIN to 2))

/** Un QR code dessiné en noir sur blanc. */
@Composable
fun QrCode(content: String, modifier: Modifier = Modifier) {
    val matrix = remember(content) { qrMatrix(content) }
    Canvas(modifier.aspectRatio(1f).background(Color.White)) {
        val cell = size.minDimension / matrix.width
        for (y in 0 until matrix.height) {
            for (x in 0 until matrix.width) {
                if (matrix.get(x, y)) {
                    drawRect(Color.Black, topLeft = Offset(x * cell, y * cell), size = Size(cell + 0.5f, cell + 0.5f))
                }
            }
        }
    }
}

/** Écran qui affiche la clé sous forme de QR code, à scanner depuis l'autre téléphone. */
@Composable
fun ShareKeyScreen(key: String, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Cv.Bg)) {
        ParentHeaderPublic("PARTAGER LA CLÉ", onBack)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Sur l'autre téléphone : Profil › Réglages parent › « Scanner une clé », puis vise ce code.",
                fontSize = 15.sp, lineHeight = 21.sp, textAlign = TextAlign.Center,
            )
            Box(Modifier.fillMaxWidth(0.85f).clip(RoundedCornerShape(16.dp)).background(Color.White).padding(12.dp)) {
                QrCode(keyToQr(key), Modifier.fillMaxWidth())
            }
            Text(
                "Ce code contient ta clé Mistral : ne le montre qu'à tes propres téléphones, et ferme cet écran ensuite.",
                fontSize = 13.sp, color = Cv.Gold, lineHeight = 19.sp, textAlign = TextAlign.Center,
            )
        }
        Box(Modifier.padding(start = 22.dp, end = 22.dp, top = 12.dp, bottom = 20.dp)) {
            GoldButton("C'est fait", onClick = onBack, modifier = Modifier.fillMaxWidth().height(58.dp))
        }
    }
}
