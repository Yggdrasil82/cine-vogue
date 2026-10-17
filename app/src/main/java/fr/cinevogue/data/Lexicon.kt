package fr.cinevogue.data

import java.util.Locale

/**
 * Petit dictionnaire construit à partir de tout le contenu de l'appli
 * (mots des scènes et mots traduits sous chaque réplique).
 */
object Lexicon {
    private val entries: Map<String, String> by lazy {
        val map = linkedMapOf<String, String>()
        ALL_GENRES.forEach { g ->
            g.scenes.forEach { s ->
                s.words.forEach { add(map, it.en, it.fr) }
                s.lines.forEach { line ->
                    line.gloss.split(" · ").forEach { part ->
                        val bits = part.split(" = ", limit = 2)
                        if (bits.size == 2) add(map, bits[0], bits[1])
                    }
                }
            }
        }
        map
    }

    private fun add(map: MutableMap<String, String>, en: String, fr: String) {
        val key = en.trim().lowercase(Locale.ROOT)
        if (key.isEmpty() || key.contains("…") || key.contains("(")) return
        if (key !in map) map[key] = fr.trim()
    }

    /** Les mots connus de l'appli présents dans une phrase, les expressions les plus longues d'abord. */
    fun known(sentence: String, max: Int = 6): List<Pair<String, String>> {
        val text = " " + sentence.lowercase(Locale.ROOT).replace(Regex("[^a-z' ]"), " ").replace(Regex("\\s+"), " ") + " "
        val found = mutableListOf<Pair<String, String>>()
        var rest = text
        entries.keys.sortedByDescending { it.length }.forEach { key ->
            if (found.size >= max) return@forEach
            val needle = " $key "
            if (rest.contains(needle)) {
                found += key to entries.getValue(key)
                rest = rest.replace(needle, " # ")
            }
        }
        return found
    }
}
