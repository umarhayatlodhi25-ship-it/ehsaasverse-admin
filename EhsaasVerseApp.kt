package com.lodhidevelop.ehsaasverse

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

class EhsaasVerseApp : Application(), Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    private var appOpenAdManager: AppOpenAdManager? = null
    private var currentActivity: Activity? = null

    override fun onCreate() {
        super.onCreate()
        
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
        } catch (e: Exception) {
            android.util.Log.e("EhsaasVerseApp", "Firebase init error", e)
        }
        
        registerActivityLifecycleCallbacks(this)
        
        // Clear test device IDs to ensure real ads can load
        val configuration = MobileAds.getRequestConfiguration().toBuilder()
            .setTestDeviceIds(emptyList()) 
            .build()
        MobileAds.setRequestConfiguration(configuration)

        try {
            MobileAds.initialize(this) { status ->
                android.util.Log.d("EhsaasVerseApp", "AdMob Initialized")
            }
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
            appOpenAdManager = AppOpenAdManager()
            appOpenAdManager?.loadAd() 
        } catch (e: Exception) {
            android.util.Log.e("EhsaasVerseApp", "AdMob init error", e)
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        currentActivity?.let { appOpenAdManager?.showAdIfAvailable(it) }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }
    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        currentActivity = null
    }

    inner class AppOpenAdManager {
        private var appOpenAd: AppOpenAd? = null
        private var isLoadingAd = false
        private var loadTime: Long = 0

        fun loadAd() {
            if (isLoadingAd || isAdAvailable()) return
            isLoadingAd = true
            val request = AdRequest.Builder().build()
            AppOpenAd.load(
                this@EhsaasVerseApp,
                AdsManager.APP_OPEN_AD_ID,
                request,
                object : AppOpenAd.AppOpenAdLoadCallback() {
                    override fun onAdLoaded(ad: AppOpenAd) {
                        appOpenAd = ad
                        isLoadingAd = false
                        loadTime = Date().time
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        isLoadingAd = false
                    }
                }
            )
        }

        private fun isAdAvailable(): Boolean {
            return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
        }

        private fun wasLoadTimeLessThanNHoursAgo(numHours: Int): Boolean {
            val dateDifference: Long = Date().time - loadTime
            val numMilliSecondsPerHour: Long = 3600000
            return dateDifference < numMilliSecondsPerHour * numHours
        }

        fun showAdIfAvailable(activity: Activity) {
            if (!isAdAvailable()) {
                loadAd()
                return
            }

            appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    appOpenAd = null
                    isLoadingAd = false
                    loadAd()
                }

                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    appOpenAd = null
                    isLoadingAd = false
                    loadAd()
                }
            }
            appOpenAd?.show(activity)
        }
    }
}
