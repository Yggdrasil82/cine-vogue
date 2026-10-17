package fr.cinevogue.data

data class Rank(val fille: String, val garcon: String, val min: Int) {
    fun title(g: Gender) = if (g == Gender.GARCON) garcon else fille
}

val RANKS = listOf(
    Rank("Figurante", "Figurant", 0),
    Rank("Second rôle", "Second rôle", 150),
    Rank("Premier rôle", "Premier rôle", 350),
    Rank("Vedette", "Vedette", 600),
    Rank("Star d'Hollywood", "Star d'Hollywood", 900),
)

data class Trophy(val title: String, val description: String, val earned: Boolean)

const val POINTS_PER_ANSWER = 5

fun rankFor(points: Int): Rank = RANKS.last { points >= it.min }

fun nextRank(points: Int): Rank? = RANKS.firstOrNull { it.min > points }

fun currentStreak(p: Progress, today: Long): Int = if (p.lastDay >= today - 1) p.streak else 0

fun sceneDone(p: Progress, sceneId: String): Boolean = (p.sceneStars[sceneId] ?: 0) > 0

fun scenesDone(g: Genre, p: Progress): Int = g.scenes.count { sceneDone(p, it.id) }

fun genreDone(g: Genre, p: Progress): Boolean = scenesDone(g, p) == g.scenes.size

/** Note à atteindre (sur 20) à l'avant-première pour débloquer le thème suivant. */
const val EVAL_PASS = 10

/** Note (sur 20) pour décrocher l'Oscar du thème. */
const val EVAL_OSCAR = 14

fun evalPassed(p: Progress, g: Genre): Boolean = (p.evalBest[g.id] ?: -1) >= EVAL_PASS

/**
 * Un thème s'ouvre quand l'avant-première du précédent est réussie.
 * Un thème déjà commencé (versions précédentes) reste ouvert.
 */
fun genreUnlocked(genres: List<Genre>, index: Int, p: Progress): Boolean =
    index == 0 || evalPassed(p, genres[index - 1]) || genres[index].scenes.any { sceneDone(p, it.id) }

/** Le premier thème terminé dont l'avant-première reste à réussir. */
fun pendingEval(genres: List<Genre>, p: Progress): Genre? =
    genres.withIndex().firstOrNull { (i, g) -> genreUnlocked(genres, i, p) && genreDone(g, p) && !evalPassed(p, g) }?.value

fun score20(correct: Int, total: Int): Int = if (total == 0) 0 else Math.round(correct * 20f / total)

/** Met à jour la carrière après une avant-première. */
fun finishEval(p: Progress, genreId: String, correct: List<String>, wrong: List<String>, today: Long): Progress {
    val score = score20(correct.size, correct.size + wrong.size)
    val streak = when {
        p.lastDay == today -> maxOf(p.streak, 1)
        p.lastDay == today - 1 -> p.streak + 1
        else -> 1
    }
    return p.copy(
        points = p.points + correct.size * POINTS_PER_ANSWER,
        evalBest = p.evalBest + (genreId to maxOf(score, p.evalBest[genreId] ?: 0)),
        streak = streak,
        bestStreak = maxOf(p.bestStreak, streak),
        lastDay = today,
        missed = (p.missed - correct.toSet()) + wrong,
    )
}

fun sceneUnlocked(g: Genre, sceneIndex: Int, p: Progress): Boolean =
    sceneIndex == 0 || sceneDone(p, g.scenes[sceneIndex - 1].id)

/** La prochaine scène à tourner, ou null si tout est terminé. */
fun nextToPlay(genres: List<Genre>, p: Progress): Pair<Genre, Scene>? {
    genres.forEachIndexed { i, g ->
        if (genreUnlocked(genres, i, p)) {
            g.scenes.firstOrNull { !sceneDone(p, it.id) }?.let { return g to it }
        }
    }
    return null
}

fun starsFor(correct: Int, total: Int): Int = when {
    total == 0 || correct >= total -> 3
    correct * 4 >= total * 3 -> 2
    else -> 1
}

/** Met à jour la carrière à la fin d'une scène. Renvoie la nouvelle progression et les étoiles gagnées. */
fun finishScene(p: Progress, sceneId: String, correct: List<String>, wrong: List<String>, today: Long): Pair<Progress, Int> {
    val stars = starsFor(correct.size, correct.size + wrong.size)
    val streak = when {
        p.lastDay == today -> maxOf(p.streak, 1)
        p.lastDay == today - 1 -> p.streak + 1
        else -> 1
    }
    val updated = p.copy(
        points = p.points + correct.size * POINTS_PER_ANSWER,
        sceneStars = p.sceneStars + (sceneId to maxOf(stars, p.sceneStars[sceneId] ?: 0)),
        streak = streak,
        bestStreak = maxOf(p.bestStreak, streak),
        lastDay = today,
        missed = (p.missed - correct.toSet()) + wrong,
        perfectCount = p.perfectCount + if (stars == 3) 1 else 0,
    )
    return updated to stars
}

fun trophies(genres: List<Genre>, p: Progress, gender: Gender, films: List<Genre> = emptyList()): List<Trophy> {
    val total = (genres + films).sumOf { scenesDone(it, p) }
    val top = RANKS.last()
    return listOf(
        Trophy("Premier clap", "Terminer ta première scène", total >= 1),
        Trophy("Sans faute", "Réussir une scène sans aucune erreur", p.perfectCount >= 1),
        Trophy("Série de 3 jours", "Tourner 3 jours de suite", p.bestStreak >= 3),
        Trophy("Une semaine de tournage", "Tourner 7 jours de suite", p.bestStreak >= 7),
        Trophy("Cinéphile", "Terminer toutes les scènes d'un de tes films", films.any { genreDone(it, p) }),
        Trophy("Scénariste", "Noter 5 répliques dans « Mes répliques »", p.myLines.size >= 5),
    ) + genres.map { g ->
        Trophy("Oscar : ${g.title}", "Obtenir au moins $EVAL_OSCAR/20 à l'avant-première", (p.evalBest[g.id] ?: 0) >= EVAL_OSCAR)
    } + listOf(
        Trophy(top.title(gender), "Atteindre le plus haut rang", p.points >= top.min),
    )
}
