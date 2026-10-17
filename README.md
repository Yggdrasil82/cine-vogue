# Ciné Vogue

Appli Android pour apprendre l'anglais en s'amusant, sur le thème du cinéma,
pour les études et les voyages. Deux niveaux : débutant (A2) et avancé (B1+ → C1).

- Plusieurs profils, chacun avec son niveau, sa progression et sa voix
- Carrière : de Figurante ou Figurant à Star d'Hollywood
- Après chaque réponse : traduction, mots importants et explication de l'erreur
- 4 jeux : le doublage, le script troué, le casting (écoute), le clap de fin
- Avant-première : évaluation de fin de thème (12 questions, correction à la fin),
  10/20 pour débloquer le thème suivant, Oscar à 14/20
- Débutant : Comédie au lycée, Road movie, Romance au café, Policier, Science-fiction
- Avancé : Drame à la fac, Thriller, Film d'aventure, Comédie sociale, Film d'anticipation
- Mes films : chaque profil choisit ses films ; scènes originales écrites dans leur univers
  (aucune réplique réelle n'est reprise)
- Mes répliques : on note une réplique entendue en VO, l'appli la lit et la fait réviser
- Scène improvisée après chaque thème : dialogue au micro avec un personnage animé joué par
  une IA (Mistral), correction à chaque réplique et bilan noté sur 20
- Répète après moi : la phrase est comparée mot à mot avec ce que le téléphone a compris,
  mots ratés en rouge avec conseils de prononciation (et conseil de l'IA)
- Voix anglaises du téléphone au choix (accent britannique par défaut)
- Rappel quotidien (18 h 30 par défaut)

## Compilation (GitHub Actions)

Lancement manuel du workflow « Ciné Vogue » :
- mode `captures` → images sur la branche `captures`
- mode `apk` → APK publié dans « Releases » (tag `vX.Y` créé automatiquement)

## Versions

- **1.3** : atelier « Répète après moi », vérification de la phrase dictée avant envoi à l'IA,
  conseils de prononciation au bilan, IA plus robuste (réessai, modèle de secours, ministral par défaut)
- **1.2** : scènes improvisées avec l'IA (Mistral), personnage animé, réglages parent, partage de la clé par QR code
- **1.1** : profils, niveau avancé B1+ → C1, corrections détaillées, choix de la voix,
  19 films au choix (2 scènes chacun), Mes répliques, évaluation de fin de thème
- **1.0** : première version

## Prochainement

- Plus de scènes par film (2 pour l'instant)
- Analyse phonétique fine avec Microsoft Azure (gratuité à vérifier)
