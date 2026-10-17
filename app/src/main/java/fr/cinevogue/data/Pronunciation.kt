package fr.cinevogue.data

import java.util.Locale

/** Un mot de la phrase à prononcer, et s'il a été compris par le téléphone. */
data class WordCheck(val word: String, val ok: Boolean)

data class PronunciationResult(val words: List<WordCheck>, val heard: String) {
    val score: Int get() = if (words.isEmpty()) 0 else words.count { it.ok } * 100 / words.size
    val missed: List<String> get() = words.filter { !it.ok }.map { it.word }
}

/**
 * Compare la phrase attendue avec ce que la reconnaissance vocale a compris,
 * mot à mot, et donne des conseils pour les pièges des francophones.
 */
object Pronunciation {

    private val CONTRACTIONS = mapOf(
        "i'm" to "i am", "you're" to "you are", "he's" to "he is", "she's" to "she is", "it's" to "it is",
        "we're" to "we are", "they're" to "they are", "that's" to "that is", "what's" to "what is",
        "where's" to "where is", "who's" to "who is", "there's" to "there is", "here's" to "here is",
        "let's" to "let us", "don't" to "do not", "doesn't" to "does not", "didn't" to "did not",
        "can't" to "cannot", "won't" to "will not", "isn't" to "is not", "aren't" to "are not",
        "wasn't" to "was not", "weren't" to "were not", "haven't" to "have not", "hasn't" to "has not",
        "hadn't" to "had not", "mustn't" to "must not", "couldn't" to "could not", "shouldn't" to "should not",
        "wouldn't" to "would not", "needn't" to "need not", "i'll" to "i will", "you'll" to "you will",
        "he'll" to "he will", "she'll" to "she will", "we'll" to "we will", "they'll" to "they will",
        "i've" to "i have", "you've" to "you have", "we've" to "we have", "they've" to "they have",
        "i'd" to "i would", "you'd" to "you would", "we'd" to "we would", "she'd" to "she would",
        "he'd" to "he would", "they'd" to "they would",
    )

    private val UNITS = listOf("zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
        "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen")
    private val TENS = listOf("", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety")

    private fun numberWords(n: Int): List<String> = when {
        n < 20 -> listOf(UNITS[n])
        n < 100 -> listOf(TENS[n / 10]) + if (n % 10 != 0) listOf(UNITS[n % 10]) else emptyList()
        else -> listOf(n.toString())
    }

    /** Transforme un mot affiché en mots « normalisés » comparables. */
    fun tokens(word: String): List<String> {
        val w = word.lowercase(Locale.ROOT).replace('’', '\'').trim { !it.isLetterOrDigit() && it != '\'' }
        if (w.isEmpty()) return emptyList()
        CONTRACTIONS[w]?.let { return it.split(" ") }
        w.toIntOrNull()?.let { return numberWords(it) }
        val clean = w.removeSuffix("'s").replace("'", "").replace(Regex("[^a-z0-9]"), " ").trim()
        return if (clean.isEmpty()) emptyList() else clean.split(Regex("\\s+")).flatMap { t ->
            t.toIntOrNull()?.let { numberWords(it) } ?: listOf(t)
        }
    }

    private fun normalize(sentence: String): List<String> = sentence.split(Regex("\\s+")).flatMap { tokens(it) }

    /** Compare une phrase avec les propositions de la reconnaissance vocale (la meilleure est gardée). */
    fun check(target: String, heardAlternatives: List<String>): PronunciationResult {
        val display = target.split(Regex("\\s+")).filter { tokens(it).isNotEmpty() }
        val owners = mutableListOf<Int>()
        val targetTokens = mutableListOf<String>()
        display.forEachIndexed { i, w -> tokens(w).forEach { targetTokens += it; owners += i } }

        var best: PronunciationResult? = null
        var bestMatched = -1
        for (heard in heardAlternatives.ifEmpty { listOf("") }) {
            val h = normalize(heard)
            val matched = lcsMatches(targetTokens, h)
            val okTokens = BooleanArray(targetTokens.size).also { arr -> matched.forEach { arr[it] = true } }
            val words = display.mapIndexed { i, w ->
                val idx = owners.indices.filter { owners[it] == i }
                WordCheck(w.trim { !it.isLetterOrDigit() && it != '\'' && it != '’' }, idx.isNotEmpty() && idx.all { okTokens[it] })
            }
            if (matched.size > bestMatched) {
                bestMatched = matched.size
                best = PronunciationResult(words, heard)
            }
        }
        return best ?: PronunciationResult(emptyList(), "")
    }

    /** Indices des mots attendus retrouvés dans l'ordre (plus longue sous-suite commune). */
    private fun lcsMatches(a: List<String>, b: List<String>): List<Int> {
        val dp = Array(a.size + 1) { IntArray(b.size + 1) }
        for (i in a.indices.reversed()) for (j in b.indices.reversed()) {
            dp[i][j] = if (a[i] == b[j]) dp[i + 1][j + 1] + 1 else maxOf(dp[i + 1][j], dp[i][j + 1])
        }
        val out = mutableListOf<Int>()
        var i = 0
        var j = 0
        while (i < a.size && j < b.size) {
            when {
                a[i] == b[j] -> { out += i; i++; j++ }
                dp[i + 1][j] >= dp[i][j + 1] -> i++
                else -> j++
            }
        }
        return out
    }

    /** Conseils pour les pièges classiques des francophones (au plus 2 par mot). */
    fun tips(word: String): List<String> {
        val w = word.lowercase(Locale.ROOT).replace('’', '\'')
        val out = mutableListOf<String>()
        if ("th" in w) out += "« th » : place le bout de la langue entre les dents et souffle doucement (ni « s », ni « z », ni « f »)."
        if (w.startsWith("h") && w !in setOf("hour", "honest", "honour", "honor", "heir")) out += "Le « h » se prononce : souffle comme pour faire de la buée sur une vitre."
        if (w.startsWith("kn")) out += "Le « k » de « kn » est muet : « know » se dit « nô »."
        if (w.startsWith("wr")) out += "Le « w » de « wr » est muet : « write » se dit « raït »."
        if (w.endsWith("mb")) out += "Le « b » final est muet : « climb » se dit « claïm »."
        if (w.endsWith("ed") && w.length > 3) out += "Terminaison « -ed » : « t » après p, k, s, ch, sh, f (worked = « workt ») ; « id » après t ou d (wanted) ; sinon « d » (played)."
        if (w.endsWith("ing")) out += "« -ing » : un son nasal sans « g » dur à la fin, comme dans « parking » dit vite."
        if (w.startsWith("w") && !w.startsWith("wr") && !w.startsWith("who")) out += "« w » : arrondis les lèvres comme pour « ou » : « ouindo » pour window."
        if ("ee" in w || "ea" in w) out += "« ee » ou « ea » : un « i » long et tendu (sheep), plus long que le « i » court de « ship »."
        if (Regex("[^aeiou]i[^aeioue]").containsMatchIn("#$w#") && "ee" !in w && "ea" !in w) out += "« i » court (sit, ship) : entre « i » et « é », très bref."
        if ("r" in w && out.size < 2) out += "« r » anglais : ne le roule pas et ne racle pas la gorge ; la langue recule sans toucher le palais."
        if (out.isEmpty()) out += "Écoute le mot lentement, découpe-le en syllabes et appuie sur la syllabe forte, puis redis-le d'une traite."
        return out.take(2)
    }
}
