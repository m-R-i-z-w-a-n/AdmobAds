import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView


class AdmobAds(private val context: Context) {
    lateinit var adView: AdView

    companion object {
        var interstitialAdDefault: InterstitialAd? = null
        var interstitialAdSplash: InterstitialAd? = null
        var mNativeAd: NativeAd? = null
        private var isInterstitialSplashLoading = false
        private var isInterstitialDefaultLoading = false
    }

    private val mTag = "banner"

    fun loadBannerAdmob(frameLayout: FrameLayout, adsLayout: RelativeLayout) {

            adView = AdView(context)
            adView.adUnitId = "ca-app-pub-3940256099942544/9214589741"
            frameLayout.addView(adView)
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)

            adView.adListener = object : AdListener() {
                override fun onAdClosed() {
                    super.onAdClosed()
                    Log.e(mTag, "onAdClosed: ")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    Log.d(mTag, "onAdFailedToLoad: Banner Ads ${loadAdError.message}")
                }

                override fun onAdOpened() {
                    super.onAdOpened()
                    Log.e(mTag, "onAdOpened: ")
                }

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    Log.e(mTag, "onAdLoaded: ")
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    Log.e(mTag, "onAdClicked: ")
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                    Log.e(mTag, "onAdImpression: ")
                }
            }
    }

    private fun getAdSize(activity: Activity): AdSize {
        val display: Display = activity.windowManager.defaultDisplay
        val displayMetrics = DisplayMetrics()
        display.getMetrics(displayMetrics)
        val density = displayMetrics.density
        val adWidthPixels = displayMetrics.widthPixels
        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
    }

    fun loadInterstitialAdmob() {
        if (interstitialAdDefault == null && !isInterstitialDefaultLoading) {
            isInterstitialDefaultLoading = true
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context,
                context.getString(R.string.interstitial_id_home),
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        super.onAdFailedToLoad(loadAdError)
                        Log.d("admob_interstitial", "failed default")
                        isInterstitialDefaultLoading = false
                    }

                    override fun onAdLoaded(minterstitialAd: InterstitialAd) {
                        super.onAdLoaded(minterstitialAd)
                        interstitialAdDefault = minterstitialAd
                        Log.d("admob_interstitial", "adLoaded default")
                    }
                }
            )
        }
    }

    fun showInterstitialAdmob(activity: Activity, onCallback: FullScreenContentCallback) {
        if (interstitialAdSplash != null) {
            showInterstitialSplash(activity, getBackPointer)
        } else {
            if (interstitialAdDefault != null) {
                interstitialAdDefault!!.show(activity)
                interstitialAdDefault!!.fullScreenContentCallback =
                    object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent()
                            onCallback.onAdDismissedFullScreenContent()
                            isInterstitialDefaultLoading = false
                            interstitialAdDefault = null
                            loadInterstitialAdmob()
                            Log.d("admob_interstitial", "intersDefault dismiss ")
                        }

                        override fun onAdShowedFullScreenContent() {
                            super.onAdShowedFullScreenContent()
                            onCallback.onAdShowedFullScreenContent()
                        }
                    }

            } else {
                loadInterstitialAdmob()
                getBackPointer?.returnAction()
            }
        }
    }

    fun loadInterstitialSplash() {
        if (interstitialAdSplash == null && !isInterstitialSplashLoading) {
            isInterstitialSplashLoading = true
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context,
                context.getString(R.string.interstitial_id_home),
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        super.onAdFailedToLoad(loadAdError)
                        Log.d("admob_interstitial", "failed splash")
                        isInterstitialSplashLoading = false
                    }

                    override fun onAdLoaded(minterstitialAd: InterstitialAd) {
                        super.onAdLoaded(minterstitialAd)
                        interstitialAdSplash = minterstitialAd
                        Log.d("admob_interstitial", "adLoaded splash")
                    }
                }
            )
        }
    }

    fun showInterstitialSplash(activity: Activity, getBackPointer: GetBackPointer?) {
        if (interstitialAdSplash != null) {
            interstitialAdSplash!!.show(activity)
            interstitialAdSplash!!.fullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent()
                        isInterstitialSplashLoading = false
                        interstitialAdSplash = null
                        getBackPointer?.returnAction()
                        Log.d("admob_interstitial", "intersSplash dismiss ")
                    }
                }
        } else {
            getBackPointer?.returnAction()
        }
    }

    fun loadNativeAdAdmob(
        activity: Activity,
        frameLayout: FrameLayout,
        image: ImageView,
        nativeId: String
    ) {
        val builder = AdLoader.Builder(context, nativeId)
        builder.forNativeAd { nativeAd ->
            val adView =
                activity.layoutInflater.inflate(R.layout.native_medium_layout, null) as NativeAdView
            populateUnifiedNativeAdView(nativeAd, adView)
            frameLayout.removeAllViews()
            frameLayout.addView(adView)
        }

        val videoOptions = VideoOptions.Builder().build()
        val adOptions = NativeAdOptions.Builder().setVideoOptions(videoOptions)
            .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT).build()
        builder.withNativeAdOptions(adOptions)
        val adLoader = builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                image.visibility = View.VISIBLE
            }

            override fun onAdLoaded() {
                frameLayout.visibility = View.VISIBLE
                image.visibility = View.INVISIBLE
                super.onAdLoaded()
            }
        }).build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun populateUnifiedNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
        adView.mediaView = adView.findViewById(R.id.ad_media)
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.starRatingView = adView.findViewById(R.id.ad_stars)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

        (adView.headlineView as TextView).text = nativeAd.headline
        adView.mediaView.setMediaContent(nativeAd.mediaContent)

        if (nativeAd.body == null) {
            adView.bodyView.visibility = View.INVISIBLE
        } else {
            adView.bodyView.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            adView.callToActionView.visibility = View.INVISIBLE
        } else {
            adView.callToActionView.visibility = View.VISIBLE
            (adView.callToActionView as Button).text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            adView.iconView.visibility = View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(nativeAd.icon.drawable)
            adView.iconView.visibility = View.VISIBLE
        }

        if (nativeAd.starRating == null) {
            adView.starRatingView.visibility = View.INVISIBLE
        } else {
            (adView.starRatingView as RatingBar).rating = nativeAd.starRating!!.toFloat()
            adView.starRatingView.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser == null) {
            adView.advertiserView.visibility = View.INVISIBLE
        } else {
            (adView.advertiserView as TextView).text = nativeAd.advertiser
            adView.advertiserView.visibility = View.VISIBLE
        }

        adView.setNativeAd(nativeAd)
    }

    interface GetBackPointer {
        fun returnAction()
    }
}
