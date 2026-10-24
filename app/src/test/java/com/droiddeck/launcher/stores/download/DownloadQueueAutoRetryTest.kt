package com.droiddeck.launcher.stores.download

import com.droiddeck.launcher.stores.Store
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DownloadQueueAutoRetryTest {
    private val app get() = RuntimeEnvironment.getApplication()

    private fun waitForState(key: String, state: DownloadState): DownloadEntry {
        val deadline = System.nanoTime() + 3_000_000_000L
        while (System.nanoTime() < deadline) {
            DownloadQueue.entry(key)?.takeIf { it.state == state }?.let { return it }
            Thread.sleep(10)
        }
        error("Expected $state but got ${DownloadQueue.entry(key)}")
    }

    @Test fun epicNetworkFailureSchedulesRetryAndPauseCancelsCountdown() {
        val e = DownloadEntry(Store.EPIC, "epic-auto-retry-test", "Epic network test")
        val attempts = AtomicInteger()
        try {
            DownloadQueue.enqueue(app, e) {
                object : DownloadQueue.DownloadJob {
                    override fun run(handle: DownloadQueue.JobHandle): String? {
                        handle.stage(DownloadStage.DOWNLOAD)
                        attempts.incrementAndGet()
                        error("network connection reset")
                    }
                    override fun cancel(deleteFiles: Boolean) {}
                }
            }
            val scheduled = waitForState(e.key, DownloadState.RETRYING)
            assertEquals(1, attempts.get())
            assertEquals(1, scheduled.retryCount)
            assertTrue(scheduled.retryAtMs > System.currentTimeMillis())
            DownloadQueue.pause(e.key)
            val paused = waitForState(e.key, DownloadState.PAUSED)
            assertEquals(0L, paused.retryAtMs)
        } finally {
            DownloadQueue.clear(app, e.key)
        }
    }

    @Test fun epicAuthFailureNeverRetries() {
        val e = DownloadEntry(Store.EPIC, "epic-auth-stop-test", "Epic auth test")
        val attempts = AtomicInteger()
        try {
            DownloadQueue.enqueue(app, e) {
                object : DownloadQueue.DownloadJob {
                    override fun run(handle: DownloadQueue.JobHandle): String? {
                        attempts.incrementAndGet()
                        error("Not signed in to Epic Games")
                    }
                    override fun cancel(deleteFiles: Boolean) {}
                }
            }
            val failed = waitForState(e.key, DownloadState.FAILED)
            assertEquals(1, attempts.get())
            assertEquals(0, failed.retryCount)
        } finally {
            DownloadQueue.clear(app, e.key)
        }
    }
}
