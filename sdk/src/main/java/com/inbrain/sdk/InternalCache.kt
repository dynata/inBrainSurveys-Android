package com.inbrain.sdk

import android.util.LruCache

class InternalCache {

    private val cache: LruCache<String, CacheEntry> = LruCache(MAX_CACHE_SIZE)

    fun put(key: String, value: Any) {
        cache.put(key, CacheEntry(value))
    }

    fun get(key: String): Any? {
        val entry = cache.get(key) ?: return null
        if (entry.isExpired()) {
            cache.remove(key)
            return null
        }
        return entry.value
    }

    private inner class CacheEntry(val value: Any) {
        private val timestamp: Long = System.currentTimeMillis()

        fun isExpired(): Boolean {
            return System.currentTimeMillis() - timestamp > EXPIRATION_TIME
        }
    }

    companion object {
        private const val MAX_CACHE_SIZE = 1
        private const val EXPIRATION_TIME: Long = 30 * 60 * 1000 // 30 minute

        const val KEY_SURVEYS_AVAILABILITY = "surveys_availability"
    }
}