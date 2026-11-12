package io.github.nikalbazu24.gahvoria.core

/**
 * One game of the store. Gahvoria never hosts games: each entry points to the creator's official page, where the
 * game is downloaded. [status] is only "tested" after it was measured on the Thor; everything else is "untested".
 */
data class StoreGame(
    val id: String,
    val title: String,
    val creator: String,
    /** "en", "es" or "?" when it isn't confirmed. */
    val lang: String,
    val page: String,
    val host: String,
    /** "zip" (download a .zip and install it here), "installer" (a Windows installer: needs a computer) or "other". */
    val format: String,
    val status: String,
    val description: String,
    val note: String,
    val aliases: List<String>,
    /** The version the creator published, or "" when the list does not say. */
    val version: String = "",
) {
    val tested get() = status == "tested"
    val installableHere get() = format == "zip"
}

object StoreCatalog {

    /** The list the app keeps up to date from the repository, so new games appear without a new APK. */
    const val URL = "https://raw.githubusercontent.com/Nikalbazu24/Gahvoria/main/app/src/main/assets/store.json"

    fun parse(text: String): List<StoreGame> {
        val root = runCatching { CoverCatalog.Json(text).value() }.getOrNull() as? Map<*, *> ?: return emptyList()
        val games = root["games"] as? List<*> ?: return emptyList()
        return games.mapNotNull { g ->
            val m = g as? Map<*, *> ?: return@mapNotNull null
            val title = m["title"] as? String ?: return@mapNotNull null
            val page = m["page"] as? String ?: return@mapNotNull null
            if (!page.startsWith("https://")) return@mapNotNull null  // only secure pages open from the store
            StoreGame(
                id = m["id"] as? String ?: title,
                title = title,
                creator = m["creator"] as? String ?: "",
                lang = (m["lang"] as? String)?.takeIf { it == "en" || it == "es" } ?: "?",
                page = page,
                host = m["host"] as? String ?: "",
                format = m["format"] as? String ?: "other",
                status = if (m["status"] == "tested") "tested" else "untested",
                description = m["description"] as? String ?: "",
                note = m["note"] as? String ?: "",
                aliases = (m["aliases"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                version = (m["version"] as? String)?.trim().orEmpty(),
            )
        }
    }

    /** Filters the list: [lang] "all"/"en"/"es", [testedOnly], and a search in titles, aliases and creators. */
    fun filter(all: List<StoreGame>, lang: String, testedOnly: Boolean, query: String): List<StoreGame> {
        val q = CoverCatalog.norm(query)
        return all.filter { g ->
            (lang == "all" || g.lang == lang) && (!testedOnly || g.tested) &&
                (q.isEmpty() || (listOf(g.title, g.creator) + g.aliases).any { CoverCatalog.norm(it).contains(q) })
        }
    }

    /** The store entry for a game in the library, by title or alias, or null. */
    fun match(all: List<StoreGame>, title: String): StoreGame? {
        val t = CoverCatalog.norm(title)
        if (t.isEmpty()) return null
        return all.firstOrNull { g -> (listOf(g.title) + g.aliases).any { CoverCatalog.norm(it) == t } }
    }

    /** True when [store] is a higher version than [installed] ("1.10" > "1.9", "v2.0" > "1.5.3"). Unknown versions are never newer. */
    fun isNewer(store: String, installed: String): Boolean {
        fun parts(v: String) = Regex("\\d+").findAll(v).map { it.value.toIntOrNull() ?: 0 }.toList()
        val a = parts(store); val b = parts(installed)
        if (a.isEmpty() || b.isEmpty()) return false
        for (i in 0 until maxOf(a.size, b.size)) {
            val x = a.getOrElse(i) { 0 }; val y = b.getOrElse(i) { 0 }
            if (x != y) return x > y
        }
        return false
    }
}
