package io.github.nikalbazu24.gahvoria.core

/** A published version, read from version.json in the "dev" release. */
data class Release(val versionCode: Int, val versionName: String, val apk: String) {

    fun isNewerThan(installedCode: Int) = versionCode > installedCode

    companion object {
        /** Reads only the fields Gahvoria needs; anything else in the file is ignored. */
        fun parse(json: String): Release? {
            val code = Regex("\"versionCode\"\\s*:\\s*(\\d+)").find(json)?.groupValues?.get(1)?.toIntOrNull() ?: return null
            val name = Regex("\"versionName\"\\s*:\\s*\"([^\"]*)\"").find(json)?.groupValues?.get(1) ?: return null
            val apk = Regex("\"apk\"\\s*:\\s*\"([^\"]*)\"").find(json)?.groupValues?.get(1) ?: "gahvoria.apk"
            if (!apk.matches(Regex("[A-Za-z0-9._-]+\\.apk"))) return null
            return Release(code, name, apk)
        }
    }
}

/** Where the updater looks: "dev" (every build) or "stable" (a version the maintainer chose to promote). */
object UpdateChannel {
    const val DEV = "dev"
    const val STABLE = "stable"

    fun valid(id: String?) = id == DEV || id == STABLE

    /** A build's development URL can point to the official or experimental release. */
    fun base(devBase: String, channel: String): String {
        val development = devBase.trimEnd('/')
        if (channel != STABLE) return "$development/"
        return development.substringBeforeLast('/') + "/$STABLE/"
    }
}
