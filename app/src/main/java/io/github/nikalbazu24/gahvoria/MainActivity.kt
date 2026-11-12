package io.github.nikalbazu24.gahvoria

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import io.github.nikalbazu24.gahvoria.ui.AppState
import io.github.nikalbazu24.gahvoria.ui.GahvoriaApp

/** Gahvoria's only screen host: everything else is Compose. Games open in the engine's GameActivity. */
class MainActivity : ComponentActivity() {
    private lateinit var state: AppState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        state = AppState(this)
        intent?.getStringExtra(EXTRA_NOTICE)?.let { state.notice = it }
        setContent { GahvoriaApp(state) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getStringExtra(EXTRA_NOTICE)?.let { state.notice = it }
    }

    override fun onResume() {
        super.onResume()
        state.onResume()
    }

    /** Controller: A presses like a tap and B goes back, as on any console. */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val mapped = when (event.keyCode) {
            KeyEvent.KEYCODE_BUTTON_A -> KeyEvent.KEYCODE_DPAD_CENTER
            KeyEvent.KEYCODE_BUTTON_B -> KeyEvent.KEYCODE_BACK
            else -> return super.dispatchKeyEvent(event)
        }
        return super.dispatchKeyEvent(KeyEvent(event.downTime, event.eventTime, event.action, mapped, event.repeatCount,
            event.metaState, event.deviceId, event.scanCode, event.flags, event.source))
    }

    companion object {
        /** A message for the player, shown in Gahvoria's own style (sent by LaunchActivity when a game cannot open). */
        const val EXTRA_NOTICE = "gahvoria.NOTICE"
    }
}
