package io.github.nikalbazu24.gahvoria.data

import android.app.Activity
import com.hatkid.mkxpz.GameActivity
import io.github.nikalbazu24.gahvoria.core.Game
import io.github.nikalbazu24.gahvoria.core.GameProfile
import io.github.nikalbazu24.gahvoria.core.Saves
import java.io.File

/** Opens a game with the reused mkxp-z engine. */
object Launcher {

    fun classicAvailable(a: Activity) = GameActivity.classicAvailable(a)

    fun saveDir(game: Game): File {
        val dir = File(game.path)
        return Saves.dirFor(dir, game.title, Storage.root(), Storage.canWrite(dir))
    }

    /** What the game's scripts say (Essentials version...). Cheap enough to read when a game is opened or shown. */
    fun profile(game: Game): GameProfile = GameProfile.read(File(game.path))

    /** The engine "auto" picks: classic for Essentials 17-18 (Ruby 1.8 era), modern otherwise. */
    fun autoEngine(game: Game): String = profile(game).recommendedEngine

    /** The engine a game really starts with. The classic one only when the APK carries it. */
    fun engineFor(a: Activity, game: Game): Int {
        val wanted = if (game.engine == Game.ENGINE_AUTO) autoEngine(game) else game.engine
        return if (wanted == Game.ENGINE_CLASSIC && classicAvailable(a)) GameActivity.ENGINE_CLASSIC else GameActivity.ENGINE_MODERN
    }

    fun play(a: Activity, game: Game) {
        val dir = File(game.path)
        val saves = saveDir(game)
        a.startActivity(
            GameActivity.intent(a, dir, game.title, saves, engineFor(a, game), GameActivity.SCREEN_ORIGINAL,
                Storage.rtpRoot(), game.touch)
        )
    }
}
