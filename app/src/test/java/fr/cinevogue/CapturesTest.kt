package fr.cinevogue

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import fr.cinevogue.data.ADVANCED
import fr.cinevogue.data.BEGINNER
import fr.cinevogue.data.Game
import fr.cinevogue.data.Gender
import fr.cinevogue.data.Genre
import fr.cinevogue.data.Level
import fr.cinevogue.data.Profile
import fr.cinevogue.data.Progress
import fr.cinevogue.data.Scene
import fr.cinevogue.data.Settings
import fr.cinevogue.data.Studio
import fr.cinevogue.ui.AppFrame
import fr.cinevogue.ui.CineTheme
import fr.cinevogue.ui.Cv
import fr.cinevogue.ui.HomeScreen
import fr.cinevogue.ui.PickerScreen
import fr.cinevogue.ui.ProfileCard
import fr.cinevogue.ui.ProfileFormScreen
import fr.cinevogue.ui.ProfileScreen
import fr.cinevogue.ui.QuestionUi
import fr.cinevogue.ui.ResultScreen
import fr.cinevogue.ui.ResultUi
import fr.cinevogue.ui.SceneScreen
import fr.cinevogue.ui.Tab
import fr.cinevogue.ui.TrophiesScreen
import fr.cinevogue.ui.VoiceScreen
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
    private val lea = Profile("p1", "Léa", Gender.FILLE, Level.DEBUTANT, 0, null, "cinevogue")
    private val tom = Profile("p2", "Tom", Gender.GARCON, Level.AVANCE, 4, null, "cinevogue_p2")
    private val sample = Progress(
        points = 120,
        sceneStars = mapOf("g1s1" to 3, "g1s2" to 2, "g1s3" to 2),
        streak = 3,
        bestStreak = 3,
        lastDay = today,
        missed = setOf("g1s2-s1", "g1s3-d0"),
        perfectCount = 1,
    )
    private val advanced = Progress(points = 185, sceneStars = mapOf("a1s1" to 3, "a1s2" to 3, "a1s3" to 2, "a1s4" to 2, "a2s1" to 2), streak = 5, bestStreak = 5, lastDay = today)

    private fun shot(content: @Composable () -> Unit) {
        paparazzi.snapshot {
            CineTheme {
                Box(Modifier.fillMaxSize().background(Cv.Bg)) { content() }
            }
        }
    }

    private fun question(genre: Genre, scene: Scene, game: Game) =
        Studio.candidates(genre, scene, Random(7)).first { it.game == game }

    private fun ui(genre: Genre, scene: Scene, game: Game, number: Int, feminine: Boolean = true) =
        QuestionUi(question(genre, scene, game), number, 8, null, false, emptySet(), if (game == Game.CLAP) 7 else null,
            Studio.sceneNumber(genre, scene), genre.title, feminine)

    @Test
    fun a_qui_tourne() = shot {
        PickerScreen(listOf(ProfileCard(lea, "Figurante", 120), ProfileCard(tom, "Second rôle", 185)), {}, {})
    }

    @Test
    fun b_nouveau_profil() = shot {
        ProfileFormScreen(null, legacyNote = true, onSave = { _, _, _, _ -> }, onCancel = {}, onDelete = null)
    }

    @Test
    fun c_accueil_debutant() = shot {
        AppFrame(Tab.PLATEAU, {}) { HomeScreen(lea, BEGINNER, sample, today, {}, {}, {}) }
    }

    @Test
    fun d_accueil_avance() = shot {
        AppFrame(Tab.PLATEAU, {}) { HomeScreen(tom, ADVANCED, advanced, today, {}, {}, {}) }
    }

    @Test
    fun e_script_troue_erreur() = shot {
        val g = BEGINNER[0]
        val u = ui(g, g.scenes[0], Game.SCRIPT, 3)
        SceneScreen(u.copy(selected = u.q.options.indices.first { it != u.q.answer }, checked = true), {}, {}, {}, {}, {})
    }

    @Test
    fun f_doublage_reussi() = shot {
        val g = BEGINNER[0]
        val u = ui(g, g.scenes[0], Game.DOUBLAGE, 1)
        SceneScreen(u.copy(selected = u.q.answer, checked = true), {}, {}, {}, {}, {})
    }

    @Test
    fun g_avance_doublage_erreur() = shot {
        val g = ADVANCED[1]
        val u = ui(g, g.scenes[0], Game.DOUBLAGE, 2, feminine = false)
        SceneScreen(u.copy(selected = u.q.options.indices.first { it != u.q.answer }, checked = true), {}, {}, {}, {}, {})
    }

    @Test
    fun h_avance_script_reussi() = shot {
        val g = ADVANCED[0]
        val u = ui(g, g.scenes[1], Game.SCRIPT, 4, feminine = false)
        SceneScreen(u.copy(selected = u.q.answer, checked = true), {}, {}, {}, {}, {})
    }

    @Test
    fun i_fin_de_scene() = shot {
        val scene = BEGINNER[0].scenes[3]
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
    fun j_profil() = shot {
        AppFrame(Tab.PROFIL, {}) {
            ProfileScreen(
                lea, BEGINNER, sample, Settings(), today,
                voiceLabel = "Voix 2 · femme (accent britannique · haute qualité · hors ligne)", voiceOk = true,
                onEdit = {}, onSwitch = {}, onReminder = {}, onPickTime = {}, onChooseVoice = {}, onTestVoice = {},
            )
        }
    }

    @Test
    fun k_choix_voix() = shot {
        VoiceScreen(
            voices = listOf(
                VoiceOption("v1", "Voix 1", "accent britannique · haute qualité · hors ligne"),
                VoiceOption("v2", "Voix 2 · femme", "accent britannique · hors ligne"),
                VoiceOption("v3", "Voix 3 · homme", "accent britannique · avec internet"),
                VoiceOption("v4", "Voix 4", "accent américain · hors ligne"),
            ),
            selected = "v2", onPick = {}, onPlay = {}, onInstall = {}, onBack = {},
        )
    }

    @Test
    fun l_trophees_acteur() = shot { AppFrame(Tab.TROPHEES, {}) { TrophiesScreen(ADVANCED, advanced, Gender.GARCON) } }
}
