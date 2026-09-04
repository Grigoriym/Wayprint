package com.grappim.wayprint.core.storage.di

/**
 * Provides [com.grappim.wayprint.core.storage.TracksStorage] with a platform-resolved directory
 * (Android's `context.filesDir`, JVM's per-user app-data directory) — the one piece of "where do
 * tracks live" that `TracksStorage` itself deliberately doesn't know (CLAUDE.md's settled
 * decision: it takes a caller-resolved [kotlinx.io.files.Path], not a `Context`).
 */
expect class PlatformStorageModule
