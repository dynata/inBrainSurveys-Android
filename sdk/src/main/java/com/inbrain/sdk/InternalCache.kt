package com.inbrain.sdk

import android.os.Handler
import android.os.Looper
import android.util.LruCache

class InternalCache {

    private val cache: LruCache<String, CacheEntry> = LruCache(MAX_CACHE_SIZE)
    private val handler: Handler = Handler(Looper.getMainLooper())

    fun put(key: String, value: Any) {
        cache.put(key, CacheEntry(value))
        handler.postDelayed({
            cache.remove(key)
        }, EXPIRATION_TIME)
    }

    fun get(key: String): Any? {
        val entry = cache.get(key)
        return if (entry != null) {
            if (entry.isExpired()) {
                cache.remove(key)
                null
            } else {
                entry.value
            }
        } else {
            null
        }
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