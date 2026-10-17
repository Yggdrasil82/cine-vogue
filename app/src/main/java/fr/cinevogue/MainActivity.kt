package fr.cinevogue

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.compose.runtime.rememberCoroutineScope
import fr.cinevogue.ai.AiError
import fr.cinevogue.ai.AiReview
import fr.cinevogue.ai.AiTurn
import fr.cinevogue.ai.ChatLine
import fr.cinevogue.ai.Mistral
import fr.cinevogue.data.AiScenario
import fr.cinevogue.data.finishAi
import fr.cinevogue.data.scenarioFor
import fr.cinevogue.ui.AiIntroScreen
import fr.cinevogue.ui.AiResultScreen
import fr.cinevogue.ui.AiDialogueScreen
import fr.cinevogue.ui.AiUi
import fr.cinevogue.ui.ParentSettingsScreen
import fr.cinevogue.ui.PinScreen
import kotlinx.coroutines.launch
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import fr.cinevogue.ui.ShareKeyScreen
import fr.cinevogue.ui.RepeatDialog
import fr.cinevogue.ui.RepeatTools
import fr.cinevogue.ui.keyFromQr
import fr.cinevogue.data.FILMS
import fr.cinevogue.data.Game
import fr.cinevogue.data.Question
import fr.cinevogue.data.finishEval
import fr.cinevogue.data.genreDone
import fr.cinevogue.data.score20
import fr.cinevogue.data.MyLine
import fr.cinevogue.data.Tip
import fr.cinevogue.data.Word
import fr.cinevogue.data.filmsOf
import fr.cinevogue.data.filmsOfLevel
import fr.cinevogue.data.Gender
import fr.cinevogue.data.Genre
import fr.cinevogue.data.POINTS_PER_ANSWER
import fr.cinevogue.data.Profile
import fr.cinevogue.data.ProfileRepo
import fr.cinevogue.data.Progress
import fr.cinevogue.data.ProgressStore
import fr.cinevogue.data.Scene
import fr.cinevogue.data.Settings
import fr.cinevogue.data.Studio
import fr.cinevogue.data.finishScene
import fr.cinevogue.data.genresFor
import fr.cinevogue.data.nextToPlay
import fr.cinevogue.data.rankFor
import fr.cinevogue.ui.AppFrame
import fr.cinevogue.ui.CineTheme
import fr.cinevogue.ui.Cv
import fr.cinevogue.ui.GenreScreen
import fr.cinevogue.ui.EvalResultScreen
import fr.cinevogue.ui.EvalUi
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
import fr.cinevogue.ui.formatTime
import kotlinx.coroutines.delay
import java.time.LocalDate
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    private lateinit var repo: ProfileRepo
    private lateinit var narrator: Narrator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repo = ProfileRepo(this)
        narrator = Narrator(this)
        Reminder.sync(this, repo.settings())
        setContent {
            CineTheme {
                Box(Modifier.fillMaxSize().background(Cv.Bg)) {
                    CineApp(repo, narrator)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Des voix ont pu être installées dans les réglages.
        narrator.refresh()
    }

    override fun onDestroy() {
        narrator.shutdown()
        super.onDestroy()
    }
}

private sealed interface Route {
    data object Picker : Route
    data class Form(val profileId: String?) : Route
    data class Main(val tab: Tab) : Route
    data class GenreRoute(val id: String) : Route
    data class Play(val sceneId: String) : Route
    data class Result(val ui: ResultUi, val nextSceneId: String?) : Route
    data object Voices : Route
    data object LineForm : Route
    data class Eval(val genreId: String) : Route
    data class EvalResult(val ui: EvalUi, val genreId: String, val nextGenreId: String?) : Route
    data class AiIntro(val genreId: String) : Route
    data class AiPlay(val genreId: String, val take: Long = System.nanoTime()) : Route
    data class AiResult(val genreId: String, val review: AiReview, val objectivesDone: Int, val best: Int) : Route
    data class Pin(val change: Boolean = false) : Route
    data object Parent : Route
    data object ShareKey : Route
}

private fun today(): Long = LocalDate.now().toEpochDay()

@Composable
private fun CineApp(repo: ProfileRepo, narrator: Narrator) {
    val context = LocalContext.current
    var profiles by remember { mutableStateOf(repo.profiles()) }
    var settings by remember { mutableStateOf(repo.settings()) }
    var currentId by remember { mutableStateOf<String?>(null) }
    var progress by remember { mutableStateOf(Progress()) }
    var route by remember { mutableStateOf<Route>(if (profiles.isEmpty()) Route.Form(null) else Route.Picker) }

    val current: Profile? = profiles.firstOrNull { it.id == currentId }
    val scope = rememberCoroutineScope()
    var aiKey by remember { mutableStateOf(repo.mistralKey) }
    var aiModel by remember { mutableStateOf(repo.mistralModel) }
    var pinError by remember { mutableStateOf<String?>(null) }
    var testMessage by remember { mutableStateOf<String?>(null) }
    var testing by remember { mutableStateOf(false) }
    var repeatTarget by remember { mutableStateOf<String?>(null) }
    val tools = RepeatTools(
        speak = { text, slow -> narrator.speak(text, slow, current?.voice) },
        aiTip = if (aiKey.isBlank()) null else { w, sentence, heard -> Mistral.pronunciationTip(aiKey, aiModel, w, sentence, heard) },
    )
    repeatTarget?.let { t -> RepeatDialog(t, tools) { repeatTarget = null } }

    fun open(p: Profile) {
        currentId = p.id
        progress = ProgressStore(context, p.prefs).load()
        route = Route.Main(Tab.PLATEAU)
    }

    fun saveProgress(p: Progress) {
        val prof = current ?: return
        progress = p
        ProgressStore(context, prof.prefs).save(p)
    }

    fun saveProfile(p: Profile) {
        repo.update(p)
        profiles = repo.profiles()
    }

    fun saveSettings(s: Settings) {
        settings = s
        repo.saveSettings(s)
        Reminder.sync(context, s)
    }

    val askPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33 && !repo.askedNotifications &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            repo.askedNotifications = true
            askPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Sans profil choisi, seuls le choix et la création sont possibles.
    val r = route
    if (current == null && r !is Route.Picker && r !is Route.Form && r !is Route.Pin && r !is Route.Parent && r !is Route.ShareKey) {
        LaunchedEffect(r) { route = if (profiles.isEmpty()) Route.Form(null) else Route.Picker }
        return
    }

    when (r) {
        is Route.Picker -> {
            PickerScreen(
                cards = profiles.map { p ->
                    val pr = ProgressStore(context, p.prefs).load()
                    ProfileCard(p, rankFor(pr.points).title(p.gender), pr.points)
                },
                onPick = { open(it) },
                onAdd = { route = Route.Form(null) },
            )
        }

        is Route.Form -> {
            val editing = r.profileId?.let { id -> profiles.firstOrNull { it.id == id } }
            val canCancel = editing != null || profiles.isNotEmpty()
            val back = { route = if (editing != null) Route.Main(Tab.PLATEAU) else Route.Picker }
            if (canCancel) BackHandler { back() }
            ProfileFormScreen(
                initial = editing,
                legacyNote = remember { repo.hasLegacyProgress() },
                onSave = { name, gender, level, color, films ->
                    if (editing == null) {
                        val created = repo.create(name, gender, level, color, films)
                        profiles = repo.profiles()
                        open(created)
                    } else {
                        saveProfile(editing.copy(name = name, gender = gender, level = level, color = color, films = films))
                        route = Route.Main(Tab.PLATEAU)
                    }
                },
                onCancel = if (canCancel) back else null,
                onDelete = editing?.let { prof ->
                    {
                        repo.delete(prof)
                        profiles = repo.profiles()
                        currentId = null
                        route = if (profiles.isEmpty()) Route.Form(null) else Route.Picker
                    }
                },
            )
        }

        is Route.Main -> {
            val prof = current!!
            val genres = genresFor(prof.level)
            val films = filmsOf(prof)
            BackHandler { route = if (r.tab != Tab.PLATEAU) Route.Main(Tab.PLATEAU) else Route.Picker }
            AppFrame(r.tab, onTab = { route = Route.Main(it) }) {
                when (r.tab) {
                    Tab.PLATEAU -> HomeScreen(
                        prof, genres, films, progress, today(),
                        onPlay = { route = Route.Play(it) },
                        onGenre = { route = Route.GenreRoute(it) },
                        onSwitch = { route = Route.Picker },
                        onEditFilms = { route = Route.Form(prof.id) },
                        onEval = { route = Route.Eval(it) },
                    )
                    Tab.REPLIQUES -> MyLinesScreen(
                        lines = progress.myLines,
                        onAdd = { route = Route.LineForm },
                        onPlay = { narrator.speak(it.en, voiceName = prof.voice) },
                        onDelete = { line ->
                            saveProgress(
                                progress.copy(
                                    myLines = progress.myLines.filter { it.id != line.id },
                                    missed = progress.missed.filter { !it.startsWith(Studio.MY_PREFIX + line.id + "-") }.toSet(),
                                ),
                            )
                        },
                        onReview = { route = Route.Play(Studio.MY_SCENE) },
                        onRepeat = { repeatTarget = it },
                    )
                    Tab.TROPHEES -> TrophiesScreen(genres, progress, prof.gender, films)
                    Tab.PROFIL -> ProfileScreen(
                        profile = prof,
                        genres = genres,
                        p = progress,
                        settings = settings,
                        today = today(),
                        voiceLabel = narrator.label(prof.voice),
                        voiceOk = narrator.ukAvailable || narrator.voices.isNotEmpty(),
                        onEdit = { route = Route.Form(prof.id) },
                        onSwitch = { route = Route.Picker },
                        onReminder = { on -> saveSettings(settings.copy(reminderOn = on)) },
                        onPickTime = {
                            TimePickerDialog(
                                context,
                                { _, h, m -> saveSettings(settings.copy(reminderHour = h, reminderMinute = m)) },
                                settings.reminderHour, settings.reminderMinute, true,
                            ).show()
                        },
                        onChooseVoice = { narrator.refresh(); route = Route.Voices },
                        onTestVoice = { narrator.speak(sampleLine(prof), voiceName = prof.voice) },
                        onParent = { pinError = null; route = Route.Pin() },
                    )
                }
            }
        }

        is Route.Voices -> {
            val prof = current!!
            BackHandler { route = Route.Main(Tab.PROFIL) }
            VoiceScreen(
                voices = narrator.voices,
                selected = prof.voice,
                onPick = { name -> saveProfile(prof.copy(voice = name)) },
                onPlay = { name -> narrator.speak(sampleLine(prof), voiceName = name) },
                onInstall = { Narrator.openSettings(context) },
                onBack = { route = Route.Main(Tab.PROFIL) },
            )
        }

        is Route.LineForm -> {
            val prof = current!!
            BackHandler { route = Route.Main(Tab.REPLIQUES) }
            MyLineFormScreen(
                films = filmsOf(prof).map { it.title },
                onListen = { narrator.speak(it, voiceName = prof.voice) },
                onSave = { en, fr, film ->
                    val line = MyLine("l" + System.currentTimeMillis(), en, fr, film)
                    saveProgress(progress.copy(myLines = progress.myLines + line))
                    route = Route.Main(Tab.REPLIQUES)
                },
                onCancel = { route = Route.Main(Tab.REPLIQUES) },
            )
        }

        is Route.GenreRoute -> {
            val prof = current!!
            val genre = (genresFor(prof.level) + filmsOf(prof)).firstOrNull { it.id == r.id }
            if (genre == null) {
                LaunchedEffect(r) { route = Route.Main(Tab.PLATEAU) }
            } else {
                BackHandler { route = Route.Main(Tab.PLATEAU) }
                GenreScreen(
                    genre, progress,
                    onPlay = { route = Route.Play(it) },
                    onBack = { route = Route.Main(Tab.PLATEAU) },
                    withEval = genresFor(prof.level).any { it.id == genre.id },
                    onEval = { route = Route.Eval(genre.id) },
                    onAi = { route = Route.AiIntro(genre.id) },
                )
            }
        }

        is Route.Play -> {
            val prof = current!!
            val genres = genresFor(prof.level)
            val pool = genres + filmsOfLevel(prof.level)
            val reminder = if (settings.reminderOn) "Prochain tournage demain à ${formatTime(settings.reminderHour, settings.reminderMinute)}" else null

            fun finish(sceneId: String, title: String, correct: List<String>, wrong: List<String>, words: List<Word>, tip: Tip, next: (Progress) -> String?) {
                val before = rankFor(progress.points)
                val (updated, stars) = finishScene(progress, sceneId, correct, wrong, today())
                saveProgress(updated)
                val after = rankFor(updated.points)
                route = Route.Result(
                    ResultUi(
                        sceneTitle = title,
                        correct = correct.size,
                        total = correct.size + wrong.size,
                        stars = stars,
                        points = correct.size * POINTS_PER_ANSWER,
                        words = words,
                        tip = tip,
                        reminder = reminder,
                        newRank = if (after != before) after.title(prof.gender) else null,
                    ),
                    next(updated),
                )
            }

            if (r.sceneId == Studio.MY_SCENE) {
                if (progress.myLines.isEmpty()) {
                    LaunchedEffect(r) { route = Route.Main(Tab.REPLIQUES) }
                } else {
                    SceneHost(
                        key = "my" + progress.myLines.size,
                        build = { rnd -> Studio.mySession(progress.myLines, pool, rnd) },
                        header = "Mes répliques",
                        subtitle = "Ton carnet",
                        profile = prof,
                        narrator = narrator,
                        tools = tools,
                        onQuit = { route = Route.Main(Tab.REPLIQUES) },
                        onFinish = { answers ->
                            val (correct, wrong) = split(answers)
                            finish(
                                Studio.MY_SCENE, "Mes répliques", correct, wrong,
                                progress.myLines.takeLast(4).map { Word(it.en, it.fr) },
                                MY_TIP,
                            ) { null }
                        },
                    )
                }
            } else {
                val found = Studio.find(r.sceneId)
                if (found == null) {
                    LaunchedEffect(r) { route = Route.Main(Tab.PLATEAU) }
                } else {
                    val (genre, scene) = found
                    SceneHost(
                        key = scene.id,
                        build = { rnd -> Studio.session(pool, genre, scene, progress.missed, progress.myLines, rnd) },
                        header = "Scène ${Studio.sceneNumber(genre, scene)}",
                        subtitle = genre.title,
                        profile = prof,
                        narrator = narrator,
                        tools = tools,
                        onQuit = { route = Route.Main(Tab.PLATEAU) },
                        onFinish = { answers ->
                            val (correct, wrong) = split(answers)
                            val isFilm = FILMS.any { it.second.id == genre.id }
                            finish(scene.id, scene.title, correct, wrong, scene.words, scene.tip) { updated ->
                                if (isFilm) {
                                    // Scène suivante du même film, s'il y en a une.
                                    genre.scenes.getOrNull(genre.scenes.indexOf(scene) + 1)?.id
                                } else {
                                    nextToPlay(genres, updated)?.second?.id
                                }
                            }
                        },
                    )
                }
            }
        }

        is Route.Eval -> {
            val prof = current!!
            val genres = genresFor(prof.level)
            val genre = genres.firstOrNull { it.id == r.genreId }
            if (genre == null || !genreDone(genre, progress)) {
                LaunchedEffect(r) { route = Route.Main(Tab.PLATEAU) }
            } else {
                SceneHost(
                    key = "eval-" + genre.id + "-" + System.identityHashCode(r),
                    build = { rnd -> Studio.evaluation(genre, rnd) },
                    header = "Avant-première",
                    subtitle = genre.title,
                    profile = prof,
                    narrator = narrator,
                    tools = tools,
                    exam = true,
                    onQuit = { route = Route.GenreRoute(genre.id) },
                    onFinish = { answers ->
                        val (correct, wrong) = split(answers)
                        val before = rankFor(progress.points)
                        val updated = finishEval(progress, genre.id, correct, wrong, today())
                        saveProgress(updated)
                        val after = rankFor(updated.points)
                        val wrongTips = answers.filter { (q, sel) -> sel != q.answer }.mapNotNull { Studio.tipOf(it.first) }.distinct()
                        val allTips = answers.mapNotNull { Studio.tipOf(it.first) }.distinct()
                        val idx = genres.indexOf(genre)
                        val next = genres.getOrNull(idx + 1)
                        route = Route.EvalResult(
                            EvalUi(
                                genreTitle = genre.title,
                                score = score20(correct.size, answers.size),
                                correct = correct.size,
                                total = answers.size,
                                best = updated.evalBest[genre.id] ?: 0,
                                nextTitle = next?.title,
                                mastered = (allTips - wrongTips.toSet()).map { it.title },
                                toReview = wrongTips,
                                mistakes = answers.filter { (q, sel) -> sel != q.answer }.map { (q, sel) ->
                                    Mistake(
                                        prompt = if (q.game == Game.CLAP) "Que veut dire « ${q.text} » ?" else "« ${q.text} »",
                                        yours = sel?.let { q.options[it] } ?: "pas de réponse",
                                        right = q.options[q.answer],
                                        explain = q.why ?: q.translation,
                                    )
                                },
                                newRank = if (after != before) after.title(prof.gender) else null,
                            ),
                            genre.id,
                            next?.id,
                        )
                    },
                )
            }
        }

        is Route.AiIntro -> {
            val s = scenarioFor(r.genreId)
            if (s == null) {
                LaunchedEffect(r) { route = Route.Main(Tab.PLATEAU) }
            } else {
                BackHandler { route = Route.GenreRoute(r.genreId) }
                AiIntroScreen(
                    s, ready = aiKey.isNotBlank(), best = progress.aiBest[r.genreId],
                    onStart = { route = Route.AiPlay(r.genreId) },
                    onBack = { route = Route.GenreRoute(r.genreId) },
                )
            }
        }

        is Route.AiPlay -> {
            val prof = current!!
            val s = scenarioFor(r.genreId)
            if (s == null) {
                LaunchedEffect(r) { route = Route.Main(Tab.PLATEAU) }
            } else {
                AiHost(
                    key = r.take, scenario = s, profile = prof, apiKey = aiKey, model = aiModel, narrator = narrator, tools = tools,
                    onQuit = { narrator.stop(); route = Route.GenreRoute(r.genreId) },
                    onDone = { review, done ->
                        val updated = finishAi(progress, r.genreId, review.score, today())
                        saveProgress(updated)
                        route = Route.AiResult(r.genreId, review, done, updated.aiBest[r.genreId] ?: review.score)
                    },
                )
            }
        }

        is Route.AiResult -> {
            val s = scenarioFor(r.genreId)!!
            BackHandler { route = Route.GenreRoute(r.genreId) }
            AiResultScreen(
                s, r.review, r.objectivesDone, r.best,
                onRetry = { route = Route.AiPlay(r.genreId) },
                onBack = { route = Route.GenreRoute(r.genreId) },
            )
        }

        is Route.Pin -> {
            val back = { route = if (current != null) Route.Main(Tab.PROFIL) else Route.Picker }
            BackHandler { back() }
            val creating = r.change || !repo.hasPin()
            PinScreen(
                creating = creating,
                error = pinError,
                onSubmit = { pin ->
                    if (creating) {
                        repo.setPin(pin)
                        pinError = null
                        route = Route.Parent
                    } else if (repo.checkPin(pin)) {
                        pinError = null
                        route = Route.Parent
                    } else {
                        pinError = "Code incorrect."
                    }
                },
                onBack = back,
            )
        }

        is Route.Parent -> {
            val back = { testMessage = null; route = if (current != null) Route.Main(Tab.PROFIL) else Route.Picker }
            BackHandler { back() }
            ParentSettingsScreen(
                hasKey = aiKey.isNotBlank(),
                model = aiModel,
                testMessage = testMessage,
                testing = testing,
                onSaveKey = { k -> repo.mistralKey = k; aiKey = repo.mistralKey; testMessage = null },
                onSaveModel = { m -> repo.mistralModel = m; aiModel = repo.mistralModel; testMessage = "✓ Modèle enregistré." },
                onTest = {
                    scope.launch {
                        testing = true
                        testMessage = try {
                            if (Mistral.test(aiKey, aiModel)) "✓ La clé fonctionne." else "La clé répond, mais de façon inattendue."
                        } catch (e: AiError) {
                            e.messageFr
                        }
                        testing = false
                    }
                },
                onDeleteKey = { repo.mistralKey = ""; aiKey = ""; testMessage = null },
                onShareKey = { route = Route.ShareKey },
                onScanKey = {
                    try {
                        GmsBarcodeScanning.getClient(
                            context,
                            GmsBarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build(),
                        ).startScan()
                            .addOnSuccessListener { code ->
                                val k = keyFromQr(code.rawValue)
                                if (k != null) {
                                    repo.mistralKey = k
                                    aiKey = repo.mistralKey
                                    testMessage = "✓ Clé importée. Touche « Tester la clé » pour vérifier."
                                } else {
                                    testMessage = "Ce QR code n'est pas une clé Ciné Vogue."
                                }
                            }
                            .addOnFailureListener { testMessage = "Le scan n'a pas fonctionné. Vérifie que les services Google Play sont à jour." }
                    } catch (e: Exception) {
                        testMessage = "Le scan n'est pas disponible sur ce téléphone."
                    }
                },
                onChangePin = { pinError = null; route = Route.Pin(change = true) },
                onBack = back,
            )
        }

        is Route.ShareKey -> {
            val back = { route = Route.Parent }
            BackHandler { back() }
            if (aiKey.isBlank()) {
                LaunchedEffect(r) { route = Route.Parent }
            } else {
                ShareKeyScreen(aiKey, onBack = back)
            }
        }

        is Route.EvalResult -> {
            BackHandler { route = Route.Main(Tab.PLATEAU) }
            EvalResultScreen(
                r.ui,
                onNext = r.nextGenreId?.let { id -> { route = Route.GenreRoute(id) } },
                onRetry = { route = Route.Eval(r.genreId) },
                onHome = { route = Route.Main(Tab.PLATEAU) },
            )
        }

        is Route.Result -> {
            BackHandler { route = Route.Main(Tab.PLATEAU) }
            ResultScreen(
                r.ui,
                onNext = r.nextSceneId?.let { id -> { route = Route.Play(id) } },
                onHome = { route = Route.Main(Tab.PLATEAU) },
            )
        }
    }
}

private const val AI_MAX_TURNS = 10

/** Déroulé d'une scène improvisée : micro → IA → réponse du personnage, puis bilan. */
@Composable
private fun AiHost(
    key: Long,
    scenario: AiScenario,
    profile: Profile,
    apiKey: String,
    model: String,
    narrator: Narrator,
    tools: RepeatTools,
    onQuit: () -> Unit,
    onDone: (AiReview, Int) -> Unit,
) {
    var pending by remember(key) { mutableStateOf<String?>(null) }
    var pendingAlts by remember(key) { mutableStateOf(listOf<String>()) }
    var practice by remember(key) { mutableStateOf<String?>(null) }
    practice?.let { t -> RepeatDialog(t, tools) { practice = null } }
    val scope = rememberCoroutineScope()
    val lines = remember(key) { mutableStateListOf(ChatLine(false, scenario.opening, AiTurn(scenario.opening, "happy"))) }
    var done by remember(key) { mutableStateOf(setOf<Int>()) }
    var loading by remember(key) { mutableStateOf(false) }
    var error by remember(key) { mutableStateOf<String?>(null) }
    var hint by remember(key) { mutableStateOf<String?>(null) }
    var mood by remember(key) { mutableStateOf("happy") }
    var reviewing by remember(key) { mutableStateOf(false) }
    var finished by remember(key) { mutableStateOf(false) }
    val learnerTurns = lines.count { it.fromLearner }
    val say = { text: String -> narrator.speak(text, voiceName = profile.voice, pitch = scenario.character.pitch) }

    fun review() {
        reviewing = true
        scope.launch {
            loading = true
            error = null
            try {
                val result = Mistral.review(apiKey, model, scenario, profile.level, lines.toList(), done.size)
                onDone(result, done.size)
            } catch (e: AiError) {
                error = e.messageFr
            } finally {
                loading = false
            }
        }
    }

    fun send() {
        scope.launch {
            loading = true
            error = null
            hint = null
            try {
                val turn = Mistral.nextTurn(apiKey, model, scenario, profile.level, profile.name, lines.toList())
                lines.add(ChatLine(false, turn.reply, turn))
                done = done + turn.objectivesDone.filter { it in scenario.objectivesEn.indices }
                mood = turn.mood
                say(turn.reply)
                if (turn.finished || lines.count { it.fromLearner } >= AI_MAX_TURNS) finished = true
            } catch (e: AiError) {
                error = e.messageFr
            } finally {
                loading = false
            }
        }
    }

    val listen = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val alts = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).orEmpty().filter { it.isNotBlank() }
        if (alts.isNotEmpty()) {
            pending = alts.first()
            pendingAlts = alts.drop(1).distinct().take(2)
        }
    }

    fun startMic() {
        narrator.stop()
        hint = null
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-GB")
            .putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak in English")
            .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        try {
            listen.launch(intent)
        } catch (e: ActivityNotFoundException) {
            error = "La reconnaissance vocale n'est pas disponible. Installe ou active l'appli Google sur ce téléphone."
        }
    }

    LaunchedEffect(key) {
        delay(400)
        say(scenario.opening)
    }
    // Fin naturelle : on laisse le personnage finir sa phrase, puis on passe au bilan.
    LaunchedEffect(finished) {
        if (finished && !reviewing) {
            delay(800)
            while (narrator.speaking) delay(200)
            review()
        }
    }

    BackHandler { onQuit() }

    AiDialogueScreen(
        ui = AiUi(
            scenario = scenario,
            lines = lines.toList(),
            objectivesDone = done,
            loading = loading,
            error = error,
            hint = hint,
            mood = mood,
            speaking = narrator.speaking,
            turnsLeft = AI_MAX_TURNS - learnerTurns,
            pending = pending,
        ),
        animate = true,
        onMic = { startMic() },
        onHint = {
            val last = lines.lastOrNull { !it.fromLearner }?.turn?.hintFr.orEmpty()
            hint = last.ifBlank { "Réponds à la question de ${scenario.character.name} avec une phrase simple. Tu peux commencer par « I… » ou « Yes, … »." }
        },
        onReplay = { say(it) },
        onRetry = { if (reviewing) review() else if (lines.lastOrNull()?.fromLearner == true) send() else error = null },
        onFinish = { narrator.stop(); review() },
        onQuit = onQuit,
        onSend = { text, edited ->
            val heard = pending
            lines.add(ChatLine(true, text, alts = if (edited) emptyList() else pendingAlts, heard = if (edited) heard else null))
            pending = null
            pendingAlts = emptyList()
            send()
        },
        onRedo = { pending = null; startMic() },
        onPractice = { practice = it },
    )
}

private fun sampleLine(p: Profile): String =
    "Hello ${p.name}! Welcome to Ciné Vogue. Are you ready for your next scene?"

/** Sépare les identifiants des questions réussies et ratées. */
private fun split(answers: List<Pair<Question, Int?>>): Pair<List<String>, List<String>> =
    answers.filter { it.second == it.first.answer }.map { it.first.id } to
        answers.filter { it.second != it.first.answer }.map { it.first.id }

private val MY_TIP = Tip(
    "Ton carnet de répliques",
    "Regarder un film en VO avec les sous-titres anglais, c'est le meilleur entraînement. Note les répliques qui te plaisent : elles reviendront en « Prise 2 » dans tes scènes.",
)

@Composable
private fun SceneHost(
    key: String,
    build: (Random) -> List<Question>,
    header: String,
    subtitle: String,
    profile: Profile,
    narrator: Narrator,
    tools: RepeatTools,
    exam: Boolean = false,
    onQuit: () -> Unit,
    onFinish: (List<Pair<Question, Int?>>) -> Unit,
) {
    var repeat by remember(key) { mutableStateOf<String?>(null) }
    repeat?.let { t -> RepeatDialog(t, tools) { repeat = null } }
    val questions = remember(key) { build(Random(System.nanoTime())) }
    var index by remember(key) { mutableIntStateOf(0) }
    var selected by remember(key) { mutableStateOf<Int?>(null) }
    var checked by remember(key) { mutableStateOf(false) }
    var eliminated by remember(key) { mutableStateOf(setOf<Int>()) }
    var seconds by remember(key) { mutableIntStateOf(10) }
    val answers = remember(key) { mutableStateListOf<Pair<Question, Int?>>() }

    val q = questions[index]
    val say = { text: String, slow: Boolean -> narrator.speak(text, slow, profile.voice) }

    fun check() {
        if (checked) return
        checked = true
        answers.add(q to selected)
    }

    fun next() {
        if (index + 1 < questions.size) {
            index += 1
            selected = null
            checked = false
            eliminated = emptySet()
        } else {
            onFinish(answers.toList())
        }
    }

    BackHandler { onQuit() }

    LaunchedEffect(key, index) {
        seconds = 10
        if (q.game == Game.CASTING) {
            delay(500)
            q.audio?.let { say(it, false) }
        }
        if (q.game == Game.CLAP && exam) {
            q.audio?.let { say(it, false) }
        } else if (q.game == Game.CLAP) {
            q.audio?.let { say(it, false) }
            while (seconds > 0 && !checked) {
                delay(1000)
                if (!checked) seconds -= 1
            }
            if (!checked) check()
        }
    }

    SceneScreen(
        ui = QuestionUi(
            q = q,
            number = index + 1,
            total = questions.size,
            selected = selected,
            checked = checked,
            eliminated = eliminated,
            secondsLeft = if (q.game == Game.CLAP && !exam) seconds else null,
            header = header,
            genreTitle = subtitle,
            feminine = profile.gender == Gender.FILLE,
            exam = exam,
        ),
        onSelect = { if (!checked && it !in eliminated) selected = it },
        onAction = {
            if (exam) {
                // Avant-première : on enregistre et on passe à la suite, sans correction.
                if (selected != null) {
                    answers.add(q to selected)
                    next()
                }
            } else if (!checked) {
                if (selected != null) check()
            } else {
                next()
            }
        },
        onHint = {
            if (!checked && eliminated.isEmpty()) {
                val wrongOptions = q.options.indices.filter { it != q.answer && it != selected }
                if (wrongOptions.isNotEmpty()) eliminated = setOf(wrongOptions.random())
            }
        },
        onListen = { slow -> q.audio?.let { say(it, slow) } },
        onQuit = onQuit,
        onRepeat = { repeat = it },
    )
}
