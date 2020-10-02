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

    val bingMarket: BingMarket
        get() {
            val autoMarketCode = preferences.getBoolean(PREF_AUTO_MARKET, true)
            var marketCode: String? = null

            if (autoMarketCode) {
                val currentLocale = context.resources.configuration.locale
                if (currentLocale != null) {
                    val isoCode = currentLocale.toString().replace("_", "-")
                    val market = BingMarket.fromMarketCode(isoCode)
                    if (market !== BingMarket.Unknown) {
                        marketCode = market.marketCode
                    }
                }
            } else {
                marketCode = preferences.getString(PREF_MARKET_CODE, null)
            }
            if (marketCode == null) {
                marketCode = DEFAULT_MARKET.marketCode
            }
            return BingMarket.fromMarketCode(marketCode)
        }

    var marketCode: String?
        get() = preferences.getString(PREF_MARKET_CODE, null)
        set(marketCode) {
            preferences.edit().putString(PREF_MARKET_CODE, marketCode).apply()
        }

    var isOrientationPortrait: Boolean
        get() = preferences.getBoolean(PREF_ORIENTATION_PORTRAIT, isPortraitDefault(context))
        set(isOrientationPortrait) {
            preferences.edit().putBoolean(PREF_ORIENTATION_PORTRAIT, isOrientationPortrait).apply()
        }

    var isAutoMarket: Boolean
        get() = preferences.getBoolean(PREF_AUTO_MARKET, true)
        set(isAutoMarket) {
            preferences.edit().putBoolean(PREF_AUTO_MARKET, isAutoMarket).apply()
        }

    companion object {
        private const val PREFS_NAME = "BingImageOfTheDay"

        private const val PREF_AUTO_MARKET = "art_source_settings_auto_market"
        private const val PREF_MARKET_CODE = "art_source_settings_market_code"
        private const val PREF_ORIENTATION_PORTRAIT = "art_source_settings_orientation_portrait"

        private val DEFAULT_MARKET = BingMarket.EN_US

        private fun isPortraitDefault(context: Context): Boolean {
            val xlarge = context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK == Configuration.SCREENLAYOUT_SIZE_XLARGE
            val large = context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK == Configuration.SCREENLAYOUT_SIZE_LARGE
            return !(xlarge or large)
        }
    }
}
