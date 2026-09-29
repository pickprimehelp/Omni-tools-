
package com.example.util

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdMobManager {

    private const val TAG = "AdMobManager"

    // Google official TEST ad unit IDs
    const val BANNER_AD_UNIT_ID =
        "ca-app-pub-3940256099942544/6300978111"

    const val INTERSTITIAL_AD_UNIT_ID =
        "ca-app-pub-3940256099942544/1033173712"

    const val REWARDED_AD_UNIT_ID =
        "ca-app-pub-3940256099942544/5224354917"

    private var isInitialized = false

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false

    private var rewardedAd: RewardedAd? = null
    private var isRewardedLoading = false

    // ---------------- INITIALIZE ADMOB ----------------

    fun initialize(context: Context) {
        if (isInitialized) {
            Log.d(TAG, "AdMob already initialized")
            return
        }

        val appContext = context.applicationContext

        Log.d(TAG, "Initializing Google Mobile Ads SDK")

        MobileAds.initialize(appContext) { status ->
            isInitialized = true

            Log.d(TAG, "AdMob SDK initialization completed")

            status.adapterStatusMap.forEach { (adapter, adapterStatus) ->
                Log.d(
                    TAG,
                    "Adapter: $adapter, Status: ${adapterStatus.initializationState}"
                )
            }

            loadInterstitial(appContext)
            loadRewarded(appContext)
        }
    }

    // ---------------- INTERSTITIAL ADS ----------------

    fun loadInterstitial(context: Context) {
        if (!isInitialized) {
            Log.d(TAG, "Interstitial waiting for SDK initialization")
            return
        }

        if (interstitialAd != null || isInterstitialLoading) {
            return
        }

        isInterstitialLoading = true

        Log.d(TAG, "Loading test interstitial ad")

        val request = AdRequest.Builder().build()

        InterstitialAd.load(
            context.applicationContext,
            INTERSTITIAL_AD_UNIT_ID,
            request,
            object : InterstitialAdLoadCallback() {

                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false

                    Log.d(TAG, "Interstitial ad loaded successfully")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading = false

                    Log.e(
                        TAG,
                        "Interstitial failed: ${error.code}, " +
                            "${error.message}, domain=${error.domain}"
                    )
                }
            }
        )
    }

    fun showInterstitial(
        activity: Activity,
        onDismiss: () -> Unit = {}
    ) {
        val ad = interstitialAd

        if (ad == null) {
            Log.d(TAG, "Interstitial not ready; continuing without ad")

            loadInterstitial(activity)
            onDismiss()
            return
        }

        interstitialAd = null

        ad.fullScreenContentCallback =
            object : FullScreenContentCallback() {

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial displayed")
                }

                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial dismissed")

                    loadInterstitial(activity)
                    onDismiss()
                }

                override fun onAdFailedToShowFullScreenContent(
                    adError: AdError
                ) {
                    Log.e(
                        TAG,
                        "Interstitial display failed: ${adError.message}"
                    )

                    loadInterstitial(activity)
                    onDismiss()
                }
            }

        try {
            ad.show(activity)
        } catch (e: Exception) {
            Log.e(TAG, "Error showing interstitial", e)

            loadInterstitial(activity)
            onDismiss()
        }
    }

    // ---------------- REWARDED ADS ----------------

    fun loadRewarded(context: Context) {
        if (!isInitialized) {
            Log.d(TAG, "Rewarded waiting for SDK initialization")
            return
        }

        if (rewardedAd != null || isRewardedLoading) {
            return
        }

        isRewardedLoading = true

        Log.d(TAG, "Loading test rewarded ad")

        val request = AdRequest.Builder().build()

        RewardedAd.load(
            context.applicationContext,
            REWARDED_AD_UNIT_ID,
            request,
            object : RewardedAdLoadCallback() {

                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isRewardedLoading = false

                    Log.d(TAG, "Rewarded ad loaded successfully")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isRewardedLoading = false

                    Log.e(
                        TAG,
                        "Rewarded failed: ${error.code}, " +
                            "${error.message}, domain=${error.domain}"
                    )
                }
            }
        )
    }

    fun showRewarded(
        activity: Activity,
        onUserEarnedReward: () -> Unit = {},
        onDismiss: () -> Unit = {}
    ) {
        val ad = rewardedAd

        if (ad == null) {
            Log.d(TAG, "Rewarded ad not ready")

            loadRewarded(activity)
            onDismiss()
            return
        }

        rewardedAd = null

        ad.fullScreenContentCallback =
            object : FullScreenContentCallback() {

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Rewarded ad displayed")
                }

                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Rewarded ad dismissed")

                    loadRewarded(activity)
                    onDismiss()
                }

                override fun onAdFailedToShowFullScreenContent(
                    adError: AdError
                ) {
                    Log.e(
                        TAG,
                        "Rewarded display failed: ${adError.message}"
                    )

                    loadRewarded(activity)
                    onDismiss()
                }
            }

        try {
            ad.show(activity) { rewardItem ->
                Log.d(
                    TAG,
                    "Reward earned: ${rewardItem.amount} ${rewardItem.type}"
                )

                onUserEarnedReward()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing rewarded ad", e)

            loadRewarded(activity)
            onDismiss()
        }
    }
}

// ---------------- BANNER ADS ----------------

@Composable
fun AdMobBannerView(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),

        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = AdMobManager.BANNER_AD_UNIT_ID

                adListener = object : AdListener() {

                    override fun onAdLoaded() {
                        Log.d(
                            "AdMobManager",
                            "Banner ad loaded successfully"
                        )
                    }

                    override fun onAdFailedToLoad(
                        error: LoadAdError
                    ) {
                        Log.e(
                            "AdMobManager",
                            "Banner failed: ${error.code}, " +
                                "${error.message}, domain=${error.domain}"
                        )
                    }

                    override fun onAdImpression() {
                        Log.d(
                            "AdMobManager",
                            "Banner impression recorded"
                        )
                    }
                }

                Log.d("AdMobManager", "Loading test banner")

                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
