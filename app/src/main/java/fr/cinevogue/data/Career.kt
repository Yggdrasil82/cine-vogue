package fr.cinevogue.data

data class Rank(val title: String, val min: Int)

val RANKS = listOf(
    Rank("Figurante", 0),
    Rank("Second rôle", 150),
    Rank("Premier rôle", 350),
    Rank("Vedette", 600),
    Rank("Star d'Hollywood", 900),
)

data class Trophy(val title: String, val description: String, val earned: Boolean)

const val POINTS_PER_ANSWER = 5

fun rankFor(points: Int): Rank = RANKS.last { points >= it.min }

fun nextRank(points: Int): Rank? = RANKS.firstOrNull { it.min > points }

fun currentStreak(p: Progress, today: Long): Int = if (p.lastDay >= today - 1) p.streak else 0

fun sceneDone(p: Progress, sceneId: String): Boolean = (p.sceneStars[sceneId] ?: 0) > 0

fun scenesDone(g: Genre, p: Progress): Int = g.scenes.count { sceneDone(p, it.id) }

fun genreDone(g: Genre, p: Progress): Boolean = scenesDone(g, p) == g.scenes.size

fun genreUnlocked(index: Int, p: Progress): Boolean = index == 0 || genreDone(GENRES[index - 1], p)

fun sceneUnlocked(g: Genre, sceneIndex: Int, p: Progress): Boolean =
    sceneIndex == 0 || sceneDone(p, g.scenes[sceneIndex - 1].id)

/** La prochaine scène à tourner, ou null si tout est terminé. */
fun nextToPlay(p: Progress): Pair<Genre, Scene>? {
    GENRES.forEachIndexed { i, g ->
        if (genreUnlocked(i, p)) {
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

fun trophies(p: Progress): List<Trophy> {
    val total = GENRES.sumOf { scenesDone(it, p) }
    val prizes = listOf("Prix de la comédie", "Prix du road movie", "Prix du café", "Prix du polar", "Prix de la science-fiction")
    return listOf(
        Trophy("Premier clap", "Terminer ta première scène", total >= 1),
        Trophy("Sans faute", "Réussir une scène sans aucune erreur", p.perfectCount >= 1),
        Trophy("Série de 3 jours", "Tourner 3 jours de suite", p.bestStreak >= 3),
        Trophy("Une semaine de tournage", "Tourner 7 jours de suite", p.bestStreak >= 7),
    ) + GENRES.mapIndexed { i, g ->
        Trophy(prizes[i], "Terminer « ${g.title} »", genreDone(g, p))
    } + listOf(
        Trophy("Star d'Hollywood", "Atteindre le plus haut rang", p.points >= RANKS.last().min),
    )
}
