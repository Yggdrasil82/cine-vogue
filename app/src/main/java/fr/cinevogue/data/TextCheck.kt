package fr.cinevogue.data

/**
 * Résultat d'une phrase écrite (sous-titrage, montage, rushes).
 * [words] = les mots attendus, marqués justes ou non ; [typos] = fautes de frappe tolérées (écrit → attendu).
 */
data class TextResult(val ok: Boolean, val typos: List<Pair<String, String>>, val words: List<WordCheck>)

/** Correction souple : majuscules, ponctuation et contractions ne comptent pas ; une lettre de travers est tolérée. */
object TextCheck {

    private fun split(s: String) = s.trim().split(Regex("\\s+")).filter { it.isNotBlank() }

    private fun tokens(s: String) = split(s).flatMap { Pronunciation.tokens(it) }

    fun check(typed: String, expected: String): TextResult {
        val t = tokens(typed)
        val e = tokens(expected)
        val display = split(expected).filter { Pronunciation.tokens(it).isNotEmpty() }
        val allOk = display.map { WordCheck(clean(it), true) }
        if (t.isEmpty()) return TextResult(false, emptyList(), display.map { WordCheck(clean(it), false) })
        if (t == e) return TextResult(true, emptyList(), allOk)
        if (t.size == e.size) {
            val diffs = t.indices.filter { t[it] != e[it] }
            val tolerated = diffs.all { e[it].length >= 4 && levenshtein(t[it], e[it]) <= 1 }
            if (diffs.size == 1 && tolerated) return TextResult(true, diffs.map { t[it] to e[it] }, allOk)
        }
        return TextResult(false, emptyList(), Pronunciation.check(expected, listOf(typed)).words)
    }

    private fun clean(w: String) = w.trim { !it.isLetterOrDigit() && it != '\'' && it != '’' }

    /** Distance d'édition (une lettre ajoutée, oubliée, changée, ou deux lettres inversées = 1). */
    fun levenshtein(a: String, b: String): Int {
        val d = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) d[i][0] = i
        for (j in 0..b.length) d[0][j] = j
        for (i in 1..a.length) for (j in 1..b.length) {
            val cost = if (a[i - 1] == b[j - 1]) 0 else 1
            d[i][j] = minOf(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + cost)
            if (i > 1 && j > 1 && a[i - 1] == b[j - 2] && a[i - 2] == b[j - 1]) d[i][j] = minOf(d[i][j], d[i - 2][j - 2] + 1)
        }
        return d[a.length][b.length]
    }

    /** Les mots d'une phrase, sans la ponctuation, pour le montage. */
    fun tiles(sentence: String): List<String> = split(sentence).map { clean(it) }.filter { it.isNotEmpty() }
}
