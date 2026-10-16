package fr.cinevogue.data

/** Une réplique du scénario. [blank] = mot à retrouver dans le jeu « script troué ». */
data class Line(
    val speaker: String,
    val en: String,
    val fr: String,
    val blank: String? = null,
    val decoys: List<String> = emptyList(),
)

data class Word(val en: String, val fr: String)

data class Tip(val title: String, val text: String)

data class Scene(
    val id: String,
    val title: String,
    val lines: List<Line>,
    val words: List<Word>,
    val tip: Tip,
)

data class Genre(
    val id: String,
    val title: String,
    val subtitle: String,
    val scenes: List<Scene>,
)

private fun l(speaker: String, en: String, fr: String) = Line(speaker, en, fr)
private fun b(speaker: String, en: String, fr: String, blank: String, vararg decoys: String) =
    Line(speaker, en, fr, blank, decoys.toList())
private fun w(en: String, fr: String) = Word(en, fr)

val GENRES: List<Genre> = listOf(
    Genre(
        "g1", "Comédie au lycée", "Se présenter, l'école, la famille, les goûts",
        listOf(
            Scene(
                "g1s1", "La nouvelle",
                listOf(
                    l("Emma", "Hi! I'm new here. What's your name?", "Salut ! Je suis nouvelle ici. Comment tu t'appelles ?"),
                    b("Leo", "My name is Leo. I am in your class.", "Je m'appelle Leo. Je suis dans ta classe.", "am", "is", "are"),
                    l("Emma", "Nice to meet you, Leo!", "Ravie de te rencontrer, Leo !"),
                    b("Leo", "Welcome to our school!", "Bienvenue dans notre lycée !", "our", "we", "us"),
                ),
                listOf(w("new", "nouveau, nouvelle"), w("name", "nom, prénom"), w("class", "classe"), w("school", "école, lycée")),
                Tip("I'm = I am = je suis", "Le verbe be (être) au présent : I am, you are, he/she is, we are, they are. À l'oral, on colle les mots : I'm new, you're funny, she's my friend."),
            ),
            Scene(
                "g1s2", "La famille de Leo",
                listOf(
                    l("Leo", "I have got a big family.", "J'ai une grande famille."),
                    b("Emma", "Have you got any brothers or sisters?", "Tu as des frères ou des sœurs ?", "any", "some", "a"),
                    l("Leo", "Yes, I have two sisters and a brother.", "Oui, j'ai deux sœurs et un frère."),
                    b("Emma", "My mother is a nurse.", "Ma mère est infirmière.", "mother", "father", "brother"),
                ),
                listOf(w("brother", "frère"), w("sister", "sœur"), w("mother", "mère"), w("father", "père")),
                Tip("have got = avoir", "Pour dire ce qu'on possède : I have got (I've got) a sister. Question : Have you got a pet? Avec he ou she : she has got."),
            ),
            Scene(
                "g1s3", "Les goûts",
                listOf(
                    l("Emma", "I love watching movies.", "J'adore regarder des films."),
                    b("Leo", "I don't like football at all.", "Je n'aime pas du tout le foot.", "don't", "doesn't", "not"),
                    l("Emma", "What do you like doing on weekends?", "Qu'est-ce que tu aimes faire le week-end ?"),
                    b("Leo", "I like playing video games.", "J'aime jouer aux jeux vidéo.", "playing", "play", "plays"),
                ),
                listOf(w("movie", "film"), w("weekend", "week-end"), w("music", "musique"), w("game", "jeu")),
                Tip("like + -ing", "Après like, love ou hate, on met souvent le verbe en -ing : I love dancing. La négation : I don't like…, she doesn't like…"),
            ),
            Scene(
                "g1s4", "L'emploi du temps",
                listOf(
                    l("Leo", "We have English on Monday.", "On a anglais le lundi."),
                    b("Emma", "What time does the lesson start?", "À quelle heure commence le cours ?", "does", "do", "is"),
                    l("Leo", "It starts at nine o'clock.", "Il commence à neuf heures."),
                    b("Emma", "I usually eat at the canteen.", "D'habitude, je mange à la cantine.", "usually", "yesterday", "tomorrow"),
                ),
                listOf(w("Monday", "lundi"), w("lesson", "cours"), w("homework", "devoirs"), w("teacher", "professeur")),
                Tip("Le présent simple", "Pour les habitudes : I eat, she eats. N'oublie pas le -s avec he, she, it ! Les questions se font avec do ou does : Does it start at nine?"),
            ),
        ),
    ),
    Genre(
        "g2", "Road movie", "Aéroport, hôtel, demander son chemin, transports",
        listOf(
            Scene(
                "g2s1", "À l'aéroport",
                listOf(
                    l("Agent", "Can I see your passport, please?", "Je peux voir ton passeport, s'il te plaît ?"),
                    b("Emma", "Here you are. Where is gate 12?", "Le voici. Où est la porte 12 ?", "Where", "When", "Who"),
                    l("Agent", "It's on your left, after the shop.", "C'est sur ta gauche, après la boutique."),
                    b("Emma", "Thank you! My flight leaves at ten.", "Merci ! Mon vol part à dix heures.", "leaves", "leave", "leaving"),
                ),
                listOf(w("passport", "passeport"), w("flight", "vol (en avion)"), w("gate", "porte d'embarquement"), w("luggage", "bagages")),
                Tip("Les mots pour questionner", "Where = où, When = quand, Who = qui, What = quoi ou quel, How = comment. Ils se placent au début : Where is the gate?"),
            ),
            Scene(
                "g2s2", "À l'hôtel",
                listOf(
                    l("Emma", "Hello, I have a reservation for two nights.", "Bonjour, j'ai une réservation pour deux nuits."),
                    b("Receptionist", "Could you spell your name, please?", "Pourriez-vous épeler votre nom, s'il vous plaît ?", "spell", "spells", "spelled"),
                    l("Emma", "Is breakfast included?", "Le petit-déjeuner est-il compris ?"),
                    b("Receptionist", "Yes, it is served from seven to ten.", "Oui, il est servi de sept à dix heures.", "from", "since", "at"),
                ),
                listOf(w("room", "chambre"), w("key", "clé"), w("night", "nuit"), w("breakfast", "petit-déjeuner")),
                Tip("Demander poliment : Could you… ?", "Could you…? = Pourriez-vous… ? C'est la formule polie idéale en voyage : Could you help me? Could I have the key?"),
            ),
            Scene(
                "g2s3", "Perdue en ville",
                listOf(
                    l("Emma", "Excuse me, how do I get to the station?", "Excusez-moi, comment je vais à la gare ?"),
                    b("Man", "Go straight on and turn right.", "Allez tout droit et tournez à droite.", "right", "left", "back"),
                    l("Emma", "Is it far from here?", "C'est loin d'ici ?"),
                    b("Man", "No, it's only five minutes on foot.", "Non, c'est à seulement cinq minutes à pied.", "on", "by", "in"),
                ),
                listOf(w("station", "gare"), w("street", "rue"), w("map", "plan, carte"), w("bridge", "pont")),
                Tip("Indiquer le chemin", "Pour donner une direction, on met le verbe seul, sans sujet : Go straight on (allez tout droit), Turn left (tournez à gauche), Take the bus (prenez le bus)."),
            ),
            Scene(
                "g2s4", "Le billet de train",
                listOf(
                    l("Emma", "A return ticket to London, please.", "Un aller-retour pour Londres, s'il vous plaît."),
                    b("Clerk", "That's twenty pounds.", "Ça fait vingt livres.", "pounds", "euros", "dollars"),
                    l("Emma", "Which platform does the train leave from?", "De quel quai part le train ?"),
                    b("Clerk", "Platform three. It's leaving in ten minutes.", "Quai trois. Il part dans dix minutes.", "leaving", "leave", "leaves"),
                ),
                listOf(w("ticket", "billet"), w("platform", "quai"), w("timetable", "horaires"), w("delay", "retard")),
                Tip("Le présent en -ing", "be + verbe-ing décrit ce qui se passe en ce moment ou un départ tout proche : The train is leaving = le train part, il est en train de partir."),
            ),
        ),
    ),
    Genre(
        "g3", "Romance au café", "Commander, payer, faire connaissance",
        listOf(
            Scene(
                "g3s1", "La commande",
                listOf(
                    l("Waiter", "Hi! What would you like?", "Salut ! Qu'est-ce que tu voudrais ?"),
                    b("Emma", "I would like a hot chocolate, please.", "Je voudrais un chocolat chaud, s'il te plaît.", "would", "will", "do"),
                    l("Waiter", "Anything else?", "Autre chose ?"),
                    b("Emma", "Yes, a piece of cake too.", "Oui, une part de gâteau aussi.", "piece", "glass", "cup"),
                ),
                listOf(w("cake", "gâteau"), w("cup", "tasse"), w("sugar", "sucre"), w("milk", "lait")),
                Tip("I would like = je voudrais", "I would like (ou I'd like) est plus poli que I want (je veux). C'est la formule à utiliser au café ou dans un magasin."),
            ),
            Scene(
                "g3s2", "L'addition",
                listOf(
                    l("Emma", "Can I have the bill, please?", "Je peux avoir l'addition, s'il vous plaît ?"),
                    b("Waiter", "Sure. That's eight pounds fifty.", "Bien sûr. Ça fait huit livres cinquante.", "That's", "There", "It"),
                    l("Emma", "Can I pay by card?", "Je peux payer par carte ?"),
                    b("Waiter", "Of course. Here is your receipt.", "Bien sûr. Voici ton ticket de caisse.", "your", "you", "yours"),
                ),
                listOf(w("bill", "addition"), w("card", "carte"), w("cash", "espèces"), w("tip", "pourboire")),
                Tip("Can I… ? = est-ce que je peux… ?", "Can I + verbe pour demander : Can I pay? Can I sit here? Pour répondre : Yes, you can ou No, you can't."),
            ),
            Scene(
                "g3s3", "La rencontre",
                listOf(
                    l("Sam", "Is this seat free?", "Cette place est libre ?"),
                    b("Emma", "Yes, sit down! Are you from here?", "Oui, assieds-toi ! Tu es d'ici ?", "from", "of", "at"),
                    l("Sam", "No, I'm from Dublin. And you?", "Non, je viens de Dublin. Et toi ?"),
                    b("Emma", "I'm French. I live in Lyon.", "Je suis française. J'habite à Lyon.", "live", "lives", "living"),
                ),
                listOf(w("friend", "ami, amie"), w("city", "ville"), w("country", "pays"), w("language", "langue")),
                Tip("Les nationalités", "En anglais, les nationalités prennent une majuscule : French, English, Irish. I'm from + ville ou pays = je viens de."),
            ),
            Scene(
                "g3s4", "On se revoit ?",
                listOf(
                    l("Sam", "Are you free on Saturday?", "Tu es libre samedi ?"),
                    b("Emma", "Yes! Let's go to the cinema.", "Oui ! Allons au cinéma.", "Let's", "Lets", "Let"),
                    l("Sam", "Great idea. I'll meet you at six.", "Super idée. Je te retrouve à six heures."),
                    b("Emma", "See you on Saturday!", "À samedi !", "See", "Look", "Watch"),
                ),
                listOf(w("Saturday", "samedi"), w("idea", "idée"), w("tonight", "ce soir"), w("tomorrow", "demain")),
                Tip("Let's = proposer", "Let's + verbe = allons… ou on… : Let's go! Let's watch a movie. C'est la façon la plus simple de proposer une sortie."),
            ),
        ),
    ),
    Genre(
        "g4", "Policier", "Poser des questions, décrire une personne, raconter au passé",
        listOf(
            Scene(
                "g4s1", "Le témoin",
                listOf(
                    l("Detective", "Where were you last night?", "Où étiez-vous hier soir ?"),
                    b("Emma", "I was at home with my family.", "J'étais à la maison avec ma famille.", "was", "were", "am"),
                    l("Detective", "Did you hear anything strange?", "Avez-vous entendu quelque chose de bizarre ?"),
                    b("Emma", "Yes, I heard a loud noise.", "Oui, j'ai entendu un grand bruit.", "heard", "hear", "hears"),
                ),
                listOf(w("last night", "hier soir"), w("noise", "bruit"), w("strange", "bizarre"), w("witness", "témoin")),
                Tip("was / were", "Le passé de be : I was, you were, he/she was, we were, they were. Where were you? = Où étais-tu ?"),
            ),
            Scene(
                "g4s2", "Le portrait-robot",
                listOf(
                    l("Detective", "What did he look like?", "À quoi ressemblait-il ?"),
                    b("Emma", "He was tall with short black hair.", "Il était grand avec des cheveux noirs courts.", "tall", "short", "young"),
                    l("Detective", "Was he wearing glasses?", "Est-ce qu'il portait des lunettes ?"),
                    b("Emma", "No, but he had a red jacket.", "Non, mais il avait une veste rouge.", "had", "has", "have"),
                ),
                listOf(w("tall", "grand (taille)"), w("hair", "cheveux"), w("glasses", "lunettes"), w("jacket", "veste")),
                Tip("L'adjectif avant le nom", "En anglais, l'adjectif se place avant le nom et ne prend jamais de s : a red jacket, black hair, two tall men."),
            ),
            Scene(
                "g4s3", "L'enquête",
                listOf(
                    l("Detective", "The thief stole a painting yesterday.", "Le voleur a volé un tableau hier."),
                    b("Emma", "When did it happen?", "Quand est-ce que c'est arrivé ?", "did", "does", "was"),
                    l("Detective", "It happened around midnight.", "C'est arrivé vers minuit."),
                    b("Emma", "I saw a car near the museum.", "J'ai vu une voiture près du musée.", "saw", "see", "seen"),
                ),
                listOf(w("thief", "voleur"), w("museum", "musée"), w("yesterday", "hier"), w("car", "voiture")),
                Tip("Le prétérit", "Pour une action terminée dans le passé : verbe + -ed (happened), ou forme irrégulière à apprendre (see → saw, steal → stole). Question : Did you see…?"),
            ),
            Scene(
                "g4s4", "Affaire classée",
                listOf(
                    l("Detective", "We found the painting!", "Nous avons retrouvé le tableau !"),
                    b("Emma", "Who was the thief?", "Qui était le voleur ?", "Who", "Where", "Why"),
                    l("Detective", "It was the museum guard.", "C'était le gardien du musée."),
                    b("Emma", "I didn't expect that!", "Je ne m'y attendais pas !", "didn't", "don't", "wasn't"),
                ),
                listOf(w("find", "trouver"), w("guard", "gardien"), w("truth", "vérité"), w("clue", "indice")),
                Tip("La négation au passé", "Au prétérit, la négation se fait avec didn't + verbe de base : I didn't see, she didn't know. Attention : jamais « didn't saw » !"),
            ),
        ),
    ),
    Genre(
        "g5", "Science-fiction", "Parler du futur et de ses projets",
        listOf(
            Scene(
                "g5s1", "Le décollage",
                listOf(
                    l("Captain", "We will land on Mars tomorrow.", "Nous atterrirons sur Mars demain."),
                    b("Emma", "Will it be dangerous?", "Est-ce que ce sera dangereux ?", "Will", "Do", "Is"),
                    l("Captain", "Don't worry, the ship is safe.", "Ne t'inquiète pas, le vaisseau est sûr."),
                    b("Emma", "I will take lots of photos!", "Je prendrai plein de photos !", "will", "am", "going"),
                ),
                listOf(w("ship", "vaisseau"), w("space", "espace"), w("star", "étoile"), w("planet", "planète")),
                Tip("Le futur avec will", "will + verbe = futur : I will travel (je voyagerai). Contraction : I'll. Négation : won't (= will not)."),
            ),
            Scene(
                "g5s2", "Les projets",
                listOf(
                    l("Robot", "What are you going to do next year?", "Qu'est-ce que tu vas faire l'année prochaine ?"),
                    b("Emma", "I'm going to study English in Canada.", "Je vais étudier l'anglais au Canada.", "going", "go", "goes"),
                    l("Robot", "That sounds amazing.", "Ça a l'air génial."),
                    b("Emma", "And then I'm going to travel the world.", "Et ensuite je vais voyager dans le monde entier.", "travel", "travels", "travelling"),
                ),
                listOf(w("next year", "l'année prochaine"), w("world", "monde"), w("dream", "rêve"), w("future", "avenir")),
                Tip("be going to", "be going to + verbe = aller faire quelque chose, pour un projet déjà décidé : I'm going to study. C'est comme notre futur proche."),
            ),
            Scene(
                "g5s3", "La planète inconnue",
                listOf(
                    l("Emma", "This planet is bigger than Earth!", "Cette planète est plus grande que la Terre !"),
                    b("Captain", "Yes, and it's colder than the Moon.", "Oui, et elle est plus froide que la Lune.", "colder", "cold", "coldest"),
                    l("Emma", "It's the most beautiful place I've ever seen.", "C'est le plus bel endroit que j'aie jamais vu."),
                    b("Captain", "Be careful, it's more dangerous than it looks.", "Fais attention, c'est plus dangereux que ça en a l'air.", "more", "most", "much"),
                ),
                listOf(w("Earth", "Terre"), w("Moon", "Lune"), w("sky", "ciel"), w("alien", "extraterrestre")),
                Tip("Les comparatifs", "Mot court : -er than (bigger than, colder than). Mot long : more … than (more dangerous than). Le plus : the biggest, the most beautiful."),
            ),
            Scene(
                "g5s4", "Le retour",
                listOf(
                    l("Captain", "We are going home. Are you happy?", "Nous rentrons à la maison. Tu es contente ?"),
                    b("Emma", "Yes, but I will miss the stars.", "Oui, mais les étoiles vont me manquer.", "miss", "misses", "missing"),
                    l("Captain", "You were a great astronaut.", "Tu as été une super astronaute."),
                    b("Emma", "One day, I will come back!", "Un jour, je reviendrai !", "come", "came", "coming"),
                ),
                listOf(w("home", "maison, chez soi"), w("journey", "voyage, trajet"), w("astronaut", "astronaute"), w("come back", "revenir")),
                Tip("Réviser le futur", "I will come back (je reviendrai). I won't forget (je n'oublierai pas). Will you come? (Viendras-tu ?)"),
            ),
        ),
    ),
)
