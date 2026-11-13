package io.github.nikalbazu24.gahvoria.data

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import io.github.nikalbazu24.gahvoria.core.Palettes

/**
 * The launcher icon in the colors of the chosen palette.
 *
 * Android cannot repaint an icon: the manifest declares one entry ("activity-alias") per palette and
 * only the chosen one is enabled. Changing it never closes the app (DONT_KILL_APP). Some launchers
 * take a few seconds to notice, and a shortcut the player pinned by hand may need to be added again.
 */
object AppIcon {
    private fun alias(context: Context, id: String) =
        ComponentName(context.packageName, "io.github.nikalbazu24.gahvoria.Icon" + id.replaceFirstChar { it.uppercase() })

    /** Enable the icon of palette [id] (or the default one when [follow] is false); the others go off. */
    fun sync(context: Context, id: String, follow: Boolean) {
        val want = if (follow) Palettes.byId(id).id else Palettes.DEFAULT
        val pm = context.packageManager
        runCatching {
            val target = alias(context, want)
            // Turn the new one on first so that there is never a moment without an icon.
            pm.setComponentEnabledSetting(target, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
            for (p in Palettes.all) if (p.id != want) {
                val other = alias(context, p.id)
                if (pm.getComponentEnabledSetting(other) != PackageManager.COMPONENT_ENABLED_STATE_DISABLED)
                    pm.setComponentEnabledSetting(other, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
            }
        }
    }
}
