package fr.cinevogue.data

/**
 * Une réplique du scénario.
 * [gloss] = les mots importants traduits, [blank] = mot à retrouver dans « script troué »,
 * [why] = explication de la bonne réponse et des pièges.
 */
data class Line(
    val speaker: String,
    val en: String,
    val fr: String,
    val gloss: String,
    val blank: String? = null,
    val why: String? = null,
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

enum class Level(val label: String, val detail: String) {
    DEBUTANT("Débutant", "Niveau A2 · lycée et voyages"),
    AVANCE("Avancé", "Niveau B1+ → C1"),
}

fun genresFor(level: Level): List<Genre> = if (level == Level.AVANCE) ADVANCED else BEGINNER

/** Thèmes et films, tous niveaux confondus. */
val ALL_GENRES: List<Genre> get() = BEGINNER + ADVANCED + FILMS_BEGINNER + FILMS_ADVANCED

internal fun l(speaker: String, en: String, fr: String, gloss: String) = Line(speaker, en, fr, gloss)
internal fun b(speaker: String, en: String, fr: String, gloss: String, blank: String, why: String, vararg decoys: String) =
    Line(speaker, en, fr, gloss, blank, why, decoys.toList())
internal fun w(en: String, fr: String) = Word(en, fr)

val BEGINNER: List<Genre> = listOf(
    Genre(
        "g1", "Comédie au lycée", "Se présenter, l'école, la famille, les goûts",
        listOf(
            Scene(
                "g1s1", "La nouvelle",
                listOf(
                    l("Emma", "Hi! I'm new here. What's your name?", "Salut ! Je suis nouvelle ici. Comment tu t'appelles ?",
                        "new = nouveau, nouvelle · here = ici · name = nom, prénom"),
                    b("Leo", "My name is Leo. I am in your class.", "Je m'appelle Leo. Je suis dans ta classe.",
                        "my = mon, ma · in = dans · your = ton, ta",
                        "am", "Avec I (je), le verbe be devient am : I am. On dit he/she is, et you/we/they are.", "is", "are"),
                    l("Emma", "Nice to meet you, Leo!", "Ravie de te rencontrer, Leo !",
                        "nice = agréable · meet = rencontrer · you = toi, vous"),
                    b("Leo", "Welcome to our school!", "Bienvenue dans notre lycée !",
                        "welcome = bienvenue · our = notre · school = école, lycée",
                        "our", "our = notre (à nous). we = nous (sujet), us = nous (complément) : We love our school.", "we", "us"),
                ),
                listOf(w("new", "nouveau, nouvelle"), w("name", "nom, prénom"), w("class", "classe"), w("school", "école, lycée")),
                Tip("I'm = I am = je suis", "Le verbe be (être) au présent : I am, you are, he/she is, we are, they are. À l'oral, on colle les mots : I'm new, you're funny, she's my friend."),
            ),
            Scene(
                "g1s2", "La famille de Leo",
                listOf(
                    l("Leo", "I have got a big family.", "J'ai une grande famille.",
                        "have got = avoir · big = grand · family = famille"),
                    b("Emma", "Have you got any brothers or sisters?", "Tu as des frères ou des sœurs ?",
                        "brothers = frères · or = ou · sisters = sœurs",
                        "any", "Dans une question, on utilise any : Have you got any…? On garde some pour les phrases affirmatives : I have got some friends. a irait avec un seul : a brother.", "some", "a"),
                    l("Leo", "Yes, I have two sisters and a brother.", "Oui, j'ai deux sœurs et un frère.",
                        "two = deux · and = et · a brother = un frère"),
                    b("Emma", "My mother is a nurse.", "Ma mère est infirmière.",
                        "mother = mère · nurse = infirmière",
                        "mother", "La traduction dit « ma mère » : mother. father = père, brother = frère. Remarque : en anglais, on met a devant un métier : she is a nurse.", "father", "brother"),
                ),
                listOf(w("brother", "frère"), w("sister", "sœur"), w("mother", "mère"), w("father", "père")),
                Tip("have got = avoir", "Pour dire ce qu'on possède : I have got (I've got) a sister. Question : Have you got a pet? Avec he ou she : she has got."),
            ),
            Scene(
                "g1s3", "Les goûts",
                listOf(
                    l("Emma", "I love watching movies.", "J'adore regarder des films.",
                        "love = adorer · watching = regarder · movies = films"),
                    b("Leo", "I don't like football at all.", "Je n'aime pas du tout le foot.",
                        "don't = ne… pas · like = aimer · at all = pas du tout",
                        "don't", "Avec I, you, we, they, la négation est don't. On garde doesn't pour he, she, it : She doesn't like football. « I not like » n'existe pas.", "doesn't", "not"),
                    l("Emma", "What do you like doing on weekends?", "Qu'est-ce que tu aimes faire le week-end ?",
                        "what = qu'est-ce que · doing = faire · on weekends = le week-end"),
                    b("Leo", "I like playing video games.", "J'aime jouer aux jeux vidéo.",
                        "playing = jouer · video games = jeux vidéo",
                        "playing", "Après like, love ou hate, on met le verbe en -ing : I like playing. « I like play » est une erreur fréquente, et plays ne va qu'avec he, she, it.", "play", "plays"),
                ),
                listOf(w("movie", "film"), w("weekend", "week-end"), w("music", "musique"), w("game", "jeu")),
                Tip("like + -ing", "Après like, love ou hate, on met souvent le verbe en -ing : I love dancing. La négation : I don't like…, she doesn't like…"),
            ),
            Scene(
                "g1s4", "L'emploi du temps",
                listOf(
                    l("Leo", "We have English on Monday.", "On a anglais le lundi.",
                        "we = nous, on · English = l'anglais · on Monday = le lundi"),
                    b("Emma", "What time does the lesson start?", "À quelle heure commence le cours ?",
                        "what time = à quelle heure · lesson = cours · start = commencer",
                        "does", "The lesson = it (il), donc la question se fait avec does. On utilise do avec I, you, we, they. is ne marche pas avec un autre verbe (start).", "do", "is"),
                    l("Leo", "It starts at nine o'clock.", "Il commence à neuf heures.",
                        "starts = commence · at = à · nine o'clock = neuf heures"),
                    b("Emma", "I usually eat at the canteen.", "D'habitude, je mange à la cantine.",
                        "usually = d'habitude · eat = manger · canteen = cantine",
                        "usually", "usually = d'habitude : c'est une habitude, d'où le présent simple. yesterday (hier) irait avec le passé, tomorrow (demain) avec le futur.", "yesterday", "tomorrow"),
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
                    l("Agent", "Can I see your passport, please?", "Je peux voir ton passeport, s'il te plaît ?",
                        "can I = est-ce que je peux · see = voir · please = s'il te plaît"),
                    b("Emma", "Here you are. Where is gate 12?", "Le voici. Où est la porte 12 ?",
                        "here you are = voici · gate = porte d'embarquement",
                        "Where", "Where = où (un lieu). When = quand (un moment), Who = qui (une personne). On cherche un lieu : Where.", "When", "Who"),
                    l("Agent", "It's on your left, after the shop.", "C'est sur ta gauche, après la boutique.",
                        "on your left = sur ta gauche · after = après · shop = boutique"),
                    b("Emma", "Thank you! My flight leaves at ten.", "Merci ! Mon vol part à dix heures.",
                        "flight = vol · leaves = part · at ten = à dix heures",
                        "leaves", "My flight = it, donc le verbe prend un -s au présent simple : it leaves. leaving demanderait is avant : it is leaving.", "leave", "leaving"),
                ),
                listOf(w("passport", "passeport"), w("flight", "vol (en avion)"), w("gate", "porte d'embarquement"), w("luggage", "bagages")),
                Tip("Les mots pour questionner", "Where = où, When = quand, Who = qui, What = quoi ou quel, How = comment. Ils se placent au début : Where is the gate?"),
            ),
            Scene(
                "g2s2", "À l'hôtel",
                listOf(
                    l("Emma", "Hello, I have a reservation for two nights.", "Bonjour, j'ai une réservation pour deux nuits.",
                        "reservation = réservation · for = pour · nights = nuits"),
                    b("Receptionist", "Could you spell your name, please?", "Pourriez-vous épeler votre nom, s'il vous plaît ?",
                        "could you = pourriez-vous · spell = épeler",
                        "spell", "Après could, on met toujours le verbe de base, sans -s ni -ed : Could you spell…? Could you help me?", "spells", "spelled"),
                    l("Emma", "Is breakfast included?", "Le petit-déjeuner est-il compris ?",
                        "breakfast = petit-déjeuner · included = compris"),
                    b("Receptionist", "Yes, it is served from seven to ten.", "Oui, il est servi de sept à dix heures.",
                        "served = servi · from … to … = de … à …",
                        "from", "from … to … = de … à … pour un intervalle. since = depuis, at = à (une heure précise).", "since", "at"),
                ),
                listOf(w("room", "chambre"), w("key", "clé"), w("night", "nuit"), w("breakfast", "petit-déjeuner")),
                Tip("Demander poliment : Could you… ?", "Could you…? = Pourriez-vous… ? C'est la formule polie idéale en voyage : Could you help me? Could I have the key?"),
            ),
            Scene(
                "g2s3", "Perdue en ville",
                listOf(
                    l("Emma", "Excuse me, how do I get to the station?", "Excusez-moi, comment je vais à la gare ?",
                        "excuse me = excusez-moi · how = comment · get to = aller à · station = gare"),
                    b("Man", "Go straight on and turn right.", "Allez tout droit et tournez à droite.",
                        "go straight on = allez tout droit · turn = tournez",
                        "right", "La traduction dit « à droite » : right. left = gauche, back = en arrière. Astuce : right veut aussi dire « juste, correct ».", "left", "back"),
                    l("Emma", "Is it far from here?", "C'est loin d'ici ?",
                        "far = loin · from here = d'ici"),
                    b("Man", "No, it's only five minutes on foot.", "Non, c'est à seulement cinq minutes à pied.",
                        "only = seulement · on foot = à pied",
                        "on", "On dit on foot (à pied), mais by bus, by car, by train pour les transports.", "by", "in"),
                ),
                listOf(w("station", "gare"), w("street", "rue"), w("map", "plan, carte"), w("bridge", "pont")),
                Tip("Indiquer le chemin", "Pour donner une direction, on met le verbe seul, sans sujet : Go straight on (allez tout droit), Turn left (tournez à gauche), Take the bus (prenez le bus)."),
            ),
            Scene(
                "g2s4", "Le billet de train",
                listOf(
                    l("Emma", "A return ticket to London, please.", "Un aller-retour pour Londres, s'il vous plaît.",
                        "return ticket = aller-retour · to London = pour Londres"),
                    b("Clerk", "That's twenty pounds.", "Ça fait vingt livres.",
                        "that's = ça fait · twenty = vingt · pounds = livres (sterling)",
                        "pounds", "La traduction dit « livres » : pounds, la monnaie du Royaume-Uni. On n'y paie ni en euros ni en dollars.", "euros", "dollars"),
                    l("Emma", "Which platform does the train leave from?", "De quel quai part le train ?",
                        "which = quel · platform = quai · leave from = partir de"),
                    b("Clerk", "Platform three. It's leaving in ten minutes.", "Quai trois. Il part dans dix minutes.",
                        "it's leaving = il part · in ten minutes = dans dix minutes",
                        "leaving", "Après It's (= It is), on met le verbe en -ing : it is leaving. C'est le présent en -ing.", "leave", "leaves"),
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
                    l("Waiter", "Hi! What would you like?", "Salut ! Qu'est-ce que tu voudrais ?",
                        "what = qu'est-ce que · would like = voudrais"),
                    b("Emma", "I would like a hot chocolate, please.", "Je voudrais un chocolat chaud, s'il te plaît.",
                        "would like = voudrais · hot chocolate = chocolat chaud",
                        "would", "I would like = je voudrais, la forme polie. I will like voudrait dire « j'aimerai » (futur), et « I do like » ne se dit pas ici.", "will", "do"),
                    l("Waiter", "Anything else?", "Autre chose ?",
                        "anything = quelque chose · else = d'autre"),
                    b("Emma", "Yes, a piece of cake too.", "Oui, une part de gâteau aussi.",
                        "a piece of = un morceau, une part · cake = gâteau · too = aussi",
                        "piece", "a piece of cake = une part de gâteau. a glass of = un verre de, a cup of = une tasse de. Bonus : « It's a piece of cake! » veut aussi dire « C'est du gâteau, c'est facile ! »", "glass", "cup"),
                ),
                listOf(w("cake", "gâteau"), w("cup", "tasse"), w("sugar", "sucre"), w("milk", "lait")),
                Tip("I would like = je voudrais", "I would like (ou I'd like) est plus poli que I want (je veux). C'est la formule à utiliser au café ou dans un magasin."),
            ),
            Scene(
                "g3s2", "L'addition",
                listOf(
                    l("Emma", "Can I have the bill, please?", "Je peux avoir l'addition, s'il vous plaît ?",
                        "can I have = je peux avoir · bill = addition"),
                    b("Waiter", "Sure. That's eight pounds fifty.", "Bien sûr. Ça fait huit livres cinquante.",
                        "sure = bien sûr · that's = ça fait · fifty = cinquante",
                        "That's", "That's (= That is) = ça fait, c'est : la formule pour annoncer un prix. There et It seuls n'ont pas de verbe ici.", "There", "It"),
                    l("Emma", "Can I pay by card?", "Je peux payer par carte ?",
                        "pay = payer · by card = par carte"),
                    b("Waiter", "Of course. Here is your receipt.", "Bien sûr. Voici ton ticket de caisse.",
                        "of course = bien sûr · here is = voici · receipt = ticket de caisse",
                        "your", "your = ton, ta, votre (devant un nom). you = tu, vous ; yours = le tien, le vôtre (sans nom après).", "you", "yours"),
                ),
                listOf(w("bill", "addition"), w("card", "carte"), w("cash", "espèces"), w("tip", "pourboire")),
                Tip("Can I… ? = est-ce que je peux… ?", "Can I + verbe pour demander : Can I pay? Can I sit here? Pour répondre : Yes, you can ou No, you can't."),
            ),
            Scene(
                "g3s3", "La rencontre",
                listOf(
                    l("Sam", "Is this seat free?", "Cette place est libre ?",
                        "seat = place, siège · free = libre (ou gratuit)"),
                    b("Emma", "Yes, sit down! Are you from here?", "Oui, assieds-toi ! Tu es d'ici ?",
                        "sit down = assieds-toi · from here = d'ici",
                        "from", "be from = venir de, être originaire de : I'm from Lyon. On ne dit pas « of here » ni « at here ».", "of", "at"),
                    l("Sam", "No, I'm from Dublin. And you?", "Non, je viens de Dublin. Et toi ?",
                        "I'm from = je viens de · and you = et toi"),
                    b("Emma", "I'm French. I live in Lyon.", "Je suis française. J'habite à Lyon.",
                        "French = français, française · live = habiter, vivre",
                        "live", "Avec I, le verbe reste à la base : I live. On ajoute -s seulement avec he, she, it : she lives.", "lives", "living"),
                ),
                listOf(w("friend", "ami, amie"), w("city", "ville"), w("country", "pays"), w("language", "langue")),
                Tip("Les nationalités", "En anglais, les nationalités prennent une majuscule : French, English, Irish. I'm from + ville ou pays = je viens de."),
            ),
            Scene(
                "g3s4", "On se revoit ?",
                listOf(
                    l("Sam", "Are you free on Saturday?", "Tu es libre samedi ?",
                        "free = libre · on Saturday = samedi"),
                    b("Emma", "Yes! Let's go to the cinema.", "Oui ! Allons au cinéma.",
                        "let's go = allons · cinema = cinéma",
                        "Let's", "Let's (= Let us) + verbe sert à proposer : Let's go! L'apostrophe est obligatoire.", "Lets", "Let"),
                    l("Sam", "Great idea. I'll meet you at six.", "Super idée. Je te retrouve à six heures.",
                        "great = super · I'll meet you = je te retrouve · at six = à six heures"),
                    b("Emma", "See you on Saturday!", "À samedi !",
                        "see you = à plus, on se voit",
                        "See", "See you… = à… (on se voit) : See you tomorrow = à demain. look = regarder, watch = regarder (un film, un match).", "Look", "Watch"),
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
                    l("Detective", "Where were you last night?", "Où étiez-vous hier soir ?",
                        "where = où · were = étiez · last night = hier soir"),
                    b("Emma", "I was at home with my family.", "J'étais à la maison avec ma famille.",
                        "at home = à la maison · with = avec",
                        "was", "Le passé de be : I was, he/she was, mais you/we/they were. Avec I : was. am est au présent.", "were", "am"),
                    l("Detective", "Did you hear anything strange?", "Avez-vous entendu quelque chose de bizarre ?",
                        "did you hear = avez-vous entendu · anything = quelque chose · strange = bizarre"),
                    b("Emma", "Yes, I heard a loud noise.", "Oui, j'ai entendu un grand bruit.",
                        "heard = ai entendu · loud = fort · noise = bruit",
                        "heard", "hear → heard est un verbe irrégulier au prétérit. hear et hears sont au présent ; ici, l'action est passée (hier soir).", "hear", "hears"),
                ),
                listOf(w("last night", "hier soir"), w("noise", "bruit"), w("strange", "bizarre"), w("witness", "témoin")),
                Tip("was / were", "Le passé de be : I was, you were, he/she was, we were, they were. Where were you? = Où étais-tu ?"),
            ),
            Scene(
                "g4s2", "Le portrait-robot",
                listOf(
                    l("Detective", "What did he look like?", "À quoi ressemblait-il ?",
                        "look like = ressembler à · did = marque du passé"),
                    b("Emma", "He was tall with short black hair.", "Il était grand avec des cheveux noirs courts.",
                        "tall = grand · short = court · black hair = cheveux noirs",
                        "tall", "La traduction dit « grand » : tall, pour la taille d'une personne. short = petit, young = jeune. Attention : big = gros.", "short", "young"),
                    l("Detective", "Was he wearing glasses?", "Est-ce qu'il portait des lunettes ?",
                        "wearing = portait · glasses = lunettes"),
                    b("Emma", "No, but he had a red jacket.", "Non, mais il avait une veste rouge.",
                        "but = mais · had = avait · jacket = veste",
                        "had", "had est le passé de have. has et have sont au présent ; on raconte une scène passée.", "has", "have"),
                ),
                listOf(w("tall", "grand (taille)"), w("hair", "cheveux"), w("glasses", "lunettes"), w("jacket", "veste")),
                Tip("L'adjectif avant le nom", "En anglais, l'adjectif se place avant le nom et ne prend jamais de s : a red jacket, black hair, two tall men."),
            ),
            Scene(
                "g4s3", "L'enquête",
                listOf(
                    l("Detective", "The thief stole a painting yesterday.", "Le voleur a volé un tableau hier.",
                        "thief = voleur · stole = a volé · painting = tableau"),
                    b("Emma", "When did it happen?", "Quand est-ce que c'est arrivé ?",
                        "when = quand · happen = arriver, se passer",
                        "did", "Au passé, la question se fait avec did + verbe de base : When did it happen? does est pour le présent, et was ne va pas avec happen.", "does", "was"),
                    l("Detective", "It happened around midnight.", "C'est arrivé vers minuit.",
                        "happened = est arrivé · around = vers · midnight = minuit"),
                    b("Emma", "I saw a car near the museum.", "J'ai vu une voiture près du musée.",
                        "saw = ai vu · near = près de · museum = musée",
                        "saw", "see → saw au prétérit (verbe irrégulier). seen s'utilise avec have : I have seen.", "see", "seen"),
                ),
                listOf(w("thief", "voleur"), w("museum", "musée"), w("yesterday", "hier"), w("car", "voiture")),
                Tip("Le prétérit", "Pour une action terminée dans le passé : verbe + -ed (happened), ou forme irrégulière à apprendre (see → saw, steal → stole). Question : Did you see…?"),
            ),
            Scene(
                "g4s4", "Affaire classée",
                listOf(
                    l("Detective", "We found the painting!", "Nous avons retrouvé le tableau !",
                        "found = avons trouvé (find → found) · painting = tableau"),
                    b("Emma", "Who was the thief?", "Qui était le voleur ?",
                        "who = qui · thief = voleur",
                        "Who", "Who = qui (une personne). Where = où, Why = pourquoi.", "Where", "Why"),
                    l("Detective", "It was the museum guard.", "C'était le gardien du musée.",
                        "guard = gardien · museum = musée"),
                    b("Emma", "I didn't expect that!", "Je ne m'y attendais pas !",
                        "didn't = ne… pas (passé) · expect = s'attendre à",
                        "didn't", "Au prétérit, la négation est didn't + verbe de base. don't est pour le présent, et wasn't ne va pas avec un verbe comme expect.", "don't", "wasn't"),
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
                    l("Captain", "We will land on Mars tomorrow.", "Nous atterrirons sur Mars demain.",
                        "will land = atterrirons · tomorrow = demain"),
                    b("Emma", "Will it be dangerous?", "Est-ce que ce sera dangereux ?",
                        "will it be = est-ce que ce sera · dangerous = dangereux",
                        "Will", "Pour une question au futur, on place will en premier : Will it be…? Do et Is ne se mettent pas devant be.", "Do", "Is"),
                    l("Captain", "Don't worry, the ship is safe.", "Ne t'inquiète pas, le vaisseau est sûr.",
                        "don't worry = ne t'inquiète pas · ship = vaisseau · safe = sûr, sans danger"),
                    b("Emma", "I will take lots of photos!", "Je prendrai plein de photos !",
                        "take photos = prendre des photos · lots of = beaucoup de",
                        "will", "will + verbe = futur : I will take. « I am take » et « I going take » sont incorrects.", "am", "going"),
                ),
                listOf(w("ship", "vaisseau"), w("space", "espace"), w("star", "étoile"), w("planet", "planète")),
                Tip("Le futur avec will", "will + verbe = futur : I will travel (je voyagerai). Contraction : I'll. Négation : won't (= will not)."),
            ),
            Scene(
                "g5s2", "Les projets",
                listOf(
                    l("Robot", "What are you going to do next year?", "Qu'est-ce que tu vas faire l'année prochaine ?",
                        "going to do = vas faire · next year = l'année prochaine"),
                    b("Emma", "I'm going to study English in Canada.", "Je vais étudier l'anglais au Canada.",
                        "study = étudier · in Canada = au Canada",
                        "going", "be going to + verbe = aller faire : I'm going to study. Il faut la forme -ing de go : going.", "go", "goes"),
                    l("Robot", "That sounds amazing.", "Ça a l'air génial.",
                        "sounds = a l'air (à l'oreille) · amazing = génial"),
                    b("Emma", "And then I'm going to travel the world.", "Et ensuite je vais voyager dans le monde entier.",
                        "then = ensuite · travel = voyager · world = monde",
                        "travel", "Après going to, on met le verbe de base, sans -s ni -ing : going to travel.", "travels", "travelling"),
                ),
                listOf(w("next year", "l'année prochaine"), w("world", "monde"), w("dream", "rêve"), w("future", "avenir")),
                Tip("be going to", "be going to + verbe = aller faire quelque chose, pour un projet déjà décidé : I'm going to study. C'est comme notre futur proche."),
            ),
            Scene(
                "g5s3", "La planète inconnue",
                listOf(
                    l("Emma", "This planet is bigger than Earth!", "Cette planète est plus grande que la Terre !",
                        "bigger than = plus grande que · Earth = la Terre"),
                    b("Captain", "Yes, and it's colder than the Moon.", "Oui, et elle est plus froide que la Lune.",
                        "colder = plus froide · Moon = Lune",
                        "colder", "Avec than (que), on utilise le comparatif : cold → colder than. coldest = la plus froide (superlatif).", "cold", "coldest"),
                    l("Emma", "It's the most beautiful place I've ever seen.", "C'est le plus bel endroit que j'aie jamais vu.",
                        "the most beautiful = le plus beau · place = endroit · ever = jamais"),
                    b("Captain", "Be careful, it's more dangerous than it looks.", "Fais attention, c'est plus dangereux que ça en a l'air.",
                        "be careful = fais attention · looks = a l'air",
                        "more", "Mot long (dangerous) : more … than. most s'utilise avec the pour le superlatif : the most dangerous. much ne forme pas de comparatif seul.", "most", "much"),
                ),
                listOf(w("Earth", "Terre"), w("Moon", "Lune"), w("sky", "ciel"), w("alien", "extraterrestre")),
                Tip("Les comparatifs", "Mot court : -er than (bigger than, colder than). Mot long : more … than (more dangerous than). Le plus : the biggest, the most beautiful."),
            ),
            Scene(
                "g5s4", "Le retour",
                listOf(
                    l("Captain", "We are going home. Are you happy?", "Nous rentrons à la maison. Tu es contente ?",
                        "going home = rentrons · happy = content, contente"),
                    b("Emma", "Yes, but I will miss the stars.", "Oui, mais les étoiles vont me manquer.",
                        "miss = manquer · stars = étoiles",
                        "miss", "Après will, le verbe reste à la base : will miss. Attention au sens : I miss you = tu me manques.", "misses", "missing"),
                    l("Captain", "You were a great astronaut.", "Tu as été une super astronaute.",
                        "were = as été · great = super"),
                    b("Emma", "One day, I will come back!", "Un jour, je reviendrai !",
                        "one day = un jour · come back = revenir",
                        "come", "will + verbe de base : will come. came est le passé, coming la forme -ing.", "came", "coming"),
                ),
                listOf(w("home", "maison, chez soi"), w("journey", "voyage, trajet"), w("astronaut", "astronaute"), w("come back", "revenir")),
                Tip("Réviser le futur", "I will come back (je reviendrai). I won't forget (je n'oublierai pas). Will you come? (Viendras-tu ?)"),
            ),
        ),
    ),
)
