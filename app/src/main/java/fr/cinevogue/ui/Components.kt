package fr.cinevogue.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GoldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fontSize: Int = 24,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Cv.Gold,
            contentColor = Cv.Bg,
            disabledContainerColor = Cv.Track,
            disabledContentColor = Cv.Dim,
        ),
    ) {
        Text(text, fontFamily = Bebas, fontSize = fontSize.sp, letterSpacing = 2.sp)
    }
}

@Composable
fun LineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = Cv.Cream,
    border: Color = Cv.Track,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, if (enabled) border else Cv.Line),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = color, disabledContentColor = Cv.Track),
    ) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

@Composable
fun Pill(text: String, background: Color = Cv.Gold, color: Color = Cv.Bg) {
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun ProgressBar(fraction: Float, modifier: Modifier = Modifier, height: Dp = 10.dp, track: Color = Cv.Bg) {
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(50))
            .background(track),
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .background(Cv.Gold),
        )
    }
}

@Composable
fun Segments(done: Int, total: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(total) { i ->
            Box(
                Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (i < done) Cv.Gold else Cv.Track),
            )
        }
    }
}

/** Étoile dessinée (pleine ou en contour). */
@Composable
fun StarMark(filled: Boolean, dp: Dp, color: Color = Cv.Gold) {
    Canvas(Modifier.size(dp)) {
        val s = size.minDimension
        val path = Path()
        val c = Offset(s / 2f, s / 2f + s * 0.04f)
        val outer = s / 2f
        val inner = outer * 0.45f
        for (i in 0 until 10) {
            val r = if (i % 2 == 0) outer else inner
            val a = -Math.PI / 2 + i * Math.PI / 5
            val x = c.x + (r * cos(a)).toFloat()
            val y = c.y + (r * sin(a)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        if (filled) drawPath(path, color) else drawPath(path, color, style = Stroke(width = s * 0.06f))
    }
}

@Composable
fun StarsRow(count: Int, total: Int = 3, dp: Dp = 46.dp, color: Color = Cv.Gold) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(total) { i -> StarMark(filled = i < count, dp = dp, color = color) }
    }
}

@Composable
fun Avatar(name: String, dp: Dp = 46.dp) {
    Box(
        Modifier
            .size(dp)
            .clip(CircleShape)
            .background(Cv.Header)
            .border(2.dp, Cv.Gold, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(name.take(1).uppercase(), fontFamily = Bebas, fontSize = 22.sp, color = Cv.Gold)
    }
}

@Composable
fun SectionTitle(text: String, size: Int = 26) {
    Text(text, fontFamily = Bebas, fontSize = size.sp, letterSpacing = 1.5.sp, color = Cv.Gold)
}

enum class Tab { PLATEAU, TROPHEES, PROFIL }

/** Écran avec la barre du bas (Plateau, Trophées, Profil). */
@Composable
fun AppFrame(tab: Tab, onTab: (Tab) -> Unit, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize().background(Cv.Bg)) {
        Box(Modifier.weight(1f)) { content() }
        Row(
            Modifier
                .fillMaxWidth()
                .background(Cv.Nav)
                .padding(top = 8.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            NavItem("Plateau", Icons.Filled.Home, tab == Tab.PLATEAU) { onTab(Tab.PLATEAU) }
            NavItem("Trophées", Icons.Filled.Star, tab == Tab.TROPHEES) { onTab(Tab.TROPHEES) }
            NavItem("Profil", Icons.Filled.Person, tab == Tab.PROFIL) { onTab(Tab.PROFIL) }
        }
    }
}

@Composable
private fun NavItem(label: String, icon: ImageVector, active: Boolean, onClick: () -> Unit) {
    val color = if (active) Cv.Gold else Cv.Dim
    Column(
        Modifier
            .widthIn(min = 72.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(icon, contentDescription = null, tint = color)
        Text(label, fontSize = 12.sp, color = color, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
    }
}
