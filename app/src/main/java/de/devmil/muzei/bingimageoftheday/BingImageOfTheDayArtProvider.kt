package de.devmil.muzei.bingimageoftheday

import android.app.PendingIntent
import android.content.*
import android.net.Uri
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

    private fun createShareIntent(context: Context, artwork: Artwork): Intent {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            val contentProviderUri = Uri.Builder()
                    .scheme(ContentResolver.SCHEME_CONTENT)
                    .authority(BuildConfig.BING_IMAGE_OF_THE_DAY_CONTENT_PROVIDER_AUTHORITY)
                    .build()
            val shareUri = ContentUris.withAppendedId(contentProviderUri, artwork.id)
            val shareMessageBuilder = StringBuilder()
            shareMessageBuilder.apply {
                if (artwork.title.isNullOrEmpty()) {
                    append(artwork.byline)
                } else {
                    append("${artwork.title} - ${artwork.byline}")
                }
                append(" ${context.getString(R.string.share_message_hashtag)}")
                if (artwork.webUri != null) {
                    append("\n\n${artwork.webUri}")
                }
            }
            putExtra(Intent.EXTRA_TEXT, shareMessageBuilder.toString())
            putExtra(Intent.EXTRA_STREAM, shareUri)
            clipData = ClipData.newUri(context.contentResolver,
                    context.getString(R.string.command_share_title), shareUri)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            type = context.contentResolver.getType(shareUri)
        }
        return Intent.createChooser(shareIntent,
                context.getString(R.string.command_share_title))
    }

    private fun createOpenIntent(context: Context, artwork: Artwork): Intent {
        return Intent(Intent.ACTION_VIEW, artwork.persistentUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    override fun getCommandActions(artwork: Artwork): List<RemoteActionCompat> {
        val context = context ?: return super.getCommandActions(artwork)
        return listOfNotNull(
                RemoteActionCompat(
                        IconCompat.createWithResource(context, R.drawable.ic_share),
                        context.getString(R.string.command_share_title),
                        "",
                        PendingIntent.getActivity(context, artwork.id.toInt(),
                                createShareIntent(context, artwork),
                                PendingIntent.FLAG_UPDATE_CURRENT)).apply {
                    setShouldShowIcon(true)
                },
                RemoteActionCompat(
                        IconCompat.createWithResource(context, R.drawable.ic_open),
                        context.getString(R.string.command_open_title),
                        "",
                        PendingIntent.getActivity(context, artwork.id.toInt(),
                                createOpenIntent(context, artwork),
                                PendingIntent.FLAG_UPDATE_CURRENT)).apply {
                    setShouldShowIcon(true)
                }
        )
    }

    override fun getCommands(artwork: Artwork): List<UserCommand> {
        val context = context ?: return super.getCommands(artwork)
        return listOfNotNull(
                UserCommand(COMMAND_ID_SHARE, context.getString(R.string.command_share_title)),
                UserCommand(COMMAND_ID_OPEN, context.getString(R.string.command_open_title)))
    }

    override fun onCommand(artwork: Artwork, id: Int) {
        val context = context ?: return super.onCommand(artwork, id)
        when (id) {
            COMMAND_ID_SHARE -> context.startActivity(createShareIntent(context, artwork))
            COMMAND_ID_OPEN -> context.startActivity(createOpenIntent(context, artwork))
        }
    }
}