package de.devmil.muzei.bingimageoftheday

import android.content.Context
import androidx.core.app.RemoteActionCompat
import androidx.core.graphics.drawable.IconCompat
import com.google.android.apps.muzei.api.MuzeiContract
import com.google.android.apps.muzei.api.UserCommand
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.MuzeiArtProvider
import de.devmil.common.utils.LogUtil
import de.devmil.muzei.bingimageoftheday.BuildConfig.BING_IMAGE_OF_THE_DAY_AUTHORITY
import de.devmil.muzei.bingimageoftheday.worker.BingImageOfTheDayWorker
import java.io.InputStream


class BingImageOfTheDayArtProvider : MuzeiArtProvider() {

    companion object {
        private val TAG = BingImageOfTheDayArtProvider::class.java.simpleName

        private const val COMMAND_ID_SHARE = 2
        private const val COMMAND_ID_OPEN = 3

        fun doUpdate(context: Context) {
            LogUtil.LOGD(TAG, "Received update request")
            if (MuzeiContract.Sources.isProviderSelected(context, BING_IMAGE_OF_THE_DAY_AUTHORITY)) {
                BingImageOfTheDayWorker.enqueueLoad(context)
            }
        }
    }

    override fun onLoadRequested(initial: Boolean) {
        LogUtil.LOGD(TAG, "Received load request")
        val context = context ?: return
        BingImageOfTheDayWorker.enqueueLoad(context)
    }

    override fun openFile(artwork: Artwork): InputStream {
        LogUtil.LOGD(TAG, "Opening file for artwork with token=${artwork.token}")
        return super.openFile(artwork)
    }

    private fun createShareAction(context: Context, artwork: Artwork): RemoteActionCompat {
        return RemoteActionCompat(
                IconCompat.createWithResource(context, R.drawable.ic_share),
                context.getString(R.string.command_share_title),
                "",
                BingImageOfTheDayBroadcastReceiver.createPendingIntent(
                        context, BingImageOfTheDayBroadcastReceiver.INTENT_ACTION_SHARE)
        ).apply {
            setShouldShowIcon(true)
        }
    }

    private fun createOpenAction(context: Context, artwork: Artwork): RemoteActionCompat {
        return RemoteActionCompat(
                IconCompat.createWithResource(context, R.drawable.ic_open),
                context.getString(R.string.command_open_title),
                "",
                BingImageOfTheDayBroadcastReceiver.createPendingIntent(
                        context, BingImageOfTheDayBroadcastReceiver.INTENT_ACTION_OPEN)
        ).apply {
            setShouldShowIcon(true)
        }
    }

    override fun getCommandActions(artwork: Artwork): List<RemoteActionCompat> {
        val context = context ?: return super.getCommandActions(artwork)
        return listOfNotNull(
                createShareAction(context, artwork),
                createOpenAction(context, artwork))
    }

    override fun getCommands(artwork: Artwork): List<UserCommand>{
        val context = context ?: return super.getCommands(artwork)
        return listOfNotNull(
                UserCommand(COMMAND_ID_SHARE, context.getString(R.string.command_share_title)),
                UserCommand(COMMAND_ID_OPEN, context.getString(R.string.command_open_title)))
    }

    override fun onCommand(artwork: Artwork, id: Int)  {
        val context = context ?: return super.onCommand(artwork, id)
        when (id) {
            COMMAND_ID_SHARE -> createShareAction(context, artwork).actionIntent.send()
            COMMAND_ID_OPEN -> createOpenAction(context, artwork).actionIntent.send()
        }
    }
}