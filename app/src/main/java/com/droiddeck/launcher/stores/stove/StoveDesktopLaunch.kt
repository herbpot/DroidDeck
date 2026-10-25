package com.droiddeck.launcher.stores.stove

import java.io.File

/**
 * One-shot LXQt autostart into the user's existing Linux STOVE bridge.
 * No Windows executables, registry values or Proton prefixes are modified.
 */
object StoveDesktopLaunch {
    private const val LAUNCHER = "root/.local/bin/droiddeck-stove"
    private const val AUTOSTART = "root/.config/autostart/droiddeck-stove-once.desktop"

    fun isInstalled(root: File): Boolean = File(root, LAUNCHER).isFile

    /** Called only if no session is running: leaving an autostart request is not safe otherwise. */
    fun arm(root: File): Boolean {
        if (!isInstalled(root)) return false
        val entry = File(root, AUTOSTART)
        return runCatching {
            check(entry.parentFile?.isDirectory == true || entry.parentFile?.mkdirs() == true)
            entry.writeText(desktopEntry())
            true
        }.getOrDefault(false)
    }

    fun isPending(root: File): Boolean = File(root, AUTOSTART).isFile

    fun cancel(root: File): Boolean = !File(root, AUTOSTART).exists() || File(root, AUTOSTART).delete()

    internal fun desktopEntry(): String = """[Desktop Entry]
Type=Application
Name=STOVE (DroidDeck)
Exec=/bin/sh -c "rm -f /root/.config/autostart/droiddeck-stove-once.desktop; exec /root/.local/bin/droiddeck-stove"
Terminal=false
X-GNOME-Autostart-enabled=true
"""
}
