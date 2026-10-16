package fr.cinevogue

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import fr.cinevogue.data.GENRES
import fr.cinevogue.data.Game
import fr.cinevogue.data.Progress
import fr.cinevogue.data.Studio
import fr.cinevogue.ui.AppFrame
import fr.cinevogue.ui.CineTheme
import fr.cinevogue.ui.Cv
import fr.cinevogue.ui.GenreScreen
import fr.cinevogue.ui.HomeScreen
import fr.cinevogue.ui.ProfileScreen
import fr.cinevogue.ui.QuestionUi
import fr.cinevogue.ui.ResultScreen
import fr.cinevogue.ui.ResultUi
import fr.cinevogue.ui.SceneScreen
import fr.cinevogue.ui.Tab
import fr.cinevogue.ui.TrophiesScreen
import org.junit.Rule
import org.junit.Test
import kotlin.random.Random

/** Captures d'écran générées sur GitHub Actions (sans téléphone). */
class CapturesTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "android:Theme.Material.NoActionBar",
    )

    private val today = 20000L
    private val sample = Progress(
        points = 120,
        sceneStars = mapOf("g1s1" to 3, "g1s2" to 2, "g1s3" to 2),
        streak = 3,
        bestStreak = 3,
        lastDay = today,
        missed = setOf("g1s2-s1", "g1s3-d0"),
        perfectCount = 1,
    )

    private fun shot(content: @Composable () -> Unit) {
        paparazzi.snapshot {
            CineTheme {
                Box(Modifier.fillMaxSize().background(Cv.Bg)) { content() }
            }
        }
    }

    private val genre = GENRES[0]
    private val scene = GENRES[0].scenes[3]
    private fun question(game: Game) = Studio.candidates(genre, scene, Random(7)).first { it.game == game }

    private fun ui(game: Game, number: Int, selected: Int?, checked: Boolean, wrongPick: Boolean = false): QuestionUi {
        val q = question(game).let { if (number == 6) it.copy(retake = true) else it }
        val pick = if (wrongPick) q.options.indices.first { it != q.answer } else selected
        return QuestionUi(q, number, 8, pick, checked, emptySet(), if (game == Game.CLAP) 7 else null, 4, genre.title)
    }

    @Test
    fun a_accueil() = shot {
        AppFrame(Tab.PLATEAU, {}) { HomeScreen(sample, today, {}, {}) }
    }

    @Test
    fun b_film() = shot { GenreScreen(genre, sample, {}, {}) }

    @Test
    fun c_doublage() = shot {
        val u = ui(Game.DOUBLAGE, 1, null, false)
        SceneScreen(u.copy(selected = u.q.answer), {}, {}, {}, {}, {})
    }

    @Test
    fun d_script_troue_erreur() = shot { SceneScreen(ui(Game.SCRIPT, 3, null, true, wrongPick = true), {}, {}, {}, {}, {}) }

    @Test
    fun e_casting() = shot { SceneScreen(ui(Game.CASTING, 6, null, false), {}, {}, {}, {}, {}) }

    @Test
    fun f_clap_de_fin_reussi() = shot {
        val u = ui(Game.CLAP, 8, null, true)
        SceneScreen(u.copy(selected = u.q.answer), {}, {}, {}, {}, {})
    }

    @Test
    fun g_fin_de_scene() = shot {
        ResultScreen(
            ResultUi(
                sceneTitle = scene.title, correct = 7, total = 8, stars = 2, points = 35,
                words = scene.words, tip = scene.tip,
                reminder = "Prochain tournage demain à 18 h 30", newRank = "Second rôle",
            ),
            onNext = {}, onHome = {},
        )
    }

    @Test
    fun h_trophees() = shot { AppFrame(Tab.TROPHEES, {}) { TrophiesScreen(sample) } }

    @Test
    fun i_profil() = shot { AppFrame(Tab.PROFIL, {}) { ProfileScreen(sample, today, true, {}, {}, {}) } }
}
