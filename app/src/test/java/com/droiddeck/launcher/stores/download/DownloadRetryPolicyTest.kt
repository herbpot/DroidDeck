package com.droiddeck.launcher.stores.download

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class DownloadRetryPolicyTest {
    @Test fun backoffIsBounded() {
        assertEquals(5_000L, DownloadRetryPolicy.delayMs(1))
        assertEquals(15_000L, DownloadRetryPolicy.delayMs(2))
        assertEquals(45_000L, DownloadRetryPolicy.delayMs(3))
    }

    @Test fun transientEpicErrorsAreRetried() {
        assertTrue(DownloadRetryPolicy.shouldRetry(DownloadStage.DOWNLOAD, "Network connection reset"))
        assertTrue(DownloadRetryPolicy.shouldRetry(DownloadStage.MANIFEST, "HTTP 503 Service Unavailable"))
        assertTrue(DownloadRetryPolicy.shouldRetry(DownloadStage.DOWNLOAD, "one or more chunks failed to download"))
        assertTrue(DownloadRetryPolicy.shouldRetry(DownloadStage.DOWNLOAD, "HTTP 429"))
        assertTrue(DownloadRetryPolicy.shouldRetry(DownloadStage.MANIFEST, "The manifest could not be fetched"))
    }

    @Test fun permanentErrorsAreNotRetried() {
        assertFalse(DownloadRetryPolicy.shouldRetry(DownloadStage.MANIFEST, "Not signed in to Epic Games"))
        assertFalse(DownloadRetryPolicy.shouldRetry(DownloadStage.DOWNLOAD, "HTTP 403 Forbidden"))
        assertFalse(DownloadRetryPolicy.shouldRetry(DownloadStage.DOWNLOAD, "insufficient storage space"))
        assertFalse(DownloadRetryPolicy.shouldRetry(DownloadStage.DOWNLOAD, "download failed: ENOSPC"))
        assertFalse(DownloadRetryPolicy.shouldRetry(DownloadStage.INSTALL, "network timeout"))
        assertFalse(DownloadRetryPolicy.shouldRetry(DownloadStage.VERIFY, "checksum mismatch"))
        assertFalse(DownloadRetryPolicy.shouldRetry(DownloadStage.DOWNLOAD, "install failed"))
        assertFalse(DownloadRetryPolicy.shouldRetry(DownloadStage.DOWNLOAD, null))
    }
}
