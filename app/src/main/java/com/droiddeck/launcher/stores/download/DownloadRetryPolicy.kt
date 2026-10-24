package com.droiddeck.launcher.stores.download

internal object DownloadRetryPolicy {
    const val MAX_RETRIES = 3

    fun delayMs(retryCount: Int): Long = when (retryCount) {
        1 -> 5_000L
        2 -> 15_000L
        else -> 45_000L
    }

    fun shouldRetry(stage: DownloadStage, error: String?): Boolean {
        if (stage != DownloadStage.MANIFEST && stage != DownloadStage.DOWNLOAD) return false
        val message = error?.lowercase()?.trim().orEmpty()
        if (message.isEmpty()) return false

        val permanent = listOf(
            "not signed in", "sign in", "login", "authentication", "authorization",
            "unauthorized", "forbidden", "permission", "license", "not owned",
            "invalid token", "expired token", "storage", "no space", "disk full",
            "out of space", "insufficient space", "enospc", "edquot", "read-only", "checksum",
            "hash mismatch", "corrupt", "unsupported", "invalid manifest",
            "decompress", "extract failed", "write failed",
        )
        if (permanent.any(message::contains)) return false
        if (Regex("""\b(?:400|401|403|404|405|409|410|422)\b""").containsMatchIn(message)) return false

        return listOf(
            "network", "timeout", "timed out", "connection", "broken pipe",
            "unreachable", "dns", "name resolution", "temporary failure",
            "socketexception", "failed to fetch", "could not be fetched", "manifest fetch failed", "failed to download",
            "download failed", "chunks failed", "cdn failed",
            "http 408", "http 429", "status 408", "status 429",
            "bad gateway", "service unavailable", "gateway timeout", "too many requests",
        ).any(message::contains) || Regex("""\b(?:http|status)\s*5\d\d\b""").containsMatchIn(message)
    }
}
