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
import fr.cinevogue.data.GENRES
import fr.cinevogue.data.Game
import fr.cinevogue.data.POINTS_PER_ANSWER
import fr.cinevogue.data.Genre
import fr.cinevogue.data.Progress
import fr.cinevogue.data.ProgressStore
import fr.cinevogue.data.Scene
import fr.cinevogue.data.Studio
import fr.cinevogue.data.finishScene
import fr.cinevogue.data.nextToPlay
import fr.cinevogue.data.rankFor
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
import fr.cinevogue.ui.formatTime
import kotlinx.coroutines.delay
import java.time.LocalDate
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    private lateinit var store: ProgressStore
    private lateinit var voice: Voice

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = ProgressStore(this)
        voice = Voice(this)
        Reminder.sync(this, store.load())
        setContent {
            CineTheme {
                Box(Modifier.fillMaxSize().background(Cv.Bg)) {
                    CineApp(store, voice)
                }
            }
        }
    }

    override fun onDestroy() {
        voice.shutdown()
        super.onDestroy()
    }
}

private sealed interface Route {
    data class Main(val tab: Tab) : Route
    data class GenreRoute(val id: String) : Route
    data class Play(val sceneId: String) : Route
    data class Result(val ui: ResultUi, val nextSceneId: String?) : Route
}

private fun today(): Long = LocalDate.now().toEpochDay()

@Composable
private fun CineApp(store: ProgressStore, voice: Voice) {
    val context = LocalContext.current
    var progress by remember { mutableStateOf(store.load()) }
    var route by remember { mutableStateOf<Route>(Route.Main(Tab.PLATEAU)) }

    fun update(p: Progress) {
        progress = p
        store.save(p)
        Reminder.sync(context, p)
    }

    val askPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33 && !store.askedNotifications &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            store.askedNotifications = true
            askPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    when (val r = route) {
        is Route.Main -> {
            if (r.tab != Tab.PLATEAU) BackHandler { route = Route.Main(Tab.PLATEAU) }
            AppFrame(r.tab, onTab = { route = Route.Main(it) }) {
                when (r.tab) {
                    Tab.PLATEAU -> HomeScreen(
                        progress, today(),
                        onPlay = { route = Route.Play(it) },
                        onGenre = { route = Route.GenreRoute(it) },
                    )
                    Tab.TROPHEES -> TrophiesScreen(progress)
                    Tab.PROFIL -> ProfileScreen(
                        progress, today(),
                        voiceOk = voice.ukAvailable,
                        onReminder = { on -> update(progress.copy(reminderOn = on)) },
                        onPickTime = {
                            TimePickerDialog(
                                context,
                                { _, h, m -> update(progress.copy(reminderHour = h, reminderMinute = m)) },
                                progress.reminderHour, progress.reminderMinute, true,
                            ).show()
                        },
                        onTestVoice = { voice.speak("Hello! Welcome to Ciné Vogue. Are you ready for your first scene?") },
                    )
                }
            }
        }

        is Route.GenreRoute -> {
            val genre = GENRES.first { it.id == r.id }
            BackHandler { route = Route.Main(Tab.PLATEAU) }
            GenreScreen(genre, progress, onPlay = { route = Route.Play(it) }, onBack = { route = Route.Main(Tab.PLATEAU) })
        }

        is Route.Play -> {
            val found = Studio.find(r.sceneId)
            if (found == null) {
                LaunchedEffect(r) { route = Route.Main(Tab.PLATEAU) }
            } else {
                val (genre, scene) = found
                SceneHost(
                    genre, scene, progress.missed, voice,
                    onQuit = { route = Route.Main(Tab.PLATEAU) },
                    onFinish = { correct, wrong ->
                        val before = rankFor(progress.points)
                        val (updated, stars) = finishScene(progress, scene.id, correct, wrong, today())
                        update(updated)
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
                                reminder = if (updated.reminderOn) "Prochain tournage demain à ${formatTime(updated.reminderHour, updated.reminderMinute)}" else null,
                                newRank = if (after != before) after.title else null,
                            ),
                            nextToPlay(updated)?.second?.id,
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

@Composable
private fun SceneHost(
    genre: Genre,
    scene: Scene,
    missed: Set<String>,
    voice: Voice,
    onQuit: () -> Unit,
    onFinish: (List<String>, List<String>) -> Unit,
) {
    val questions = remember(scene.id) { Studio.session(genre, scene, missed, Random(System.nanoTime())) }
    var index by remember(scene.id) { mutableIntStateOf(0) }
    var selected by remember(scene.id) { mutableStateOf<Int?>(null) }
    var checked by remember(scene.id) { mutableStateOf(false) }
    var eliminated by remember(scene.id) { mutableStateOf(setOf<Int>()) }
    var seconds by remember(scene.id) { mutableIntStateOf(10) }
    val correct = remember(scene.id) { mutableStateListOf<String>() }
    val wrong = remember(scene.id) { mutableStateListOf<String>() }

    val q = questions[index]

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
            q.audio?.let { voice.speak(it) }
        }
        if (q.game == Game.CLAP) {
            q.audio?.let { voice.speak(it) }
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
        onListen = { slow -> q.audio?.let { voice.speak(it, slow) } },
        onQuit = onQuit,
    )
}
