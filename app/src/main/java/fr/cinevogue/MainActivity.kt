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
import fr.cinevogue.data.Game
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
    if (current == null && r !is Route.Picker && r !is Route.Form) {
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
            val back = { route = if (editing != null) Route.Main(Tab.PROFIL) else Route.Picker }
            if (canCancel) BackHandler { back() }
            ProfileFormScreen(
                initial = editing,
                legacyNote = remember { repo.hasLegacyProgress() },
                onSave = { name, gender, level, color ->
                    if (editing == null) {
                        val created = repo.create(name, gender, level, color)
                        profiles = repo.profiles()
                        open(created)
                    } else {
                        saveProfile(editing.copy(name = name, gender = gender, level = level, color = color))
                        route = Route.Main(Tab.PROFIL)
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
            BackHandler { route = if (r.tab != Tab.PLATEAU) Route.Main(Tab.PLATEAU) else Route.Picker }
            AppFrame(r.tab, onTab = { route = Route.Main(it) }) {
                when (r.tab) {
                    Tab.PLATEAU -> HomeScreen(
                        prof, genres, progress, today(),
                        onPlay = { route = Route.Play(it) },
                        onGenre = { route = Route.GenreRoute(it) },
                        onSwitch = { route = Route.Picker },
                    )
                    Tab.TROPHEES -> TrophiesScreen(genres, progress, prof.gender)
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

        is Route.GenreRoute -> {
            val prof = current!!
            val genre = genresFor(prof.level).firstOrNull { it.id == r.id }
            if (genre == null) {
                LaunchedEffect(r) { route = Route.Main(Tab.PLATEAU) }
            } else {
                BackHandler { route = Route.Main(Tab.PLATEAU) }
                GenreScreen(genre, progress, onPlay = { route = Route.Play(it) }, onBack = { route = Route.Main(Tab.PLATEAU) })
            }
        }

        is Route.Play -> {
            val prof = current!!
            val genres = genresFor(prof.level)
            val found = Studio.find(r.sceneId)
            if (found == null) {
                LaunchedEffect(r) { route = Route.Main(Tab.PLATEAU) }
            } else {
                val (genre, scene) = found
                SceneHost(
                    genres, genre, scene, progress.missed, prof, narrator,
                    onQuit = { route = Route.Main(Tab.PLATEAU) },
                    onFinish = { correct, wrong ->
                        val before = rankFor(progress.points)
                        val (updated, stars) = finishScene(progress, scene.id, correct, wrong, today())
                        saveProgress(updated)
                        val after = rankFor(updated.points)
                        route = Route.Result(
                            ResultUi(
                                sceneTitle = scene.title,
                                correct = correct.size,
                                total = correct.size + wrong.size,
                                stars = stars,
                                points = correct.size * POINTS_PER_ANSWER,
                                words = scene.words,
                                tip = scene.tip,
                                reminder = if (settings.reminderOn) "Prochain tournage demain à ${formatTime(settings.reminderHour, settings.reminderMinute)}" else null,
                                newRank = if (after != before) after.title(prof.gender) else null,
                            ),
                            nextToPlay(genres, updated)?.second?.id,
                        )
                    },
                )
            }
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

private fun sampleLine(p: Profile): String =
    "Hello ${p.name}! Welcome to Ciné Vogue. Are you ready for your next scene?"

@Composable
private fun SceneHost(
    genres: List<Genre>,
    genre: Genre,
    scene: Scene,
    missed: Set<String>,
    profile: Profile,
    narrator: Narrator,
    onQuit: () -> Unit,
    onFinish: (List<String>, List<String>) -> Unit,
) {
    val questions = remember(scene.id) { Studio.session(genres, genre, scene, missed, Random(System.nanoTime())) }
    var index by remember(scene.id) { mutableIntStateOf(0) }
    var selected by remember(scene.id) { mutableStateOf<Int?>(null) }
    var checked by remember(scene.id) { mutableStateOf(false) }
    var eliminated by remember(scene.id) { mutableStateOf(setOf<Int>()) }
    var seconds by remember(scene.id) { mutableIntStateOf(10) }
    val correct = remember(scene.id) { mutableStateListOf<String>() }
    val wrong = remember(scene.id) { mutableStateListOf<String>() }

    val q = questions[index]
    val say = { text: String, slow: Boolean -> narrator.speak(text, slow, profile.voice) }

    fun check() {
        if (checked) return
        checked = true
        if (selected == q.answer) correct.add(q.id) else wrong.add(q.id)
    }

    BackHandler { onQuit() }

    LaunchedEffect(scene.id, index) {
        seconds = 10
        if (q.game == Game.CASTING) {
            delay(500)
            q.audio?.let { say(it, false) }
        }
        if (q.game == Game.CLAP) {
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
            secondsLeft = if (q.game == Game.CLAP) seconds else null,
            sceneNumber = Studio.sceneNumber(genre, scene),
            genreTitle = genre.title,
            feminine = profile.gender == Gender.FILLE,
        ),
        onSelect = { if (!checked && it !in eliminated) selected = it },
        onAction = {
            if (!checked) {
                if (selected != null) check()
            } else if (index + 1 < questions.size) {
                index += 1
                selected = null
                checked = false
                eliminated = emptySet()
            } else {
                onFinish(correct.toList(), wrong.toList())
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
    )
}
