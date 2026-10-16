package fr.cinevogue.ui

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import fr.cinevogue.R

/** Palette « tapis rouge ». */
object Cv {
    val Bg = Color(0xFF22060B)
    val Header = Color(0xFF7A0E1C)
    val Card = Color(0xFF3A0D14)
    val Locked = Color(0xFF2E0A10)
    val Nav = Color(0xFF180408)
    val Screen = Color(0xFF0E0204)
    val Gold = Color(0xFFE2B546)
    val Cream = Color(0xFFF6EEDC)
    val Muted = Color(0xFFE9D9BE)
    val Dim = Color(0xFFCDB9A0)
    val Line = Color(0xFF4A1520)
    val Track = Color(0xFF5C1E27)
    val Selected = Color(0xFF4A121B)
    val Good = Color(0xFF7FD3A8)
    val GoodBg = Color(0xFF1C3527)
    val Bad = Color(0xFFF5A08A)
    val BadBg = Color(0xFF45160F)
}

val Bebas = FontFamily(Font(R.font.bebas_neue))

val DmSans = FontFamily(
    Font(R.font.dm_sans_regular, FontWeight.Normal),
    Font(R.font.dm_sans_bold, FontWeight.Bold),
)

@Composable
fun CineTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Cv.Gold,
            onPrimary = Cv.Bg,
            background = Cv.Bg,
            onBackground = Cv.Cream,
            surface = Cv.Card,
            onSurface = Cv.Cream,
        ),
    ) {
        CompositionLocalProvider(
            LocalContentColor provides Cv.Cream,
            LocalTextStyle provides TextStyle(fontFamily = DmSans, fontSize = 16.sp, color = Cv.Cream),
        ) {
            content()
        }
    }
}
