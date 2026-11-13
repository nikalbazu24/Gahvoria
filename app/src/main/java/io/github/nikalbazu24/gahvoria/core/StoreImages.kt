package io.github.nikalbazu24.gahvoria.core

import java.net.URI

/**
 * The picture of a store game comes from the creator's own page (its "og:image", the image the page shows when
 * it is shared), so Gahvoria never keeps a list of image links of its own that could point to something else.
 */
object StoreImages {

    /** Pages that only hold a file or a text: their image is the site's logo, not the game's. */
    private val NO_IMAGE_HOSTS = listOf("mega.nz", "mega.io", "mediafire.com", "pastebin.com", "drive.google.com", "dropbox.com", "docs.google.com")

    fun hasUsefulImage(page: String): Boolean {
        val host = runCatching { URI(page).host }.getOrNull()?.lowercase() ?: return false
        return NO_IMAGE_HOSTS.none { host == it || host.endsWith(".$it") }
    }

    /** The https link of the page's share image, or null. [base] is the page's own address (links may be relative). */
    fun ogImage(html: String, base: String): String? {
        val head = html.take(400_000)
        val tag = Regex("<meta\\b[^>]*>", RegexOption.IGNORE_CASE)
        val key = Regex("""(?:property|name)\s*=\s*["'](og:image(?::secure_url)?|twitter:image(?::src)?)["']""", RegexOption.IGNORE_CASE)
        val content = Regex("""content\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE)
        var best: String? = null
        for (m in tag.findAll(head)) {
            val t = m.value
            if (!key.containsMatchIn(t)) continue
            val raw = content.find(t)?.groupValues?.get(1)?.trim() ?: continue
            val url = runCatching { URI(base).resolve(unescape(raw)).toString() }.getOrNull() ?: continue
            if (!url.startsWith("https://")) continue
            if (best == null || key.find(t)!!.groupValues[1].startsWith("og:image")) best = url
            if (key.find(t)!!.groupValues[1].startsWith("og:image")) break
        }
        return best
    }

    private fun unescape(s: String) = s.replace("&amp;", "&").replace("&#38;", "&").replace("&quot;", "\"").replace("&#x2F;", "/")
}
