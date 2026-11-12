package io.github.nikalbazu24.gahvoria.data

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Checks a DeepL key the player pasted, so the app can say right away whether it works. It translates nothing. */
object Deepl {

    data class Result(val ok: Boolean, val saved: Boolean, val message: String)

    /** Free keys end in ":fx" and use the free server; the others use the paid one. */
    fun host(key: String) = if (key.trim().endsWith(":fx")) "https://api-free.deepl.com" else "https://api.deepl.com"

    fun verify(key: String): Result {
        return try {
            val c = URL(host(key) + "/v2/usage").openConnection() as HttpURLConnection
            c.connectTimeout = 10_000
            c.readTimeout = 15_000
            c.setRequestProperty("Authorization", "DeepL-Auth-Key " + key.trim())
            when (val code = c.responseCode) {
                200 -> {
                    val j = JSONObject(c.inputStream.bufferedReader().use { it.readText() })
                    val used = j.optLong("character_count", -1)
                    val limit = j.optLong("character_limit", -1)
                    val use = if (used >= 0 && limit > 0) " Usados ${fmt(used)} de ${fmt(limit)} caracteres este período." else ""
                    Result(true, true, "Clave guardada y válida.$use")
                }
                403 -> Result(false, true, "DeepL no aceptó la clave. Guardada, pero revisa que la copiaste completa (las gratuitas terminan en :fx).")
                456 -> Result(false, true, "La clave es válida, pero ya no te quedan caracteres este período.")
                else -> Result(false, true, "Guardé la clave, pero DeepL respondió con el código $code y no pude comprobarla.")
            }
        } catch (e: Exception) {
            Result(false, true, "Guardé la clave, pero no pude comprobarla ahora: ${if (e is java.io.IOException) "sin conexión a Internet" else "error inesperado"}.")
        }
    }

    private fun fmt(n: Long) = String.format(java.util.Locale.GERMANY, "%,d", n)
}
