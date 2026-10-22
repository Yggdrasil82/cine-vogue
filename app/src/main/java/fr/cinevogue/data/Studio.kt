package fr.cinevogue.data

import kotlin.random.Random

enum class Game(val label: String) {
    DOUBLAGE("Le doublage"),
    SCRIPT("Le script troué"),
    CASTING("Le casting"),
    CLAP("Clap de fin"),
    SOUSTITRAGE("Le sous-titrage"),
    MONTAGE("Le montage"),
    RUSHES("Les rushes"),
}

/** Comment on répond : choisir, écrire, ou remettre des étiquettes dans l'ordre. */
enum class Mode { CHOICE, TYPE, TILES }

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
    val mode: Mode = Mode.CHOICE,
    /** Phrase anglaise attendue (écrire ou montage). */
    val expected: String? = null,
    /** Étiquettes : mots mélangés du montage, ou aide « mots en désordre » du sous-titrage. */
    val tiles: List<String> = emptyList(),
    /** Consigne propre à la question (rushes). */
    val instruction: String? = null,
    /** Règle à revoir (phrases de l'avant-première). */
    val tip: Tip? = null,
    /** Rushes : « enfr », « fren », « listen » ou « write ». */
    val kind: String? = null,
) {
    val isText: Boolean get() = mode != Mode.CHOICE
}

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
        out += montage("${scene.id}-m0", scene.lines[0], rnd)
        out += subtitling("${scene.id}-t2", scene.lines[2], lines, rnd)
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

    /** Montage : remettre dans l'ordre les mots d'une réplique. */
    private fun montage(id: String, line: Line, rnd: Random, tip: Tip? = null): Question {
        val words = TextCheck.tiles(line.en)
        var shuffled = words.shuffled(rnd)
        var tries = 0
        while (words.size > 1 && shuffled == words && tries++ < 8) shuffled = words.shuffled(rnd)
        return Question(
            id = id, game = Game.MONTAGE, speaker = line.speaker.ifBlank { null },
            text = line.fr, subtitle = null, audio = line.en, options = emptyList(), answer = -1,
            translation = full(line), gloss = line.gloss.ifBlank { null }, why = null, optionNotes = emptyList(),
            mode = Mode.TILES, expected = line.en, tiles = shuffled, tip = tip,
        )
    }

    /** Sous-titrage : écrire en anglais une réplique donnée en français. */
    private fun subtitling(id: String, line: Line, others: List<Line>, rnd: Random, tip: Tip? = null): Question {
        val words = TextCheck.tiles(line.en)
        val lower = words.map { it.lowercase() }.toSet()
        val decoys = others.flatMap { TextCheck.tiles(it.en) }
            .filter { it.lowercase() !in lower && it.length > 1 }
            .distinct().shuffled(rnd).take(2)
        return Question(
            id = id, game = Game.SOUSTITRAGE, speaker = line.speaker.ifBlank { null },
            text = line.fr, subtitle = null, audio = line.en, options = emptyList(), answer = -1,
            translation = full(line), gloss = line.gloss.ifBlank { null }, why = line.why, optionNotes = emptyList(),
            mode = Mode.TYPE, expected = line.en, tiles = (words + decoys).shuffled(rnd), tip = tip,
        )
    }

    private val BANK_ID = Regex("^(.+)-e(\\d+)-([sdktm])$")

    /** Question de l'avant-première à partir d'une phrase de la banque. */
    private fun bankQuestion(genre: Genre, index: Int, code: Char, rnd: Random): Question? {
        val bank = EVAL_BANK[genre.id] ?: return null
        val item = bank.getOrNull(index) ?: return null
        val line = item.line
        val tip = genre.scenes.getOrNull(item.scene)?.tip
        val id = "${genre.id}-e$index-$code"
        val others = bank.map { it.line }
        val enOfFr = others.associate { it.fr to it.en }
        val frOfEn = others.associate { it.en to it.fr }
        val gloss = line.gloss.ifBlank { null }
        return when (code) {
            's' -> {
                val blank = line.blank ?: return null
                val (opts, ans) = pick(blank, line.decoys + blank, rnd)
                Question(id, Game.SCRIPT, null, blanked(line), line.fr, line.en, opts, ans, full(line), gloss, line.why, opts.map { null }, tip = tip)
            }
            'd' -> {
                val (opts, ans) = pick(line.fr, others.map { it.fr }, rnd)
                Question(id, Game.DOUBLAGE, null, line.en, null, line.en, opts, ans, full(line), gloss, line.why,
                    opts.map { o -> if (o == line.fr) null else enOfFr[o]?.let { "« $o » se dirait plutôt « $it »." } }, tip = tip)
            }
            'k' -> {
                val (opts, ans) = pick(line.en, others.map { it.en }, rnd)
                Question(id, Game.CASTING, null, line.en, null, line.en, opts, ans, full(line), gloss, line.why,
                    opts.map { o -> if (o == line.en) null else frOfEn[o]?.let { "Tu as choisi « $o », qui veut dire « $it »." } }, tip = tip)
            }
            't' -> subtitling(id, line, others, rnd, tip)
            'm' -> montage(id, line, rnd, tip)
            else -> null
        }
    }

    fun question(id: String, rnd: Random, myLines: List<MyLine> = emptyList(), pool: List<Genre> = emptyList()): Question? {
        BANK_ID.matchEntire(id)?.let { m ->
            val genre = ALL_GENRES.firstOrNull { it.id == m.groupValues[1] } ?: return null
            return bankQuestion(genre, m.groupValues[2].toInt(), m.groupValues[3][0], rnd)
        }
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
     * Une scène = 2 doublages, 2 scripts troués, 1 casting, 1 montage, 1 sous-titrage,
     * jusqu'à 2 « Prise 2 » (répliques ratées avant ou répliques notées) puis 2 mots en clap de fin.
     * [pool] = thèmes et films du profil, pour ne reprendre que des questions de son niveau.
     */
    fun session(pool: List<Genre>, genre: Genre, scene: Scene, missed: Set<String>, myLines: List<MyLine>, rnd: Random): List<Question> {
        val all = candidates(genre, scene, rnd)
        val main = all.filter { it.game != Game.CLAP }
        val sameLevel = pool.flatMap { g -> g.scenes.map { it.id } }.toSet()
        val levelGenres = pool.map { it.id }.toSet()
        val retakes = missed
            .filter { id ->
                val bank = BANK_ID.matchEntire(id)
                when {
                    id.startsWith(MY_PREFIX) -> true
                    bank != null -> bank.groupValues[1] in levelGenres
                    else -> !id.startsWith(scene.id + "-") && id.substringBeforeLast('-') in sameLevel
                }
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
        val clap = all.filter { it.game == Game.CLAP }.shuffled(rnd).take(2)
        return main + retakes + clap
    }

    /** Nombre de questions de l'avant-première. */
    const val EVAL_SIZE = 12

    /**
     * L'avant-première d'un thème : 12 phrases nouvelles tirées au hasard dans la banque du thème,
     * en script troué, doublage, casting, sous-titrage et montage.
     */
    fun evaluation(genre: Genre, rnd: Random): List<Question> {
        val bank = EVAL_BANK[genre.id]
        if (bank.isNullOrEmpty()) {
            return genre.scenes.flatMap { s -> candidates(genre, s, rnd).filter { it.game == Game.SCRIPT } }.shuffled(rnd).take(EVAL_SIZE)
        }
        val codes = "sssddkkttmms".toList().shuffled(rnd)
        return bank.indices.shuffled(rnd).take(EVAL_SIZE)
            .mapIndexedNotNull { n, i -> bankQuestion(genre, i, codes[n % codes.size], rnd) }
    }

    /** Les rushes : mini-révision des mots de la scène (4 mots de la scène + 2 mots des répliques). */
    fun rushes(genre: Genre, scene: Scene, rnd: Random): List<Question> {
        data class Item(val id: String, val en: String, val fr: String)
        val known = scene.words.map { it.en.lowercase() }.toSet()
        val glossWords = scene.lines.flatMap { line ->
            line.gloss.split(" · ").mapNotNull { part ->
                val p = part.split(" = ")
                if (p.size != 2) null else p[0].trim() to p[1].trim()
            }
        }.filter { (en, fr) ->
            en.isNotEmpty() && fr.isNotEmpty() && en.lowercase() !in known &&
                en.split(" ").size <= 2 && fr.length <= 32 && !en.contains('(') && !en.contains('…')
        }.distinctBy { it.first.lowercase() }
        val items = scene.words.mapIndexed { i, w -> Item("${scene.id}-c$i", w.en, w.fr) } +
            glossWords.shuffled(rnd).take(2).mapIndexed { j, (en, fr) -> Item("${scene.id}-g$j", en, fr) }
        val frPool = genre.scenes.flatMap { it.words }.map { it.fr } + glossWords.map { it.second }
        val enPool = genre.scenes.flatMap { it.words }.map { it.en } + glossWords.map { it.first }
        val kinds = listOf("enfr", "fren", "listen", "write", "fren", "write")
        return items.shuffled(rnd).mapIndexed { n, item ->
            val kind = kinds[n % kinds.size]
            val translation = "${item.en} = ${item.fr}"
            when (kind) {
                "enfr" -> pick(item.fr, frPool + item.fr, rnd).let { (opts, ans) ->
                    Question(item.id, Game.RUSHES, null, item.en, null, item.en, opts, ans, translation, null, null, opts.map { null },
                        instruction = "Que veut dire ce mot ?", kind = kind)
                }
                "fren" -> pick(item.en, enPool + item.en, rnd).let { (opts, ans) ->
                    Question(item.id, Game.RUSHES, null, item.fr, null, item.en, opts, ans, translation, null, null, opts.map { null },
                        instruction = "Comment dit-on en anglais ?", kind = kind)
                }
                "listen" -> pick(item.en, enPool + item.en, rnd).let { (opts, ans) ->
                    Question(item.id, Game.RUSHES, null, "", null, item.en, opts, ans, translation, null, null, opts.map { null },
                        instruction = "Quel mot as-tu entendu ?", kind = kind)
                }
                else -> Question(item.id, Game.RUSHES, null, item.fr, null, item.en, emptyList(), -1, translation, null, null, emptyList(),
                    mode = Mode.TYPE, expected = item.en, instruction = "Écris ce mot en anglais :", kind = kind)
            }
        }
    }

    /** La règle de la scène d'où vient une question. */
    fun tipOf(q: Question): Tip? = q.tip ?: find(q.id.substringBeforeLast('-'))?.second?.tip

    /** Révision de « Mes répliques » : jusqu'à 8 questions, doublage et casting mélangés. */
    fun mySession(myLines: List<MyLine>, pool: List<Genre>, rnd: Random): List<Question> =
        myLines.flatMap { line -> listOf("d", "k").map { myQuestion(line, it, myLines, pool, rnd) } }
            .shuffled(rnd)
            .take(8)
}

/** Même question, options remélangées : pour la reposer tout de suite (rushes). */
fun Question.reshuffled(rnd: Random): Question = when (mode) {
    Mode.TILES -> copy(tiles = tiles.shuffled(rnd), retake = true)
    Mode.TYPE -> copy(tiles = tiles.shuffled(rnd), retake = true)
    Mode.CHOICE -> {
        val order = options.indices.shuffled(rnd)
        copy(
            options = order.map { options[it] },
            optionNotes = order.map { optionNotes.getOrNull(it) },
            answer = order.indexOf(answer),
            retake = true,
        )
    }
}
