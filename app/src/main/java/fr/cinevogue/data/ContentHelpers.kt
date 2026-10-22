package fr.cinevogue.data

// Fonctions d'écriture du contenu, sans état (évite les cycles d'initialisation entre fichiers).
internal fun l(speaker: String, en: String, fr: String, gloss: String) = Line(speaker, en, fr, gloss)
internal fun b(speaker: String, en: String, fr: String, gloss: String, blank: String, why: String, vararg decoys: String) =
    Line(speaker, en, fr, gloss, blank, why, decoys.toList())
internal fun w(en: String, fr: String) = Word(en, fr)
