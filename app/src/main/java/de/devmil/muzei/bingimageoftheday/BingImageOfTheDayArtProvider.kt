package de.devmil.muzei.bingimageoftheday

import android.content.Context
import androidx.core.app.RemoteActionCompat
import androidx.core.graphics.drawable.IconCompat
import com.google.android.apps.muzei.api.UserCommand
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.MuzeiArtProvider
import de.devmil.common.utils.LogUtil
import de.devmil.muzei.bingimageoftheday.events.RequestMarketSettingChangedEvent
import de.devmil.muzei.bingimageoftheday.events.RequestPortraitSettingChangedEvent
import de.devmil.muzei.bingimageoftheday.worker.BingImageOfTheDayWorker
import de.greenrobot.event.EventBus
import java.io.InputStream


class BingImageOfTheDayArtProvider : MuzeiArtProvider() {

    /**
     * This class is used to get EventBus events
     */
    class EventCatcher {
        init {
            EventBus.getDefault().register(this)
        }

        fun onEventBackgroundThread(e: RequestPortraitSettingChangedEvent) {
            requestUpdate(e.context)
        }

        fun onEventBackgroundThread(e: RequestMarketSettingChangedEvent) {
            requestUpdate(e.context)
        }

        private fun requestUpdate(context: Context) {
            doUpdate()
        }

    }

    companion object {
        private const val TAG = "BingImageOfTheDayArtProvider"

        private const val COMMAND_ID_SHARE = 2
        private const val COMMAND_ID_OPEN = 3

        private var CatcherInstance: BingImageOfTheDayArtProvider.EventCatcher? = null

        init {
            //instantiate the EventCatcher when BingImageOfTheDayArtSource is loaded
            CatcherInstance = BingImageOfTheDayArtProvider.EventCatcher()
        }

        private var _isActive: Boolean? = null
        var isActive: Boolean?
            get() = _isActive
            private set(value) {
                _isActive = value
            }

        fun doUpdate() {
            BingImageOfTheDayWorker.enqueueLoad()
        }
    }

    override fun onLoadRequested(initial: Boolean) {
        isActive = true
        BingImageOfTheDayWorker.enqueueLoad()
    }

    override fun openFile(artwork: Artwork): InputStream {
        LogUtil.LOGD(TAG, "Loading artwork: ${artwork.title} (${artwork.persistentUri})")
        return super.openFile(artwork);
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