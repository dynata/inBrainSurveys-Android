package com.inbrain.sdk

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.webkit.WebView
import com.inbrain.sdk.ConfirmRewardsExecutor.ConfirmRewardsCallback
import com.inbrain.sdk.FetchCurrencySaleExecutor.CurrencySaleExecutorCallback
import com.inbrain.sdk.GetNativeSurveysListExecutor.NativeSurveysExecutorCallback
import com.inbrain.sdk.RewardsExecutor.RequestRewardsCallback
import com.inbrain.sdk.SurveysAvailabilityExecutor.SurveysAvailableExecutorCallback
import com.inbrain.sdk.TokenExecutor.TokenCallback
import com.inbrain.sdk.callback.*
import com.inbrain.sdk.model.*
import java.util.regex.Pattern


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

    fun setApiClientId(clientId: String?) {
        this.apiClientID = clientId
    }

    fun setApiSecret(apiSecret: String?) {
        this.apiSecret = apiSecret
    }

    fun setIsS2S(isS2S: Boolean) {
        this.isS2S = isS2S
    }

    fun setDeviceId(deviceId: String?) {
        this.deviceId = deviceId
    }

    fun setUserId(userId: String?) {
        this.userID = userId;
    }

    fun addCallback(callback: InBrainCallback?) {
        callbacksList.add(callback)
    }

    fun removeCallback(callback: InBrainCallback?) {
        callbacksList.remove(callback)
    }

    private fun checkForInit(): Boolean {
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

    fun canStartSurveys(context: Context, callback: StartSurveysCallback): Boolean {
        if (!checkForInit()) {
            handler.post { callback.onFail("SDK not initialized") }
            return false
        }
        // todo pay attention to minimal required chrome version, old devices may have updates
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            try {
                val webView = WebView(context)
                val userAgent = webView.settings.userAgentString
                val pattern = Pattern.compile(
                    "chrome/(\\d+)\\.(\\d+)\\.(\\d+)",
                    Pattern.CASE_INSENSITIVE
                )
                val m = pattern.matcher(userAgent)
                val matches = java.util.ArrayList<String>()
                while (m.find()) {
                    for (i in 1..m.groupCount()) {
                        matches.add(m.group(i))
                    }
                }
                if (matches.size > 2) {
                    val group0 = matches[0].toInt()
                    val group1 = matches[1].toInt()
                    val group2 = matches[2].toInt()
                    val group0Matches = group0 >= Constants.MINIMUM_WEBVIEW_VERSION_GROUP_1
                    val group1Matches = group1 >= Constants.MINIMUM_WEBVIEW_VERSION_GROUP_2
                    val group2Matches = group2 >= Constants.MINIMUM_WEBVIEW_VERSION_GROUP_3
                    if (group0Matches) {
                        if (!group1Matches) {
                            handler.post { callback.onFail("Android System WebView version isn't supported") }
                            return false
                        } else if (!group2Matches) {
                            handler.post { callback.onFail("Android System WebView version isn't supported") }
                            return false
                        }
                    } else {
                        handler.post { callback.onFail("Android System WebView version isn't supported") }
                        return false
                    }
                } else {
                    handler.post { callback.onFail("Failed to check webview version, can't start SDK") }
                    return false
                }
            } catch (ex: java.lang.Exception) {
                handler.post { callback.onFail("Failed to check webview version, can't start SDK") }
                return false
            }
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
                            val callback = (params[0] as Array<*>)[0] as GetRewardsCallback?
                            if (callback != null)
                                handler.post { callback.onFailToLoadRewards(t) }
                        }
                        RequestType.ARE_SURVEYS_AVAILABLE -> {
                            val callback = (params[0] as Array<*>)[1] as SurveysAvailableCallback
                            handler.post { callback.onSurveysAvailable(false) }
                        }
                        RequestType.GET_NATIVE_SURVEYS -> {
                            val callback = (params[0] as Array<*>)[3] as GetNativeSurveysCallback
                            handler.post { callback.nativeSurveysReceived(java.util.ArrayList()) }
                        }
                        RequestType.GET_CURRENCY_SALE -> {
                            val callback = (params[0] as Array<*>)[0] as GetCurrencySaleCallback
                            handler.post { callback.currencySaleReceived(null) }
                        }
                        else -> {}
                    }
                }
            })
        } else {
            when (requestType) {
                RequestType.GET_REWARDS -> {
                    val callback = if (params[0] != null) (params[0] as Array<*>)[0] else null
                    requestRewardsWithTokenUpdate(
                        if (callback != null) callback as GetRewardsCallback else null,
                        updateTokenIfRequired
                    )
                }
                RequestType.CONFIRM_REWARDS -> {
                    val pendingRewardIds = (params[0] as Array<*>)[0] as Set<Long>
                    requestConfirmRewards(pendingRewardIds, updateTokenIfRequired)
                }
                RequestType.ARE_SURVEYS_AVAILABLE -> {
                    val context = (params[0] as Array<*>)[0]
                    val callback = (params[0] as Array<*>)[1]
                    requestSurveysAvailabilityWithTokenUpdate(
                        context as Context,
                        callback as SurveysAvailableCallback,
                        updateTokenIfRequired
                    )
                }
                RequestType.GET_NATIVE_SURVEYS -> {
                    val placeId = (params[0] as Array<*>)[0]
                    val includeCate = (params[0] as Array<*>)[1]
                    val excludeCate = (params[0] as Array<*>)[2]
                    val callback = (params[0] as Array<*>)[3]
                    requestNativeSurveysWithTokenUpdate(
                        if (placeId is String) placeId else null,
                        includeCate as List<SurveyCategory>,
                        excludeCate as List<SurveyCategory>,
                        callback as GetNativeSurveysCallback,
                        updateTokenIfRequired
                    )
                }
                RequestType.GET_CURRENCY_SALE -> {
                    val callback = (params[0] as Array<*>)[0]
                    fetchCurrencySaleWithTokenUpdate(
                        callback as GetCurrencySaleCallback,
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
        val surveysAvailabilityExecutor = SurveysAvailabilityExecutor()
        surveysAvailabilityExecutor.areSurveysAvailable(
            context, token, stagingMode,
            object : SurveysAvailableExecutorCallback {
                override fun onSurveysAvailable(available: Boolean) {
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
                        handler.post { callback.onSurveysAvailable(false) }
                    }
                }
            }, userID, deviceId
        )
    }

    private fun requestNativeSurveysWithTokenUpdate(
        placeId: String?,
        includeCategoryIds: List<SurveyCategory>,
        excludeCategoryIds: List<SurveyCategory>,
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

    fun onClosed(byWebView: Boolean, rewards: MutableList<InBrainSurveyReward>) {
        if (callbacksList.isEmpty()) {
            return
        }

        for (callback in callbacksList) {
            if (callback != null) {
                handler.post { callback.surveysClosed(byWebView, rewards) }

                //deprecated functions support
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