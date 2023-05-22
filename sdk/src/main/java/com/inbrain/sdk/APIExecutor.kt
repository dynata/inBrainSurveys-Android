package com.inbrain.sdk

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import com.inbrain.sdk.ConfirmRewardsExecutor.ConfirmRewardsCallback
import com.inbrain.sdk.FetchCurrencySaleExecutor.CurrencySaleExecutorCallback
import com.inbrain.sdk.GetNativeSurveysListExecutor.NativeSurveysExecutorCallback
import com.inbrain.sdk.RewardsExecutor.RequestRewardsCallback
import com.inbrain.sdk.SurveysAvailabilityExecutor.SurveysAvailableExecutorCallback
import com.inbrain.sdk.TokenExecutor.TokenCallback
import com.inbrain.sdk.callback.*
import com.inbrain.sdk.model.*


enum class RequestType {
    GET_REWARDS,
    CONFIRM_REWARDS,
    ARE_SURVEYS_AVAILABLE,
    GET_NATIVE_SURVEYS,
    GET_CURRENCY_SALE
}

internal class APIExecutor {
    private var apiClientID: String? = null
    private var apiSecret: String? = null
    private var isS2S = false
    private var userID: String? = null
    private var deviceId: String? = null

    private var token: String? = null
    private var wrongClientIdError = false

    private val callbacksList: MutableList<InBrainCallback?> = ArrayList()

    private val handler: Handler = Handler(Looper.myLooper()!!)

    private val myCache = InternalCache()

    fun setApiClientId(clientId: String?) {
        this.apiClientID = clientId
    }

    fun getApiClientId(): String? {
        return this.apiClientID
    }

    fun setApiSecret(apiSecret: String?) {
        this.apiSecret = apiSecret
    }

    fun getApiSecret(): String? {
        return this.apiSecret
    }

    fun setIsS2S(isS2S: Boolean) {
        this.isS2S = isS2S
    }

    fun getIsS2S(): Boolean {
        return this.isS2S
    }

    fun setDeviceId(deviceId: String?) {
        this.deviceId = deviceId
    }

    fun getDeviceId(): String? {
        return this.deviceId
    }

    fun setUserId(userId: String?) {
        this.userID = userId;
    }

    fun getUserId(): String? {
        return this.userID
    }

    fun addCallback(callback: InBrainCallback?) {
        callbacksList.add(callback)
    }

    fun removeCallback(callback: InBrainCallback?) {
        callbacksList.remove(callback)
    }

    fun checkForInit(): Boolean {
        if (TextUtils.isEmpty(apiClientID) || TextUtils.isEmpty(apiSecret)) {
            Log.e(Constants.LOG_TAG, "Please first call setInBrain() method!")
            return false
        }
        if (wrongClientIdError) {
            Log.e(Constants.LOG_TAG, "Wrong client id!")
            return false
        }
        return true
    }

    private fun refreshToken(tokenCallback: TokenCallback) {
        val executor = TokenExecutor(stagingMode, apiClientID, apiSecret)
        executor.getToken(object : TokenCallback {
            override fun onGetToken(token: String) {
                this@APIExecutor.token = token
                tokenCallback.onGetToken(token)
            }

            override fun onFailToLoadToken(t: Throwable) {
                if (t is InvalidClientException) {
                    token = null
                    wrongClientIdError = true
                    Log.w(Constants.LOG_TAG, "Invalid client")
                }
                tokenCallback.onFailToLoadToken(t)
            }
        })
    }

    /**
     * execute a request
     */
    fun execute(requestType: RequestType, updateTokenIfRequired: Boolean, vararg params: Any?) {
        if (!checkForInit()) {
            return
        }
        if (TextUtils.isEmpty(token)) {
            refreshToken(object : TokenCallback {
                override fun onGetToken(token: String) {
                    Log.d(Constants.LOG_TAG, "onGetToken: $token")
                    execute(requestType, false, params)
                }

                override fun onFailToLoadToken(t: Throwable) {
                    if (BuildConfig.DEBUG) {
                        Log.e(Constants.LOG_TAG, "Failed to load token");
                        t.printStackTrace()
                    }
                    when (requestType) {
                        RequestType.GET_REWARDS -> {
                            val callback =
                                if (params[0] != null) params[0] as GetRewardsCallback else null
                            if (callback != null) {
                                handler.post { callback.onFailToLoadRewards(t) }
                            }
                        }

                        RequestType.ARE_SURVEYS_AVAILABLE -> {
                            val callback = params[1] as SurveysAvailableCallback
                            handler.post { callback.onSurveysAvailable(false) }
                        }

                        RequestType.GET_NATIVE_SURVEYS -> {
                            val callback = params[3] as GetNativeSurveysCallback
                            handler.post { callback.nativeSurveysReceived(java.util.ArrayList()) }
                        }

                        RequestType.GET_CURRENCY_SALE -> {
                            val callback = params[0] as GetCurrencySaleCallback
                            handler.post { callback.currencySaleReceived(null) }
                        }

                        else -> {}
                    }
                }
            })
        } else {
            when (requestType) {
                RequestType.GET_REWARDS -> {
                    val callback =
                        if (params[0] != null) (params[0] as GetRewardsCallback) else null
                    requestRewardsWithTokenUpdate(
                        callback,
                        updateTokenIfRequired
                    )
                }

                RequestType.CONFIRM_REWARDS -> {
                    @Suppress("UNCHECKED_CAST")
                    val pendingRewardIds = params[0] as Set<Long>
                    requestConfirmRewards(pendingRewardIds, updateTokenIfRequired)
                }

                RequestType.ARE_SURVEYS_AVAILABLE -> {
                    val context = params[0] as Context
                    val callback = params[1] as SurveysAvailableCallback
                    requestSurveysAvailabilityWithTokenUpdate(
                        context,
                        callback,
                        updateTokenIfRequired
                    )
                }

                RequestType.GET_NATIVE_SURVEYS -> {
                    val placeId: Any?
                    val includeCate: Any?
                    val excludeCate: Any?
                    val callback: Any?
                    if (params.size == 1 && params[0] != null) {
                        placeId = (params[0] as Array<*>)[0]
                        includeCate = (params[0] as Array<*>)[1]
                        excludeCate = (params[0] as Array<*>)[2]
                        callback = (params[0] as Array<*>)[3]
                    } else {
                        placeId = params[0]
                        includeCate = params[1]
                        excludeCate = params[2]
                        callback = params[3]
                    }
                    @Suppress("UNCHECKED_CAST")
                    requestNativeSurveysWithTokenUpdate(
                        if (placeId is String) placeId else null,
                        if (includeCate != null) includeCate as List<SurveyCategory> else null,
                        if (excludeCate != null) excludeCate as List<SurveyCategory> else null,
                        callback as GetNativeSurveysCallback,
                        updateTokenIfRequired
                    )
                }

                RequestType.GET_CURRENCY_SALE -> {
                    val callback = params[0] as GetCurrencySaleCallback
                    fetchCurrencySaleWithTokenUpdate(
                        callback,
                        updateTokenIfRequired
                    )
                }
            }
        }
    }

    private fun onGetRewardsSuccess(callback: GetRewardsCallback? = null, rewards: List<Reward>) {
        if (shouldConfirmNewRewards(rewards, callback)) {
            InBrain.getInstance().confirmRewards(rewards)
        }
    }

    private fun shouldConfirmNewRewards(
        rewards: List<Reward>,
        externalCallback: GetRewardsCallback?
    ): Boolean {
        if (externalCallback != null) {
            return externalCallback.handleRewards(rewards) // notify by request
        } else if (callbacksList.isNotEmpty()) {
            var processed = false
            for (callback in callbacksList) {
                if (callback != null && callback.didReceiveInBrainRewards(rewards)) {
                    processed = true
                }
            }
            return processed // confirm by subscriber
        }
        return false // no subscriptions for rewards, leave rewards for next call
    }

    private fun requestRewardsWithTokenUpdate(
        callback: GetRewardsCallback? = null,
        updateToken: Boolean
    ) {
        val rewardsExecutor = RewardsExecutor()
        rewardsExecutor.getRewards(stagingMode, token, object : RequestRewardsCallback {
            override fun onGetRewards(rewards: List<Reward>) {
                onGetRewardsSuccess(callback, rewards)
            }

            override fun onFailToLoadRewards(t: Throwable) {
                if (BuildConfig.DEBUG) {
                    Log.e(Constants.LOG_TAG, "Failed to load rewards")
                    t.printStackTrace()
                }
                if (t is TokenExpiredException) {
                    if (BuildConfig.DEBUG) {
                        Log.e(Constants.LOG_TAG, "Token expired")
                    }
                    if (updateToken) {
                        refreshToken(object : TokenCallback {
                            override fun onGetToken(token: String) {
                                requestRewardsWithTokenUpdate(callback, false)
                            }

                            override fun onFailToLoadToken(t: Throwable) {
                                if (BuildConfig.DEBUG) {
                                    Log.e(Constants.LOG_TAG, "Failed to load token")
                                    t.printStackTrace()
                                }
                                if (callback != null)
                                    handler.post { callback.onFailToLoadRewards(t) }
                            }
                        })
                    } else {
                        if (callback != null)
                            handler.post { callback.onFailToLoadRewards(t) }
                    }
                } else {
                    if (callback != null)
                        handler.post { callback.onFailToLoadRewards(t) }
                }
            }
        }, userID, deviceId)
    }

    private fun requestConfirmRewards(pendingRewardIds: Set<Long>, updateTokenIfRequired: Boolean) {
        val confirmRewardsExecutor = ConfirmRewardsExecutor()
        confirmRewardsExecutor.confirmRewards(
            stagingMode,
            token,
            pendingRewardIds,
            object : ConfirmRewardsCallback {
                override fun onSuccess() {
                    if (BuildConfig.DEBUG) {
                        Log.d(Constants.LOG_TAG, "Successfully confirmed rewards")
                    }
                    val newPendingRewardIds: MutableSet<Long> =
                        PreferenceUtil.getPendingRewardIds() // It might have changed
                    newPendingRewardIds.removeAll(pendingRewardIds)
                    PreferenceUtil.savePendingRewards(newPendingRewardIds)
                }

                override fun onFailed(t: Throwable) {
                    if (updateTokenIfRequired) {
                        if (t is TokenExpiredException) {
                            if (BuildConfig.DEBUG) {
                                Log.e(Constants.LOG_TAG, "Token expired")
                            }
                            refreshToken(object : TokenCallback {
                                override fun onGetToken(token: String) {
                                    val confirmRewardsExecutor1 = ConfirmRewardsExecutor()
                                    confirmRewardsExecutor1.confirmRewards(
                                        stagingMode,
                                        token,
                                        pendingRewardIds,
                                        object : ConfirmRewardsCallback {
                                            override fun onSuccess() {
                                                if (BuildConfig.DEBUG) {
                                                    Log.d(
                                                        Constants.LOG_TAG,
                                                        "Successfully confirmed rewards"
                                                    )
                                                }
                                                val newPendingRewardIds: MutableSet<Long> =
                                                    PreferenceUtil.getPendingRewardIds() // It might have changed
                                                newPendingRewardIds.removeAll(pendingRewardIds)
                                                PreferenceUtil.savePendingRewards(
                                                    newPendingRewardIds
                                                )
                                            }

                                            override fun onFailed(t: Throwable) {
                                                if (BuildConfig.DEBUG) {
                                                    Log.e(
                                                        Constants.LOG_TAG,
                                                        "On failed to confirm rewards:$t"
                                                    )
                                                }
                                            }
                                        },
                                        userID,
                                        deviceId
                                    )
                                }

                                override fun onFailToLoadToken(t: Throwable) {
                                    if (BuildConfig.DEBUG) {
                                        Log.e(Constants.LOG_TAG, "Failed to load token")
                                        t.printStackTrace()
                                    }
                                }
                            })
                        } else {
                            if (BuildConfig.DEBUG) {
                                Log.e(
                                    Constants.LOG_TAG,
                                    "On failed to confirm rewards:$t"
                                )
                            }
                        }
                    }
                }
            },
            userID,
            deviceId
        )
    }

    private fun requestSurveysAvailabilityWithTokenUpdate(
        context: Context,
        callback: SurveysAvailableCallback,
        updateToken: Boolean
    ) {
        val cachedData = myCache.get(InternalCache.KEY_SURVEYS_AVAILABILITY)
        if (cachedData != null) {
            if (BuildConfig.DEBUG) {
                Log.d(Constants.LOG_TAG, "Get surveys availability from cache")
            }
            callback.onSurveysAvailable(cachedData as Boolean)
        } else {
            val surveysAvailabilityExecutor = SurveysAvailabilityExecutor()
            surveysAvailabilityExecutor.areSurveysAvailable(
                token, stagingMode,
                object : SurveysAvailableExecutorCallback {
                    override fun onSurveysAvailable(available: Boolean) {
                        myCache.put(InternalCache.KEY_SURVEYS_AVAILABILITY, available)
                        callback.onSurveysAvailable(available)
                    }

                    override fun onFailToLoadSurveysAvailability(t: Exception) {
                        if (BuildConfig.DEBUG) {
                            Log.e(Constants.LOG_TAG, "Failed to load surveys availability")
                            t.printStackTrace()
                        }
                        if (t is TokenExpiredException) {
                            if (BuildConfig.DEBUG) {
                                Log.e(Constants.LOG_TAG, "Token expired")
                            }
                            if (updateToken) {
                                refreshToken(object : TokenCallback {
                                    override fun onGetToken(token: String) {
                                        requestSurveysAvailabilityWithTokenUpdate(
                                            context,
                                            callback,
                                            false
                                        )
                                    }

                                    override fun onFailToLoadToken(t: Throwable) {
                                        if (BuildConfig.DEBUG) {
                                            Log.e(Constants.LOG_TAG, "Failed to load token")
                                            t.printStackTrace()
                                        }
                                        handler.post { callback.onSurveysAvailable(false) }
                                    }
                                })
                            } else {
                                handler.post { callback.onSurveysAvailable(false) }
                            }
                        } else {
                            myCache.put(InternalCache.KEY_SURVEYS_AVAILABILITY, false)
                            handler.post { callback.onSurveysAvailable(false) }
                        }
                    }
                }, userID, deviceId
            )
        }
    }

    private fun requestNativeSurveysWithTokenUpdate(
        placeId: String?,
        includeCategoryIds: List<SurveyCategory>?,
        excludeCategoryIds: List<SurveyCategory>?,
        callback: GetNativeSurveysCallback,
        updateToken: Boolean
    ) {
        val getNativeSurveysListExecutor = GetNativeSurveysListExecutor()
        getNativeSurveysListExecutor.getNativeSurveysList(
            token, stagingMode,
            object : NativeSurveysExecutorCallback {

                override fun onNativeSurveysAvailable(surveys: MutableList<Survey>?) {
                    callback.nativeSurveysReceived(surveys)
                }

                override fun onFailToLoadNativeSurveysList(ex: Exception) {
                    if (BuildConfig.DEBUG) {
                        Log.e(
                            Constants.LOG_TAG,
                            "Failed to load native surveys: $ex"
                        )
                        ex.printStackTrace()
                    }
                    if (ex is TokenExpiredException) {
                        if (BuildConfig.DEBUG) {
                            Log.e(Constants.LOG_TAG, "Token expired")
                        }
                        if (updateToken) {
                            refreshToken(object : TokenCallback {
                                override fun onGetToken(token: String) {
                                    if (BuildConfig.DEBUG) {
                                        Log.d(
                                            Constants.LOG_TAG,
                                            "onGetToken: $token"
                                        )
                                    }
                                    requestNativeSurveysWithTokenUpdate(
                                        placeId,
                                        includeCategoryIds,
                                        excludeCategoryIds,
                                        callback,
                                        false
                                    )
                                }

                                override fun onFailToLoadToken(t: Throwable) {
                                    if (BuildConfig.DEBUG) {
                                        Log.e(Constants.LOG_TAG, "Failed to load token")
                                        t.printStackTrace()
                                    }
                                    handler.post { callback.nativeSurveysReceived(ArrayList()) }
                                }
                            })
                        } else {
                            handler.post { callback.nativeSurveysReceived(ArrayList()) }
                        }
                    } else {
                        handler.post { callback.nativeSurveysReceived(ArrayList()) }
                    }
                }
            }, userID, deviceId, placeId, includeCategoryIds, excludeCategoryIds
        )
    }

    private fun fetchCurrencySaleWithTokenUpdate(
        callback: GetCurrencySaleCallback,
        updateToken: Boolean
    ) {
        val fetchCurrencySaleExecutor = FetchCurrencySaleExecutor()
        fetchCurrencySaleExecutor.fetchCurrencySale(token, stagingMode,
            object : CurrencySaleExecutorCallback {

                override fun onCurrencySaleAvailable(currencySale: CurrencySale?) {
                    callback.currencySaleReceived(currencySale)
                }

                override fun onFailedToFetchCurrencySale(ex: Exception) {
                    if (BuildConfig.DEBUG) {
                        Log.e(
                            Constants.LOG_TAG,
                            "Failed to fetch currency sale: $ex"
                        )
                        ex.printStackTrace()
                    }
                    if (ex is TokenExpiredException) {
                        if (BuildConfig.DEBUG) {
                            Log.e(Constants.LOG_TAG, "Token expired")
                        }
                        if (updateToken) {
                            refreshToken(object : TokenCallback {
                                override fun onGetToken(token: String) {
                                    if (BuildConfig.DEBUG) {
                                        Log.d(
                                            Constants.LOG_TAG,
                                            "onGetToken: $token"
                                        )
                                    }
                                    fetchCurrencySaleWithTokenUpdate(callback, false)
                                }

                                override fun onFailToLoadToken(t: Throwable) {
                                    if (BuildConfig.DEBUG) {
                                        Log.e(Constants.LOG_TAG, "Failed to load token")
                                        t.printStackTrace()
                                    }
                                    handler.post { callback.currencySaleReceived(null) }
                                }
                            })
                            return
                        }
                    }
                    handler.post { callback.currencySaleReceived(null) }
                }
            })
    }

    fun onClosed(byWebView: Boolean, rewards: MutableList<InBrainSurveyReward>?) {
        if (callbacksList.isEmpty()) {
            return
        }

        for (callback in callbacksList) {
            if (callback != null) {
                handler.post { callback.surveysClosed(byWebView, rewards) }

                //deprecated functions support
                @Suppress("DEPRECATION")
                if (byWebView) {
                    handler.post { callback.surveysClosedFromPage() }
                } else {
                    handler.post { callback.surveysClosed() }
                }
            }
        }
    }

    companion object {
        const val stagingMode = false
    }
}