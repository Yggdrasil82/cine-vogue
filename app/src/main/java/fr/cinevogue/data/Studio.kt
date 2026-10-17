package fr.cinevogue.data

import kotlin.random.Random

enum class Game(val label: String) {
    DOUBLAGE("Le doublage"),
    SCRIPT("Le script troué"),
    CASTING("Le casting"),
    CLAP("Clap de fin"),
}

data class Question(
    val id: String,
    val game: Game,
    val speaker: String?,
    /** Texte affiché : réplique, réplique trouée ou mot. */
    val text: String,
    /** Traduction affichée sous le script troué. */
    val subtitle: String?,
    /** Texte lu par la voix. */
    val audio: String?,
    val options: List<String>,
    val answer: Int,
    /** Correction : la phrase complète et sa traduction. */
    val translation: String,
    /** Les mots importants traduits. */
    val gloss: String?,
    /** Pourquoi c'est la bonne réponse (script troué). */
    val why: String?,
    /** Ce que veut dire chaque mauvaise réponse, si elle est choisie. */
    val optionNotes: List<String?>,
    val retake: Boolean = false,
)

/** Fabrique les questions de chaque scène. */
object Studio {

    const val BLANK = "_____"

    fun find(sceneId: String): Pair<Genre, Scene>? {
        for (g in ALL_GENRES) for (s in g.scenes) if (s.id == sceneId) return g to s
        return null
    }

    fun sceneNumber(genre: Genre, scene: Scene): Int = genre.scenes.indexOf(scene) + 1

    private fun pick(correct: String, pool: List<String>, rnd: Random): Pair<List<String>, Int> {
        val wrong = pool.filter { it != correct }.distinct().shuffled(rnd).take(2)
        val options = (wrong + correct).shuffled(rnd)
        return options to options.indexOf(correct)
    }

    fun blanked(line: Line): String {
        val blank = line.blank ?: return line.en
        return Regex("\\b" + Regex.escape(blank) + "\\b").replaceFirst(line.en, BLANK)
    }

    private fun full(line: Line) = "« ${line.en} »\n= « ${line.fr} »"

    /** Toutes les questions possibles d'une scène. */
    fun candidates(genre: Genre, scene: Scene, rnd: Random): List<Question> {
        val lines = genre.scenes.flatMap { it.lines }
        val words = genre.scenes.flatMap { it.words }
        val enOfFr = lines.associate { it.fr to it.en }
        val frOfEn = lines.associate { it.en to it.fr }
        val enOfWord = words.associate { it.fr to it.en }
        val out = mutableListOf<Question>()

        for (i in listOf(0, 2)) {
            val line = scene.lines[i]
            val (opts, ans) = pick(line.fr, lines.map { it.fr }, rnd)
            out += Question(
                id = "${scene.id}-d$i", game = Game.DOUBLAGE, speaker = line.speaker,
                text = line.en, subtitle = null, audio = line.en, options = opts, answer = ans,
                translation = full(line), gloss = line.gloss, why = null,
                optionNotes = opts.map { o -> if (o == line.fr) null else enOfFr[o]?.let { "« $o » se dirait plutôt « $it »." } },
            )
        }
        for (i in listOf(1, 3)) {
            val line = scene.lines[i]
            val blank = line.blank ?: continue
            val (opts, ans) = pick(blank, line.decoys + blank, rnd)
            out += Question(
                id = "${scene.id}-s$i", game = Game.SCRIPT, speaker = line.speaker,
                text = blanked(line), subtitle = line.fr, audio = line.en, options = opts, answer = ans,
                translation = full(line), gloss = line.gloss, why = line.why,
                optionNotes = opts.map { null },
            )
        }
        run {
            val line = scene.lines[3]
            val (opts, ans) = pick(line.en, lines.map { it.en }, rnd)
            out += Question(
                id = "${scene.id}-k3", game = Game.CASTING, speaker = line.speaker,
                text = line.en, subtitle = null, audio = line.en, options = opts, answer = ans,
                translation = full(line), gloss = line.gloss, why = null,
                optionNotes = opts.map { o -> if (o == line.en) null else frOfEn[o]?.let { "Tu as choisi « $o », qui veut dire « $it »." } },
            )
        }
        scene.words.forEachIndexed { i, word ->
            val (opts, ans) = pick(word.fr, words.map { it.fr }, rnd)
            out += Question(
                id = "${scene.id}-c$i", game = Game.CLAP, speaker = null,
                text = word.en, subtitle = null, audio = word.en, options = opts, answer = ans,
                translation = "${word.en} = ${word.fr}", gloss = null, why = null,
                optionNotes = opts.map { o -> if (o == word.fr) null else enOfWord[o]?.let { "« $o » se dit « $it »." } },
            )
        }
        return out
    }

    fun question(id: String, rnd: Random, myLines: List<MyLine> = emptyList(), pool: List<Genre> = emptyList()): Question? {
        if (id.startsWith(MY_PREFIX)) {
            val lineId = id.removePrefix(MY_PREFIX).substringBeforeLast('-')
            val line = myLines.firstOrNull { it.id == lineId } ?: return null
            return myQuestion(line, id.substringAfterLast('-'), myLines, pool, rnd)
        }
        val (g, s) = find(id.substringBeforeLast('-')) ?: return null
        return candidates(g, s, rnd).firstOrNull { it.id == id }
    }

    const val MY_PREFIX = "my-"
    const val MY_SCENE = "my"

    fun isMine(q: Question) = q.id.startsWith(MY_PREFIX)

    /** Les mots connus de l'appli dans une réplique, prêts à afficher. */
    fun knownWords(en: String): String? =
        Lexicon.known(en).takeIf { it.isNotEmpty() }?.joinToString(" · ") { "${it.first} = ${it.second}" }

    /** Question à partir d'une réplique notée : « d » = doublage, « k » = casting. */
    private fun myQuestion(line: MyLine, kind: String, myLines: List<MyLine>, pool: List<Genre>, rnd: Random): Question {
        val others = myLines.filter { it.id != line.id }
        val content = pool.flatMap { g -> g.scenes.flatMap { it.lines } }
        val enOfFr = (content.map { it.fr to it.en } + myLines.map { it.fr to it.en }).toMap()
        val frOfEn = (content.map { it.en to it.fr } + myLines.map { it.en to it.fr }).toMap()
        val translation = "« ${line.en} »\n= « ${line.fr} »"
        val gloss = knownWords(line.en)
        val film = line.film.ifBlank { null }?.let { "Vu dans : $it" }
        return if (kind == "k") {
            val (opts, ans) = pick(line.en, others.map { it.en } + content.map { it.en }, rnd)
            Question(
                id = "$MY_PREFIX${line.id}-k", game = Game.CASTING, speaker = null,
                text = line.en, subtitle = film, audio = line.en, options = opts, answer = ans,
                translation = translation, gloss = gloss, why = null,
                optionNotes = opts.map { o -> if (o == line.en) null else frOfEn[o]?.let { "Tu as choisi « $o », qui veut dire « $it »." } },
            )
        } else {
            val (opts, ans) = pick(line.fr, others.map { it.fr } + content.map { it.fr }, rnd)
            Question(
                id = "$MY_PREFIX${line.id}-d", game = Game.DOUBLAGE, speaker = null,
                text = line.en, subtitle = film, audio = line.en, options = opts, answer = ans,
                translation = translation, gloss = gloss, why = null,
                optionNotes = opts.map { o -> if (o == line.fr) null else enOfFr[o]?.let { "« $o » se dirait plutôt « $it »." } },
            )
        }
    }

    /**
     * Une scène = 2 doublages, 2 scripts troués, 1 casting,
     * jusqu'à 2 « Prise 2 » (répliques ratées avant ou répliques notées) puis 3 mots en clap de fin.
     * [pool] = thèmes et films du profil, pour ne reprendre que des questions de son niveau.
     */
    fun session(pool: List<Genre>, genre: Genre, scene: Scene, missed: Set<String>, myLines: List<MyLine>, rnd: Random): List<Question> {
        val all = candidates(genre, scene, rnd)
        val main = all.filter { it.game != Game.CLAP }
        val sameLevel = pool.flatMap { g -> g.scenes.map { it.id } }.toSet()
        val retakes = missed
            .filter { id ->
                if (id.startsWith(MY_PREFIX)) true
                else !id.startsWith(scene.id + "-") && id.substringBeforeLast('-') in sameLevel
            }
            .shuffled(rnd)
            .mapNotNull { question(it, rnd, myLines, pool)?.copy(retake = true) }
            .take(2)
            .toMutableList()
        if (retakes.size < 2 && myLines.isNotEmpty()) {
            val line = myLines.random(rnd)
            val q = myQuestion(line, "d", myLines, pool, rnd)
            if (retakes.none { it.id == q.id }) retakes += q.copy(retake = true)
        }
        val clap = all.filter { it.game == Game.CLAP }.shuffled(rnd).take(3)
        return main + retakes + clap
    }

    /** Révision de « Mes répliques » : jusqu'à 8 questions, doublage et casting mélangés. */
    fun mySession(myLines: List<MyLine>, pool: List<Genre>, rnd: Random): List<Question> =
        myLines.flatMap { line -> listOf("d", "k").map { myQuestion(line, it, myLines, pool, rnd) } }
            .shuffled(rnd)
            .take(8)
}
