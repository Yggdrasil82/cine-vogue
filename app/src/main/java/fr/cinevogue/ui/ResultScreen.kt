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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.cinevogue.data.Tip
import fr.cinevogue.data.Word

data class ResultUi(
    val sceneTitle: String,
    val correct: Int,
    val total: Int,
    val stars: Int,
    val points: Int,
    val words: List<Word>,
    val tip: Tip,
    val reminder: String?,
    val newRank: String?,
)

@Composable
fun ResultScreen(ui: ResultUi, onNext: (() -> Unit)?, onHome: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Cv.Bg)) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Cv.Header)
                    .padding(horizontal = 22.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("COUPEZ !", fontFamily = Bebas, fontSize = 56.sp, letterSpacing = 3.sp, color = Cv.Gold)
                Text(
                    when (ui.stars) {
                        3 -> "Une prise parfaite, bravo !"
                        2 -> "C'est dans la boîte, bravo !"
                        else -> "Scène tournée ! On s'améliore à chaque prise."
                    },
                )
                StarsRow(ui.stars)
                Text(
                    "+ ${ui.points} ★ · ${ui.correct} bonne${if (ui.correct > 1) "s" else ""} réponse${if (ui.correct > 1) "s" else ""} sur ${ui.total}",
                    fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Cv.Gold,
                )
                ui.newRank?.let { Pill("Nouveau rang : $it !") }
            }
            Box(Modifier.fillMaxWidth().height(3.dp).background(Cv.Gold))

            Column(
                Modifier.padding(horizontal = 22.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                SectionTitle("Tes nouvelles répliques", 24)
                ui.words.chunked(2).forEach { pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        pair.forEach { w ->
                            Column(
                                Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Cv.Card)
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                            ) {
                                Text(w.en, fontWeight = FontWeight.Bold)
                                Text(w.fr, fontSize = 13.sp, color = Cv.Muted)
                            }
                        }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                }

                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Cv.Cream)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("LE SECRET DU SCÉNARIO", fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Cv.Header)
                    Text(ui.tip.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Cv.Bg)
                    Text(ui.tip.text, fontSize = 14.sp, lineHeight = 20.sp, color = Cv.Bg)
                }

                ui.reminder?.let {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Filled.Notifications, contentDescription = null, tint = Cv.Gold)
                        Text(it, fontSize = 14.sp, color = Cv.Muted)
                    }
                }
            }
        }

        Column(
            Modifier.padding(start = 22.dp, end = 22.dp, top = 12.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (onNext != null) {
                GoldButton("Scène suivante", onClick = onNext, modifier = Modifier.fillMaxWidth().height(58.dp), fontSize = 26)
            }
            LineButton("Retour au plateau", onClick = onHome, modifier = Modifier.fillMaxWidth())
        }
    }
}
