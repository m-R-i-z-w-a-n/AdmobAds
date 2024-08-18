import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.ViewGroup
import android.view.WindowMetrics
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import app.fitness.fitpal.R
import app.fitness.fitpal.utils.getAdSizeAds
import app.fitness.fitpal.utils.hideView
import app.fitness.fitpal.utils.showView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions

class AdmobAds(private val context: Context) : LifecycleEventObserver {
    private var isViewActive: Boolean

    init {
        isViewActive = true
    }

    private companion object {
        private var interstitialAdDefault: InterstitialAd? = null
        private var interstitialAdSplash: InterstitialAd? = null
        private var mNativeAd: NativeAd? = null
        private var isInterstitialSplashLoading = false
        private var isInterstitialDefaultLoading = false

        private const val TAG = "AdmobAdsTAG"
    }

    fun loadBannerAd(activity: Activity, adContainer: ViewGroup) {
        val adView = AdView(context)
        adView.adUnitId = context.getString(R.string.banner_test_ads_id)
        adView.setAdSize(activity.getAdSizeAds())
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        adView.adListener = object : AdListener() {
            override fun onAdClosed() {
                super.onAdClosed()
                Log.d(TAG, "onAdClosed: ")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                Log.e(TAG, "onAdFailedToLoad: Banner Ads ${loadAdError.message}")
                adContainer.removeAllViews()
                adContainer.hideView()
            }

            override fun onAdOpened() {
                super.onAdOpened()
                Log.d(TAG, "onAdOpened: ")
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                Log.d(TAG, "onAdLoaded: ")
                adContainer.removeAllViews()
                adContainer.addView(adView)
                adContainer.showView()
            }

            override fun onAdClicked() {
                super.onAdClicked()
                Log.d(TAG, "onAdClicked: ")
            }

            override fun onAdImpression() {
                super.onAdImpression()
                Log.d(TAG, "onAdImpression: ")
            }
        }
    }

    private fun Activity.getAdSizeAds(): AdSize {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics: WindowMetrics = windowManager.currentWindowMetrics
            val bounds: Rect = windowMetrics.bounds
            val adWidthPixels = bounds.width()
            val density: Float = this.resources.displayMetrics.density
            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
        } else {
            val display = this.windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)
            val widthPixels = outMetrics.widthPixels.toFloat()
            val density = outMetrics.density
            val adWidth = (widthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
        }
    }

    fun loadInterstitialAd() {
        if (interstitialAdDefault == null && !isInterstitialDefaultLoading) {
            isInterstitialDefaultLoading = true
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context,
                context.getString(R.string.interstitial_test_ads_id),
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        super.onAdFailedToLoad(loadAdError)
                        Log.e(TAG, "onAdFailedToLoad: $loadAdError")
                        isInterstitialDefaultLoading = false
                    }

                    override fun onAdLoaded(mInterstitialAd: InterstitialAd) {
                        super.onAdLoaded(mInterstitialAd)
                        Log.d(TAG, "onAdLoaded: ")
                        interstitialAdDefault = mInterstitialAd
                    }
                }
            )
        }
    }

    fun showInterstitialAd(activity: Activity, onAdDismiss: (() -> Unit?)? = null) {
        if (interstitialAdSplash != null) {
            showInterstitialAdSplash(activity, onAdDismiss)
        } else {
            if (interstitialAdDefault != null) {
                interstitialAdDefault!!.show(activity)
                interstitialAdDefault!!.fullScreenContentCallback =
                    object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent()
                            isInterstitialDefaultLoading = false
                            interstitialAdDefault = null
                            loadInterstitialAd()
                            onAdDismiss?.invoke()
                            Log.d("admob_interstitial", "intersDefault dismiss ")
                        }

                        override fun onAdShowedFullScreenContent() {
                            super.onAdShowedFullScreenContent()
                            onAdDismiss?.invoke()
                        }
                    }

            } else {
                loadInterstitialAd()
                onAdDismiss?.invoke()
            }
        }
    }

    fun loadInterstitialAdSplash() {
        if (interstitialAdSplash == null && !isInterstitialSplashLoading) {
            isInterstitialSplashLoading = true
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context,
                context.getString(R.string.interstitial_test_ads_id),
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        super.onAdFailedToLoad(loadAdError)
                        Log.e(TAG, "onAdFailedToLoad: $loadAdError")
                        isInterstitialSplashLoading = false
                    }

                    override fun onAdLoaded(minterstitialAd: InterstitialAd) {
                        super.onAdLoaded(minterstitialAd)
                        Log.d(TAG, "onAdLoaded: ")
                        interstitialAdSplash = minterstitialAd
                    }
                }
            )
        }
    }

    fun showInterstitialAdSplash(activity: Activity, onAdDismiss: (() -> Unit?)? = null) {
        if (interstitialAdSplash != null) {
            interstitialAdSplash?.show(activity)
            interstitialAdSplash?.fullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent()
                        Log.d(TAG, "onAdDismissedFullScreenContent: ")
                        isInterstitialSplashLoading = false
                        interstitialAdSplash = null
                        onAdDismiss?.invoke()
                    }
                }
        } else {
            onAdDismiss?.invoke()
        }
    }

    fun loadNativeAd(onAdLoaded: (nativeAd: NativeAd?) -> Unit) {
        Log.d(TAG, "loadNativeAd: ")
        if (mNativeAd != null) {
            if (isViewActive)
                onAdLoaded(mNativeAd)

            return
        }

        val adLoader =
            AdLoader.Builder(context, context.getString(R.string.native_test_ads_id))
                .forNativeAd { nativeAd ->
                    mNativeAd = nativeAd

                    if (!isViewActive) return@forNativeAd
                    onAdLoaded(mNativeAd)
                }
                .withAdListener(object : AdListener() {
                    override fun onAdLoaded() {
                        super.onAdLoaded()
                        Log.d(TAG, "onAdLoaded: ")
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.e(TAG, "onAdFailedToLoad: $adError")
                        mNativeAd = null
                    }
                })
                .withNativeAdOptions(NativeAdOptions.Builder().build())
                .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        Log.d(TAG, "onStateChanged: $event")
        if (event == Lifecycle.Event.ON_DESTROY) {
            isViewActive = false
        }
    }
}
