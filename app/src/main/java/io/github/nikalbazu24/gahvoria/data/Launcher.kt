package io.github.nikalbazu24.gahvoria.data

import android.app.Activity
import com.hatkid.mkxpz.GameActivity
import io.github.nikalbazu24.gahvoria.core.Game
import io.github.nikalbazu24.gahvoria.core.Saves
import java.io.File

/** Opens a game with the reused mkxp-z engine. */
object Launcher {

    fun classicAvailable(a: Activity) = GameActivity.classicAvailable(a)

    fun saveDir(game: Game): File {
        val dir = File(game.path)
        return Saves.dirFor(dir, game.title, Storage.root(), Storage.canWrite(dir))
    }

    /** "auto" uses the modern engine (Ruby 3.1); the classic one (Ruby 1.8) only when chosen. */
    fun engineFor(a: Activity, game: Game) =
        if (game.engine == Game.ENGINE_CLASSIC && classicAvailable(a)) GameActivity.ENGINE_CLASSIC else GameActivity.ENGINE_MODERN

    fun play(a: Activity, game: Game) {
        val dir = File(game.path)
        val saves = saveDir(game)
        a.startActivity(
            GameActivity.intent(a, dir, game.title, saves, engineFor(a, game), GameActivity.SCREEN_ORIGINAL,
                Storage.rtpRoot(), game.touch)
        )
    }
}
