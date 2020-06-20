package de.devmil.muzei.bingimageoftheday

import androidx.multidex.MultiDexApplication

class BingImageOfTheDayApplication : MultiDexApplication() {
    companion object {
        lateinit var instance: BingImageOfTheDayApplication private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}