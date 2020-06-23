package de.devmil.muzei.bingimageoftheday

import android.app.PendingIntent
import android.content.*
import android.os.*
import androidx.core.content.FileProvider
import com.google.android.apps.muzei.api.provider.ProviderContract
import de.devmil.common.utils.LogUtil
import de.devmil.common.utils.goAsync
import de.devmil.common.utils.toastFromBackground

class BingImageOfTheDayBroadcastReceiver : BroadcastReceiver() {
    companion object {
        private val TAG = BingImageOfTheDayBroadcastReceiver::class.java.simpleName

        const val INTENT_ACTION_SHARE = "${BuildConfig.APPLICATION_ID}.action.SHARE"
        const val INTENT_ACTION_OPEN = "${BuildConfig.APPLICATION_ID}.action.OPEN"
        const val INTENT_ACTION_UPDATE = "${BuildConfig.APPLICATION_ID}.action.UPDATE"

        fun createIntent(
                context: Context,
                action: String
        ): Intent {
            return Intent(context, BingImageOfTheDayBroadcastReceiver::class.java).apply {
                setAction(action)
            }
        }

        fun createPendingIntent(
                context: Context,
                action: String
        ): PendingIntent {
            val intent = createIntent(context, action)
            return PendingIntent.getBroadcast(context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        LogUtil.LOGD(TAG, "Received broadcast Intent with action=${intent.action}")
        goAsync {
            when (intent.action) {
                INTENT_ACTION_SHARE -> {
                    shareLast(context)
                }
                INTENT_ACTION_OPEN -> {
                    openLast(context)
                }
                INTENT_ACTION_UPDATE, Intent.ACTION_BOOT_COMPLETED -> {
                    BingImageOfTheDayArtProvider.doUpdate(context)
                }
            }
        }
    }

    private fun shareLast(context: Context) {
        val success = ProviderContract.getProviderClient(
                context, BuildConfig.BING_IMAGE_OF_THE_DAY_AUTHORITY).run {
            lastAddedArtwork?.let { artwork ->
                LogUtil.LOGD(TAG, "Sharing artwork with token=${artwork.token}")
                val cacheFilename = artwork.metadata
                try {
                    cacheFilename?.let { filename ->
                        context.cacheDir?.resolve("images")?.apply { mkdir() }?.apply { deleteOnExit() }?.resolve(filename)
                    }?.let { cacheFile ->
                        if (!cacheFile.exists()) {
                            if (artwork.data.exists()) {
                                artwork.data.copyTo(cacheFile)
                            } else {
                                // Attempt to rebuild artwork cache
                                val artworkUri = ContentUris.withAppendedId(contentUri, artwork.id)
                                context.contentResolver.openFileDescriptor(
                                        artworkUri, "r").run {
                                    ParcelFileDescriptor.AutoCloseInputStream(this).copyTo(cacheFile.outputStream())
                                }
                            }
                        }
                        val shareMessageBuilder = StringBuilder()
                        shareMessageBuilder.apply {
                            if (artwork.title.isNullOrEmpty()) {
                                append(artwork.byline)
                            } else {
                                append("${artwork.title} - ${artwork.byline}")
                            }
                            append(" #BingImageOfTheDay")
                            if (artwork.webUri != null) {
                                append("\n\n${artwork.webUri}")
                            }
                        }
                        Intent(Intent.ACTION_SEND).apply {
                            val shareUri = FileProvider.getUriForFile(context,
                                    BuildConfig.BING_IMAGE_OF_THE_DAY_FILE_PROVIDER_AUTHORITY, cacheFile)
                            putExtra(Intent.EXTRA_TEXT, shareMessageBuilder.toString())
                            putExtra(Intent.EXTRA_STREAM, shareUri)
                            clipData = ClipData(shareMessageBuilder.toString(), arrayOf("image/*"), ClipData.Item(shareUri))
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            type = "image/*"
                        }.let {
                            context.startActivity(Intent.createChooser(it, context.getString(R.string.command_share_title)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        }
                        true
                    } ?: false
                } catch (e: Exception) {
                    LogUtil.LOGE(TAG, "Error sharing file", e)
                    false
                }
            } ?: false
        }
        if (!success) {
            context.toastFromBackground(R.string.command_share_error)
        }
    }

    private fun openLast(context: Context) {
        ProviderContract.getProviderClient(
                context, BuildConfig.BING_IMAGE_OF_THE_DAY_AUTHORITY).run {
            lastAddedArtwork?.let { artwork ->
                LogUtil.LOGD(TAG, "Opening URL for artwork with token=${artwork.token}")
                context.startActivity(Intent(Intent.ACTION_VIEW, artwork.persistentUri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } ?: context.toastFromBackground(R.string.command_open_error)
        }
    }
}