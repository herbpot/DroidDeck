package com.droiddeck.launcher.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.droiddeck.launcher.runtime.LinuxRuntime
import com.droiddeck.launcher.session.SessionState
import com.droiddeck.launcher.stores.stove.StoveDesktopLaunch

/** STOVE is a Windows desktop client, not an Android OAuth/library API. */
@Composable
internal fun StovePane(actions: FrontEndActions) {
    val ctx = LocalContext.current
    val root = remember(ctx) { LinuxRuntime.rootDir(ctx) }
    var message by remember { mutableStateOf<String?>(null) }
    val installed = StoveDesktopLaunch.isInstalled(root)
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("STOVE", style = MaterialTheme.typography.headlineMedium)
        Text("PC client · Proton / Linux desktop", style = MaterialTheme.typography.bodyMedium)
        Text(if (installed) "Existing STOVE launcher found" else "STOVE launcher not configured",
            style = MaterialTheme.typography.titleSmall)
        Text("Uses your current STOVE installation. Sign-in, updates, and game downloads remain in the official PC client.",
            style = MaterialTheme.typography.bodyMedium)
        PrimaryButton("Launch STOVE", main = true) {
            when {
                !installed -> message = "Install the DroidDeck STOVE unified bridge in the Linux desktop first. Existing games will be preserved."
                SessionState.running -> message = "A session is already running. Open the desktop and run droiddeck-stove from its menu or terminal."
                StoveDesktopLaunch.arm(root) -> {
                    message = "Starting the Linux desktop and STOVE..."
                    actions.onDesktop()
                }
                else -> message = "Unable to register a one-time STOVE launch. No existing installation was changed."
            }
        }
        PrimaryButton("Open Linux desktop") { actions.onDesktop() }
        if (StoveDesktopLaunch.isPending(root)) {
            PrimaryButton("Cancel pending STOVE launch") {
                message = if (StoveDesktopLaunch.cancel(root)) "Pending STOVE launch canceled." else "Could not remove the pending launcher."
            }
        }
        message?.let { Note(it) }
        Spacer(Modifier.height(2.dp))
        Text("Google sign-in returns through the Linux sgup:// handler. Configure or repair that bridge in the desktop if the browser cannot find STOVE.",
            style = MaterialTheme.typography.bodySmall)
        Text("Game library sync and direct installations are not supported in this preview.",
            style = MaterialTheme.typography.bodySmall)
    }
}
