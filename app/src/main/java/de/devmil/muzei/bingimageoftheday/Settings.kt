package de.devmil.muzei.bingimageoftheday

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration

/**
* Created by michaellamers on 05.05.15.
*/
class Settings(private val context: Context) {

    @Suppress("DEPRECATION")
    private val preferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE)

    // Try find best match based on local
    // no best match? Default!
    var bingMarket: BingMarket
        get() {
            var marketCode = preferences.getString(PREF_MARKET_CODE, null)

            if (marketCode == null) {
                val currentLocale = context.resources.configuration.locale
                if (currentLocale != null) {
                    val isoCode = currentLocale.toString().replace("_", "-")
                    val market = BingMarket.fromMarketCode(isoCode)
                    if (market !== BingMarket.Unknown) {
                        marketCode = market.marketCode
                    }
                }
                if (marketCode == null) {
                    return DEFAULT_MARKET
                }
            }
            return BingMarket.fromMarketCode(marketCode)
        }
        set(bingMarket) {
            preferences.edit().putString(PREF_MARKET_CODE, bingMarket.marketCode).apply()
        }

    var isOrientationPortrait: Boolean
        get() = preferences.getBoolean(PREF_ORIENTATION_PORTRAIT, isPortraitDefault(context))
        set(isOrientationPortrait) {
            preferences.edit().putBoolean(PREF_ORIENTATION_PORTRAIT, isOrientationPortrait).apply()
        }

    companion object {
        private const val PREFS_NAME = "BingImageOfTheDay"

        private const val PREF_MARKET_CODE = "art_source_settings_market_code"
        private const val PREF_ORIENTATION_PORTRAIT = "art_source_settings_orientation_portrait"

        val DEFAULT_MARKET = BingMarket.EN_US

        private fun isPortraitDefault(context: Context): Boolean {
            val xlarge = context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK == Configuration.SCREENLAYOUT_SIZE_XLARGE
            val large = context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK == Configuration.SCREENLAYOUT_SIZE_LARGE
            return !(xlarge or large)
        }
    }
}
