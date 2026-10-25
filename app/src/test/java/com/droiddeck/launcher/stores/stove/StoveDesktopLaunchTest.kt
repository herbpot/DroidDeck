package com.droiddeck.launcher.stores.stove

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.file.Files

class StoveDesktopLaunchTest {
    @Test fun cannotArmWithoutInstalledLauncher() {
        val root = Files.createTempDirectory("stove-empty").toFile()
        try {
            assertFalse(StoveDesktopLaunch.arm(root))
            assertFalse(root.resolve("root/.config/autostart/droiddeck-stove-once.desktop").exists())
        } finally { root.deleteRecursively() }
    }

    @Test fun oneShotAutostartIsRemovable() {
        val root = Files.createTempDirectory("stove-test").toFile()
        try {
            val launcher = root.resolve("root/.local/bin/droiddeck-stove")
            launcher.parentFile.mkdirs()
            launcher.writeText("#!/bin/sh\n")
            assertTrue(StoveDesktopLaunch.isInstalled(root))
            assertTrue(StoveDesktopLaunch.arm(root))
            val entry = root.resolve("root/.config/autostart/droiddeck-stove-once.desktop")
            assertTrue(entry.readText().contains("exec /root/.local/bin/droiddeck-stove"))
            assertTrue(entry.readText().contains("rm -f /root/.config/autostart/droiddeck-stove-once.desktop"))
            assertTrue(StoveDesktopLaunch.isPending(root))
            assertTrue(StoveDesktopLaunch.cancel(root))
            assertFalse(StoveDesktopLaunch.isPending(root))
            assertFalse(entry.exists())
            assertEquals("#!/bin/sh\n", launcher.readText())
        } finally { root.deleteRecursively() }
    }
}
