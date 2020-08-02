package de.devmil.muzei.bingimageoftheday

import android.content.*

class BingImageOfTheDayUpdateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        BingImageOfTheDayArtProvider.doUpdate(context)
    }
}