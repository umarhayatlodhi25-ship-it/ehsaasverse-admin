package com.lodhidevelop.ehsaasverse

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdsManager {
    private var mInterstitialAd: InterstitialAd? = null
    private var isLoading = false
    
    // Real Ad Unit IDs
    const val BANNER_AD_ID = "ca-app-pub-1467815422214490/2615950854"
    const val INTERSTITIAL_AD_ID = "ca-app-pub-1467815422214490/3105915765"
    const val NATIVE_AD_ID = "ca-app-pub-1467815422214490/9479752420"
    const val REWARDED_AD_ID = "ca-app-pub-1467815422214490/5656057483"
    const val APP_OPEN_AD_ID = "ca-app-pub-1467815422214490/5808786485"

    /* Test Ad Unit IDs (Google Sample IDs)
    const val TEST_BANNER_AD_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_AD_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_NATIVE_AD_ID = "ca-app-pub-3940256099942544/2247696110"
    const val TEST_REWARDED_AD_ID = "ca-app-pub-3940256099942544/5224354917"
    const val TEST_APP_OPEN_AD_ID = "ca-app-pub-3940256099942544/9257391924"
    */

    fun loadInterstitialAd(context: Context) {
        if (isLoading || mInterstitialAd != null) return
        isLoading = true
        
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, INTERSTITIAL_AD_ID, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                android.util.Log.e("AdsManager", "Ad Failed to Load: ${adError.message}, Code: ${adError.code}")
                mInterstitialAd = null
                isLoading = false
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                android.util.Log.d("AdsManager", "Ad Loaded Successfully")
                mInterstitialAd = interstitialAd
                isLoading = false
            }
        })
    }

    fun showInterstitialAd(context: Context, onAdDismissed: () -> Unit) {
        if (mInterstitialAd != null && context is Activity) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    mInterstitialAd = null
                    loadInterstitialAd(context)
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    mInterstitialAd = null
                    onAdDismissed()
                }
            }
            mInterstitialAd?.show(context)
        } else {
            onAdDismissed()
            if (mInterstitialAd == null) {
                loadInterstitialAd(context)
            }
        }
    }
}
