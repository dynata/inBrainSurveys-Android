package com.inbrain.sdk

import android.content.Context
import android.content.SharedPreferences

object PreferenceUtil {
    private const val PREFERENCES = "SharedPreferences_inBrain25930"
    private const val PREFERENCE_DEVICE_ID = "529826892"
    private const val PREFERENCE_PENDING_REWARDS = "372131_f4lied"

    private var preferences: SharedPreferences? = null

    fun init(context: Context) {
        if (preferences == null) {
            preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        }
    }

    fun saveDeviceId(deviceId: String) {
        if (preferences != null) {
            preferences!!.edit().putString(PREFERENCE_DEVICE_ID, deviceId).apply()
        }
    }

    fun getDeviceId(): String? {
        if (preferences != null && preferences!!.contains(PREFERENCE_DEVICE_ID)) {
            return preferences!!.getString(PREFERENCE_DEVICE_ID, null)
        }
        return null
    }

    fun savePendingRewards(rewardsIds: Set<Long>?) {
        if (preferences == null)
            return

        if (rewardsIds == null) {
            preferences!!.edit()
                .putStringSet(PREFERENCE_PENDING_REWARDS, null)
                .apply()
            return
        }
        val set: MutableSet<String> = HashSet()
        for (id in rewardsIds) set.add(id.toString())
        preferences!!.edit()
            .putStringSet(PREFERENCE_PENDING_REWARDS, set)
            .apply()
    }

    fun getPendingRewardIds(): MutableSet<Long> {
        val ids: MutableSet<Long> = HashSet()
        if (preferences != null) {
            val set: Set<String>? = preferences!!.getStringSet(PREFERENCE_PENDING_REWARDS, null)
            if (set != null) {
                for (stringNumber in set) {
                    try {
                        ids.add(stringNumber.toLong())
                    } catch (ignored: Exception) {
                    }
                }
            }
        }
        return ids
    }
}