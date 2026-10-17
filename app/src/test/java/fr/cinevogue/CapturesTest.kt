package fr.cinevogue

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import fr.cinevogue.ai.AiReview
import fr.cinevogue.ai.AiTurn
import fr.cinevogue.ai.ChatLine
import fr.cinevogue.data.ADVANCED
import fr.cinevogue.data.AI_SCENARIOS
import fr.cinevogue.data.BEGINNER
import fr.cinevogue.data.FILMS_ADVANCED
import fr.cinevogue.data.FILMS_BEGINNER
import fr.cinevogue.data.MyLine
import fr.cinevogue.data.filmsOf
import fr.cinevogue.data.Game
import fr.cinevogue.data.Gender
import fr.cinevogue.data.Genre
import fr.cinevogue.data.Level
import fr.cinevogue.data.Profile
import fr.cinevogue.data.Progress
import fr.cinevogue.data.Scene
import fr.cinevogue.data.Settings
import fr.cinevogue.data.Studio
import fr.cinevogue.ui.AiDialogueScreen
import fr.cinevogue.ui.AiIntroScreen
import fr.cinevogue.ui.AiResultScreen
import fr.cinevogue.ui.AiUi
import fr.cinevogue.ui.AppFrame
import fr.cinevogue.ui.ParentSettingsScreen
import fr.cinevogue.ui.ShareKeyScreen
import fr.cinevogue.ui.CineTheme
import fr.cinevogue.ui.Cv
import fr.cinevogue.ui.EvalResultScreen
import fr.cinevogue.ui.EvalUi
import fr.cinevogue.ui.GenreScreen
import fr.cinevogue.ui.HomeScreen
import fr.cinevogue.ui.Mistake
import fr.cinevogue.ui.MyLineFormScreen
import fr.cinevogue.ui.MyLinesScreen
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
    private val lea = Profile("p1", "Léa", Gender.FILLE, Level.DEBUTANT, 0, null, "cinevogue", FILMS_BEGINNER.map { it.id })
    private val tom = Profile("p2", "Tom", Gender.GARCON, Level.AVANCE, 4, null, "cinevogue_p2", FILMS_ADVANCED.map { it.id })
    private val lines = listOf(
        MyLine("l1", "I'm not afraid of anything.", "Je n'ai peur de rien.", "Harry Potter"),
        MyLine("l2", "We need to find the treasure before them.", "On doit trouver le trésor avant eux.", "Uncharted"),
    )
    private val sample = Progress(
        points = 120,
        sceneStars = mapOf("g1s1" to 3, "g1s2" to 2, "g1s3" to 2),
        streak = 3,
        bestStreak = 3,
        lastDay = today,
        missed = setOf("g1s2-s1", "g1s3-d0"),
        perfectCount = 1,
        myLines = lines,
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
            "Scène ${Studio.sceneNumber(genre, scene)}", genre.title, feminine)

    @Test
    fun a_qui_tourne() = shot {
        PickerScreen(listOf(ProfileCard(lea, "Figurante", 120), ProfileCard(tom, "Second rôle", 185)), {}, {})
    }

    @Test
    fun b_nouveau_profil() = shot {
        ProfileFormScreen(null, legacyNote = true, onSave = { _, _, _, _, _ -> }, onCancel = {}, onDelete = null)
    }

    @Test
    fun c_accueil_debutant() = shot {
        AppFrame(Tab.PLATEAU, {}) { HomeScreen(lea, BEGINNER, filmsOf(lea), sample.copy(sceneStars = sample.sceneStars + ("f01s1" to 3)), today, {}, {}, {}, {}) }
    }

    @Test
    fun d_accueil_avance() = shot {
        AppFrame(Tab.PLATEAU, {}) { HomeScreen(tom, ADVANCED, filmsOf(tom), advanced, today, {}, {}, {}, {}) }
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
    fun l_trophees_acteur() = shot { AppFrame(Tab.TROPHEES, {}) { TrophiesScreen(ADVANCED, advanced, Gender.GARCON, filmsOf(tom)) } }

    @Test
    fun m_film_harry_potter() = shot {
        val g = FILMS_BEGINNER[0]
        val u = ui(g, g.scenes[0], Game.SCRIPT, 3)
        SceneScreen(u.copy(selected = u.q.answer, checked = true), {}, {}, {}, {}, {})
    }

    @Test
    fun n_film_interstellar() = shot {
        val g = FILMS_ADVANCED.first { it.id == "f16" }
        val u = ui(g, g.scenes[0], Game.SCRIPT, 2, feminine = false)
        SceneScreen(u.copy(selected = u.q.options.indices.first { it != u.q.answer }, checked = true), {}, {}, {}, {}, {})
    }

    @Test
    fun o_mes_repliques() = shot {
        AppFrame(Tab.REPLIQUES, {}) { MyLinesScreen(lines, {}, {}, {}, {}) }
    }

    @Test
    fun p_nouvelle_replique() = shot {
        MyLineFormScreen(filmsOf(lea).map { it.title }.take(4), {}, { _, _, _ -> }, {})
    }

    @Test
    fun q_prise2_ta_replique() = shot {
        val q = Studio.mySession(lines, BEGINNER, Random(3)).first { it.game == Game.DOUBLAGE }.copy(retake = true)
        SceneScreen(QuestionUi(q, 6, 8, q.answer, true, emptySet(), null, "Scène 2", "Harry Potter", true), {}, {}, {}, {}, {})
    }

    private val g1Done = sample.copy(sceneStars = mapOf("g1s1" to 3, "g1s2" to 2, "g1s3" to 2, "g1s4" to 3))

    @Test
    fun r_theme_avant_premiere() = shot {
        GenreScreen(BEGINNER[0], g1Done.copy(evalBest = mapOf("g1" to 15)), {}, {}, withEval = true, onEval = {})
    }

    @Test
    fun s_avant_premiere_question() = shot {
        val q = Studio.evaluation(BEGINNER[0], Random(5)).first { it.game == Game.SCRIPT }
        SceneScreen(QuestionUi(q, 4, 12, q.options.indices.first { it != q.answer }, false, emptySet(), null, "Avant-première", BEGINNER[0].title, true, exam = true), {}, {}, {}, {}, {})
    }

    @Test
    fun t_bilan_reussi() = shot {
        val g = BEGINNER[0]
        EvalResultScreen(
            EvalUi(
                genreTitle = g.title, score = 15, correct = 9, total = 12, best = 15, nextTitle = BEGINNER[1].title,
                mastered = listOf(g.scenes[0].tip.title, g.scenes[2].tip.title, g.scenes[3].tip.title),
                toReview = listOf(g.scenes[1].tip),
                mistakes = listOf(Mistake("« Have you got _____ brothers or sisters? »", "some", "any", g.scenes[1].lines[1].why ?: "")),
                newRank = null,
            ),
            onNext = {}, onRetry = {}, onHome = {},
        )
    }

    @Test
    fun u_bilan_a_retenter() = shot {
        val g = ADVANCED[1]
        EvalResultScreen(
            EvalUi(
                genreTitle = g.title, score = 8, correct = 5, total = 12, best = 8, nextTitle = ADVANCED[2].title,
                mastered = listOf(g.scenes[3].tip.title),
                toReview = listOf(g.scenes[0].tip, g.scenes[2].tip),
                mistakes = listOf(Mistake("« So the thief _____ have come through the garden. »", "mustn't", "can't", g.scenes[0].lines[1].why ?: "")),
                newRank = null,
            ),
            onNext = {}, onRetry = {}, onHome = {},
        )
    }

    private val jordan = AI_SCENARIOS.first { it.genreId == "g1" }
    private val reid = AI_SCENARIOS.first { it.genreId == "a2" }

    @Test
    fun v_ia_presentation() = shot { AiIntroScreen(jordan, ready = true, best = null, onStart = {}, onBack = {}) }

    @Test
    fun w_ia_dialogue() = shot {
        val t1 = AiTurn("Nice to meet you, Léa! How old are you?", "happy", "On dit « I'm from France » : from pour l'origine.", "My name is Léa and I'm from France.", "Dis ton âge : « I'm fifteen. »", setOf(0))
        AiDialogueScreen(
            AiUi(
                scenario = jordan,
                lines = listOf(
                    ChatLine(false, jordan.opening, AiTurn(jordan.opening)),
                    ChatLine(true, "my name is Léa I am of France"),
                    ChatLine(false, t1.reply, t1),
                ),
                objectivesDone = setOf(0), loading = false, error = null,
                hint = "Dis ton âge : « I'm fifteen. »", mood = "happy", speaking = true, turnsLeft = 9,
            ),
            animate = false, onMic = {}, onHint = {}, onReplay = {}, onRetry = {}, onFinish = {}, onQuit = {},
        )
    }

    @Test
    fun x_ia_dialogue_avance() = shot {
        val t1 = AiTurn("Quite the coincidence. And who exactly can confirm that?", "thinking", "", "", "", setOf(0))
        AiDialogueScreen(
            AiUi(
                scenario = reid,
                lines = listOf(
                    ChatLine(false, reid.opening, AiTurn(reid.opening)),
                    ChatLine(true, "I was looking at the sculptures on the first floor when the alarm went off"),
                    ChatLine(false, t1.reply, t1),
                ),
                objectivesDone = setOf(0), loading = true, error = null, hint = null, mood = "thinking", speaking = false, turnsLeft = 9,
            ),
            animate = false, onMic = {}, onHint = {}, onReplay = {}, onRetry = {}, onFinish = {}, onQuit = {},
        )
    }

    @Test
    fun y_ia_bilan() = shot {
        AiResultScreen(
            jordan,
            AiReview(
                15, "Tu as répondu à toutes les questions de Jordan et tu as osé parler. Ta prononciation est claire.",
                listOf("Pour l'origine, dis « I'm from France » et non « of France ».", "Pense au -s : « He likes football »."),
                "« What about you? » = « Et toi ? » : parfait pour relancer la conversation.",
            ),
            objectivesDone = 3, best = 15, onRetry = {}, onBack = {},
        )
    }

    @Test
    fun z_reglages_parent() = shot {
        ParentSettingsScreen(
            hasKey = true, model = "", testMessage = "✓ La clé fonctionne.", testing = false,
            onSaveKey = {}, onSaveModel = {}, onTest = {}, onDeleteKey = {}, onShareKey = {}, onScanKey = {}, onChangePin = {}, onBack = {},
        )
    }

    @Test
    fun zz_partager_cle() = shot { ShareKeyScreen("exemple-de-cle-fictive-0123456789", onBack = {}) }
}
