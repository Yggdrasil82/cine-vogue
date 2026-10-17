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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.cinevogue.data.EVAL_OSCAR
import fr.cinevogue.data.EVAL_PASS
import fr.cinevogue.data.Tip

/** Une question ratée, pour le bilan. */
data class Mistake(val prompt: String, val yours: String, val right: String, val explain: String)

data class EvalUi(
    val genreTitle: String,
    val score: Int,
    val correct: Int,
    val total: Int,
    val best: Int,
    val nextTitle: String?,
    val mastered: List<String>,
    val toReview: List<Tip>,
    val mistakes: List<Mistake>,
    val newRank: String?,
) {
    val passed get() = score >= EVAL_PASS
    val oscar get() = score >= EVAL_OSCAR
}

@Composable
fun EvalResultScreen(ui: EvalUi, onNext: (() -> Unit)?, onRetry: () -> Unit, onHome: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Cv.Bg)) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Cv.Header)
                    .padding(horizontal = 22.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("AVANT-PREMIÈRE", fontFamily = Bebas, fontSize = 22.sp, letterSpacing = 3.sp, color = Cv.Muted)
                Text(ui.genreTitle, fontWeight = FontWeight.Bold, fontSize = 18.sp, textAlign = TextAlign.Center)
                Text("${ui.score}/20", fontFamily = Bebas, fontSize = 72.sp, color = if (ui.passed) Cv.Gold else Cv.Bad)
                Text(
                    when {
                        ui.oscar -> "Et l'Oscar est pour… toi ! Thème validé avec brio."
                        ui.passed -> "Thème validé ! La suite est débloquée."
                        else -> "Pas encore : il faut $EVAL_PASS/20 pour valider. Revois les points ci-dessous et retente !"
                    },
                    textAlign = TextAlign.Center,
                )
                Text("${ui.correct} bonne${if (ui.correct > 1) "s" else ""} réponse${if (ui.correct > 1) "s" else ""} sur ${ui.total} · meilleure note : ${ui.best}/20", fontSize = 13.sp, color = Cv.Muted)
                if (ui.oscar) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        StarMark(filled = true, dp = 28.dp)
                        Pill("OSCAR : ${ui.genreTitle.uppercase()}")
                    }
                }
                ui.newRank?.let { Pill("Nouveau rang : $it !") }
            }
            Box(Modifier.fillMaxWidth().height(3.dp).background(Cv.Gold))

            Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (ui.mastered.isNotEmpty()) {
                    SectionTitle("Points réussis", 24)
                    ui.mastered.forEach { Text("✓  $it", color = Cv.Good, fontWeight = FontWeight.Bold) }
                }
                if (ui.toReview.isNotEmpty()) {
                    SectionTitle("À revoir", 24)
                    ui.toReview.forEach { tip ->
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Cv.Cream)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(tip.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Cv.Bg)
                            Text(tip.text, fontSize = 14.sp, lineHeight = 20.sp, color = Cv.Bg)
                        }
                    }
                }
                if (ui.mistakes.isNotEmpty()) {
                    SectionTitle("La correction", 24)
                    ui.mistakes.forEachIndexed { i, m ->
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Cv.Card)
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text("Question ratée ${i + 1}", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Cv.Gold)
                            Text(m.prompt, fontWeight = FontWeight.Bold)
                            Text("Ta réponse : ${m.yours}", fontSize = 14.sp, color = Cv.Bad)
                            Text("Bonne réponse : ${m.right}", fontSize = 14.sp, color = Cv.Good)
                            Text(m.explain, fontSize = 14.sp, lineHeight = 20.sp, color = Cv.Muted)
                        }
                    }
                } else {
                    Text("Aucune erreur : un sans-faute digne d'une star !", fontWeight = FontWeight.Bold, color = Cv.Good)
                }
            }
        }

        Column(
            Modifier.padding(start = 22.dp, end = 22.dp, top = 12.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            when {
                ui.passed && onNext != null ->
                    GoldButton("Thème suivant : ${ui.nextTitle ?: ""}", onClick = onNext, modifier = Modifier.fillMaxWidth().height(58.dp), fontSize = 22)
                !ui.passed ->
                    GoldButton("Retenter l'avant-première", onClick = onRetry, modifier = Modifier.fillMaxWidth().height(58.dp), fontSize = 22)
            }
            if (ui.passed && !ui.oscar) {
                LineButton("Retenter pour l'Oscar ($EVAL_OSCAR/20)", onClick = onRetry, color = Cv.Gold, border = Cv.Gold, modifier = Modifier.fillMaxWidth())
            }
            LineButton("Retour au plateau", onClick = onHome, modifier = Modifier.fillMaxWidth())
        }
    }
}
