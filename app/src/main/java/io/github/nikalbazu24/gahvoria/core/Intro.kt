package io.github.nikalbazu24.gahvoria.core

/** One screen of the introduction: a title and a short text. */
data class Slide(val title: String, val text: String)

/**
 * The introduction shown the first time Gahvoria opens, and the "what's new" screens of big updates. Big updates
 * get a new number in [UPDATES] with only their changes; small fixes add nothing, so the screens don't appear.
 */
object Intro {

    val WELCOME = listOf(
        Slide("Bienvenido a Gahvoria", "Juega tus fan games de RPG Maker y Pokémon Essentials en tu teléfono o consola, con tus partidas siempre a salvo."),
        Slide("Añade tus juegos", "Elige la carpeta de un juego y Gahvoria lo revisa. Quitar un juego de la biblioteca nunca borra sus archivos."),
        Slide("Ábrelos desde tu launcher", "Puedes crear accesos para Díptico, Daijishō o ES-DE, y elegir en qué carpeta quedan."),
        Slide("Si algo falla", "Gahvoria te dice qué pasó y, si quieres, envía un informe para que se pueda corregir. Tú decides si se envía."),
    )

    /** Big updates, oldest first: (number, its changes). Only add one when the update is big. */
    val UPDATES: List<Pair<Int, List<Slide>>> = listOf(
        1 to listOf(
            Slide("Pantalla de carga con contador", "Al abrir un juego ves cuánto lleva y si está cargando, tardando más de lo normal o pegado."),
            Slide("Carátulas de verdad", "La imagen oficial de Internet cuando el juego está en el catálogo, o una de la carpeta del juego."),
            Slide("Si un juego falla, te lo explica", "Una ventana dice qué pasó, si se puede arreglar aquí y te deja enviar el informe con un toque."),
            Slide("Accesos donde tú quieras", "Elige la carpeta de accesos para Díptico, Daijishō o ES-DE desde Ajustes."),
            Slide("Ajustes ordenados", "Cada opción está en un solo lugar: lo general en Ajustes y lo de cada juego en sus opciones."),
        ),
        2 to listOf(
            Slide("Ajustes más a mano", "Las secciones quedan siempre visibles al lado, con buscador y «Lo esencial» primero."),
            Slide("Carga con tu juego", "El cartucho flota sobre la carátula del juego, difuminada con los colores de Gahvoria."),
            Slide("Tus botones, a tu manera", "En las opciones de cada juego, mueve, agranda u oculta los botones táctiles. Cada uno trae su guía."),
            Slide("Respaldo de partidas", "Guarda un .zip de tus partidas e impórtalas desde un respaldo o desde otro emulador. Antes de reemplazar, te pregunta."),
            Slide("Borrar un juego", "Si quieres, lo borras por completo con dos confirmaciones y un respaldo opcional. «Quitar de la biblioteca» sigue sin borrar nada."),
        ),
        3 to listOf(
            Slide("Juegos en otro idioma", "Activa el traductor en Ajustes y elige en qué juegos. Traduce mientras juegas y guarda las frases para no esperar la próxima vez."),
            Slide("Gratis o con tu clave", "Puede traducir en tu teléfono sin costo, o con tu propia clave de DeepL para mejor calidad."),
            Slide("Pantalla por juego", "En cada juego, elige panorámica completa con batallas (en pruebas), panorámica solo en el mapa, o pantalla retro."),
            Slide("Clave de DeepL comprobada", "Al guardar tu clave, Gahvoria te dice si funciona y cuánto te queda."),
            Slide("Tienda de fan games", "Una lista con la página oficial de cada juego. Lo bajas allí y Gahvoria lo instala desde el .zip y lo agrega a tu biblioteca."),
            Slide("Motor automático", "Si un juego falla al abrir, Gahvoria lo reabre con el otro motor y recuerda el que funciona."),
        ),
        4 to listOf(
            Slide("Panel de creador", "Si haces un fan game, activa el modo creador en Ajustes: marca tu juego y mira su resumen, su historial de errores y su chequeo."),
            Slide("Chequeo antes de publicar", "Gahvoria revisa los archivos de tu juego y avisa de lo que suele fallar en Android: RTP, rutas de Windows, .dll y más."),
            Slide("Pide un ajuste", "Cuenta qué pasa y, si quieres, adjunta el último informe y los datos del juego. Lo envías tú, cuando tú decides."),
            Slide("Versión nueva de un juego", "Si la tienda lista una versión más nueva del juego que tienes, las opciones del juego te lo avisan."),
            Slide("Canal de actualizaciones", "En Actualizar puedes elegir recibir todas las versiones o solo las estables."),
        ),
    )

    val LATEST: Int get() = UPDATES.maxOfOrNull { it.first } ?: 0

    /**
     * What to show when the app opens, or null. New players get the welcome; players who already had games when
     * this arrived, and players after a big update, get only the changes they haven't seen.
     */
    fun pending(introSeen: Boolean, seenUpdate: Int, hasGames: Boolean): List<Slide>? = when {
        !introSeen && !hasGames -> WELCOME
        seenUpdate < LATEST -> UPDATES.filter { it.first > seenUpdate }.flatMap { it.second }.ifEmpty { null }
        else -> null
    }
}
