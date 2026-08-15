package io.github.allendior.almanac

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.ViewModelProvider
import io.github.allendior.almanac.ui.AlmanacRoot
import io.github.allendior.almanac.ui.AlmanacViewModel
import io.github.allendior.almanac.ui.theme.AlmanacTheme

class MainActivity : FragmentActivity() {

    private lateinit var viewModel: AlmanacViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as AlmanacApp
        viewModel = ViewModelProvider(this, AlmanacViewModel.Factory(app))[AlmanacViewModel::class.java]

        // Cold-start routing (Lock, then Welcome if unseen, then Today) is decided
        // reactively inside the ViewModel once settings have actually loaded from
        // DataStore — not here, since DataStore is async and settings would not yet
        // reflect a saved lock preference at this synchronous point in onCreate.

        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            val repository = remember { app.container.repository }
            AlmanacTheme {
                AlmanacRoot(
                    state = state,
                    viewModel = viewModel,
                    repository = repository,
                    onRequestUnlock = ::promptForUnlock,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Midnight can pass while the app sits open; "today" is re-read, never cached
        // across a day boundary.
        viewModel.refreshToday()
    }

    private fun promptForUnlock() {
        val manager = BiometricManager.from(this)
        val allowed = BiometricManager.Authenticators.BIOMETRIC_WEAK or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL

        if (manager.canAuthenticate(allowed) != BiometricManager.BIOMETRIC_SUCCESS) {
            // No enrolled biometric or device credential: the lock cannot be honoured,
            // and refusing entry would lock the owner out of their own archive.
            viewModel.unlock()
            return
        }

        val prompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    viewModel.unlock()
                }
            },
        )
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Almanac")
                .setSubtitle("Unlock your archive")
                .setAllowedAuthenticators(allowed)
                .build(),
        )
    }
}
