package fr.cinevogue.data

/** Apparence du personnage dessiné joué par l'IA. */
data class Look(
    val skin: Long,
    val hair: Long,
    val shirt: Long,
    val longHair: Boolean = false,
    val glasses: Boolean = false,
    val cap: Boolean = false,
    val robot: Boolean = false,
)

data class AiCharacter(
    val name: String,
    /** Rôle et personnalité, en anglais, pour l'IA. */
    val persona: String,
    /** Présentation en français, pour l'élève. */
    val roleFr: String,
    val look: Look,
    /** Hauteur de la voix (1.0 = normale). */
    val pitch: Float = 1.0f,
)

/** La scène d'improvisation avec l'IA, à la fin d'un grand thème. */
data class AiScenario(
    val genreId: String,
    val title: String,
    /** La situation, en français. */
    val setupFr: String,
    /** La situation, en anglais, pour l'IA. */
    val setupEn: String,
    val character: AiCharacter,
    /** La mission de l'élève : en français (affichée) et en anglais (pour l'IA). */
    val objectivesFr: List<String>,
    val objectivesEn: List<String>,
    /** Première réplique du personnage. */
    val opening: String,
    /** Grammaire à faire pratiquer, en anglais, pour l'IA. */
    val focusEn: String,
)

val AI_SCENARIOS: List<AiScenario> = listOf(
    // ---------- Débutant (A2) ----------
    AiScenario(
        "g1", "Le premier jour",
        "C'est ton premier jour dans un lycée à Londres. Jordan, un élève sympa, vient te parler dans la cour.",
        "It's the learner's first day at a secondary school in London. Jordan, a friendly student, comes to talk to them in the playground.",
        AiCharacter("Jordan", "Jordan, a cheerful 16-year-old British student who loves football and music", "Jordan, un élève britannique joyeux",
            Look(0xFFE0B08C, 0xFF3B2A1E, 0xFF2F6FB0)),
        listOf("Te présenter (prénom, âge, d'où tu viens)", "Dire ce que tu aimes faire", "Demander à Jordan sa matière préférée"),
        listOf("introduce themselves (name, age, where they are from)", "say what they like doing", "ask Jordan about their favourite subject"),
        "Hi! Are you new here? I'm Jordan. What's your name?",
        "the verb be, have got, like + -ing, present simple",
    ),
    AiScenario(
        "g2", "Perdu à Londres",
        "Tu es à la gare de King's Cross et tu veux aller au British Museum. Mrs Patel, une passante serviable, te propose son aide.",
        "The learner is at King's Cross station and wants to go to the British Museum. Mrs Patel, a helpful passer-by, offers to help.",
        AiCharacter("Mrs Patel", "Mrs Patel, a kind, patient Londoner in her fifties", "Mrs Patel, une Londonienne serviable",
            Look(0xFFB98563, 0xFF1E1E1E, 0xFF8E3B6E, longHair = true, glasses = true), pitch = 1.1f),
        listOf("Demander où est le British Museum", "Demander si c'est loin", "Demander quel bus ou métro prendre"),
        listOf("ask where the British Museum is", "ask if it is far", "ask which bus or tube to take"),
        "Hello there! You look a bit lost. Can I help you?",
        "question words (where, how, which), could you, directions (go straight on, turn left)",
    ),
    AiScenario(
        "g3", "Au café de Camden",
        "Tu entres dans un petit café de Camden. Sam, le serveur, vient prendre ta commande.",
        "The learner walks into a small café in Camden. Sam, the waiter, comes to take their order.",
        AiCharacter("Sam", "Sam, a relaxed and funny waiter at a café in Camden", "Sam, un serveur décontracté",
            Look(0xFFF1C9A5, 0xFFC2702E, 0xFF2E7D5B, cap = true)),
        listOf("Commander une boisson", "Commander quelque chose à manger", "Demander l'addition et payer par carte"),
        listOf("order a drink", "order something to eat", "ask for the bill and pay by card"),
        "Hi, welcome to the Camden Café! What can I get you?",
        "I would like, can I have, polite requests, prices",
    ),
    AiScenario(
        "g4", "Le témoin",
        "Un vol a eu lieu hier soir près de chez toi. Detective Morgan t'interroge comme témoin.",
        "A robbery happened last night near the learner's home. Detective Morgan questions the learner as a witness.",
        AiCharacter("Detective Morgan", "Detective Morgan, a calm, polite police detective", "Detective Morgan, une inspectrice calme",
            Look(0xFF8D5A3B, 0xFF14100E, 0xFF3A3F4B, longHair = true), pitch = 0.95f),
        listOf("Dire où tu étais hier soir", "Décrire la personne que tu as vue (taille, cheveux, vêtements)", "Dire à quelle heure c'est arrivé"),
        listOf("say where they were last night", "describe the person they saw (height, hair, clothes)", "say what time it happened"),
        "Good evening. I'm Detective Morgan. Thank you for helping us. Where were you last night at nine o'clock?",
        "simple past (was/were, regular and irregular verbs), describing people",
    ),
    AiScenario(
        "g5", "Recrutement spatial",
        "Tu passes l'entretien pour la première mission vers Mars. Captain Nova veut connaître tes projets.",
        "The learner has an interview for the first mission to Mars. Captain Nova wants to know about their plans.",
        AiCharacter("Captain Nova", "Captain Nova, an enthusiastic astronaut who recruits young crew members", "Captain Nova, une astronaute enthousiaste",
            Look(0xFFF0C8A0, 0xFFE8E8E8, 0xFF5B4BB7), pitch = 1.05f),
        listOf("Expliquer pourquoi tu veux partir (going to)", "Dire ce que tu feras sur Mars (will)", "Comparer la Terre et Mars (comparatifs)"),
        listOf("explain why they want to go, using going to", "say what they will do on Mars, using will", "compare Earth and Mars, using comparatives"),
        "Welcome to the Space Academy! I'm Captain Nova. So, why do you want to go to Mars?",
        "be going to, will, comparatives (bigger than, more dangerous than)",
    ),
    // ---------- Avancé (B1+ → C1) ----------
    AiScenario(
        "a1", "L'entretien à Oxford",
        "Tu passes l'entretien d'admission dans une université d'Oxford. Professor Hughes veut comprendre ton parcours et ta motivation.",
        "The learner is at an admissions interview at an Oxford college. Professor Hughes wants to understand their background and motivation.",
        AiCharacter("Professor Hughes", "Professor Hughes, a sharp but kind Oxford tutor who asks probing follow-up questions", "Professor Hughes, un professeur exigeant mais bienveillant",
            Look(0xFFEAC3A2, 0xFF9A9A9A, 0xFF6B4A2B, glasses = true), pitch = 0.9f),
        listOf("Présenter ton parcours (present perfect)", "Expliquer pourquoi ce cursus t'intéresse", "Raconter un échec et ce que tu en as appris (3e conditionnel)"),
        listOf("present their background using the present perfect", "explain why this course appeals to them", "describe a failure and what they learnt from it, using the third conditional"),
        "Good afternoon, and welcome to Oxford. Do take a seat. So, tell me, what brings you here?",
        "present perfect vs past simple, third conditional, formal register",
    ),
    AiScenario(
        "a2", "L'interrogatoire",
        "Un tableau a disparu d'une galerie hier soir, et ton nom figure sur la liste des visiteurs. Inspector Reid, sceptique, t'interroge.",
        "A painting disappeared from a gallery last night and the learner's name is on the visitor list. Inspector Reid, a sceptical detective, questions them.",
        AiCharacter("Inspector Reid", "Inspector Reid, a dry, sceptical detective with a sense of humour, who never accuses without evidence", "Inspector Reid, un inspecteur sceptique",
            Look(0xFFD9A982, 0xFF5A3A22, 0xFF2B2B2B), pitch = 0.88f),
        listOf("Donner ton alibi (past continuous)", "Rapporter ce qu'un témoin t'a dit (discours rapporté)", "Proposer une déduction (must have, can't have)"),
        listOf("give their alibi using the past continuous", "report what a witness told them, using reported speech", "suggest a deduction using must have / can't have"),
        "Sit down, please. A painting was stolen from the gallery last night, and your name is on the visitor list. Care to explain?",
        "past continuous, reported speech, modals of deduction, passive voice",
    ),
    AiScenario(
        "a3", "Vol annulé",
        "Ton vol pour Vancouver vient d'être annulé à l'aéroport d'Heathrow. Chris, l'agent au comptoir, doit trouver une solution avec toi.",
        "The learner's flight to Vancouver has just been cancelled at Heathrow. Chris, the agent at the desk, must find a solution with them.",
        AiCharacter("Chris", "Chris, a polite but overworked airline agent who follows the rules", "Chris, un agent de compagnie aérienne débordé",
            Look(0xFFC68E6A, 0xFF2A1A12, 0xFF1F4E8C, cap = true)),
        listOf("Exprimer ton mécontentement poliment", "Demander tes droits (remboursement, hôtel, repas)", "Négocier une solution avec des conditionnels"),
        listOf("express their dissatisfaction politely", "ask about their rights (refund, hotel, meals)", "negotiate a solution using conditionals"),
        "I'm afraid flight BA 304 to Vancouver has been cancelled due to a technical issue. How can I help you?",
        "polite complaints, conditionals (if, unless, provided that), be entitled to",
    ),
    AiScenario(
        "a4", "Le débat radio",
        "Tu es invité dans une émission de radio pour jeunes. Jamie, l'animateur, lance le débat : faut-il interdire les smartphones au lycée ?",
        "The learner is a guest on a youth radio show. Jamie, the host, launches a debate: should smartphones be banned in schools?",
        AiCharacter("Jamie", "Jamie, a lively radio host who plays devil's advocate to challenge guests", "Jamie, un animateur radio qui aime contredire",
            Look(0xFFF2D0B0, 0xFF8A2B1E, 0xFFB8432E, longHair = true), pitch = 1.08f),
        listOf("Donner ton avis avec nuance", "Utiliser des connecteurs (although, whereas, despite)", "Réfuter poliment un argument de Jamie"),
        listOf("give a nuanced opinion", "use linking words such as although, whereas, despite", "politely refute one of Jamie's arguments"),
        "Good evening and welcome to Youth Talk! Tonight's question: should smartphones be banned in schools? What's your take?",
        "opinion phrases, linking words, concession and contrast",
    ),
    AiScenario(
        "a5", "L'IA de 2080",
        "Tu as voyagé jusqu'en 2080. ARIA, l'intelligence artificielle qui gère la ville, accepte de répondre à tes questions… et t'en pose aussi.",
        "The learner has travelled to the year 2080. ARIA, the artificial intelligence that runs the city, agrees to answer their questions and asks some of its own.",
        AiCharacter("ARIA", "ARIA, a curious, slightly philosophical city AI from 2080 who is fascinated by humans from the past", "ARIA, l'intelligence de la ville en 2080",
            Look(0xFFB8C7D9, 0xFF6FD3E8, 0xFF2B3A55, robot = true), pitch = 1.15f),
        listOf("Imaginer l'avenir (will be -ing, will have)", "Faire une hypothèse irréelle (If I were…)", "Défendre un point de vue sur l'IA et l'éthique"),
        listOf("imagine the future using the future continuous or future perfect", "make an unreal hypothesis (If I were...)", "defend a point of view about AI and ethics"),
        "Greetings, traveller. I am ARIA, the mind of this city in the year 2080. What would you like to know about the future?",
        "future continuous, future perfect, second conditional, inversion",
    ),
)

fun scenarioFor(genreId: String): AiScenario? = AI_SCENARIOS.firstOrNull { it.genreId == genreId }
