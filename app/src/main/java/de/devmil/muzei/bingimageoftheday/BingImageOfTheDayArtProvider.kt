package de.devmil.muzei.bingimageoftheday

import android.app.PendingIntent
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.net.Uri
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.core.app.RemoteActionCompat
import androidx.core.graphics.drawable.IconCompat
import com.google.android.apps.muzei.api.MuzeiContract
import com.google.android.apps.muzei.api.UserCommand
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.MuzeiArtProvider
import de.devmil.common.utils.LogUtil
import de.devmil.muzei.bingimageoftheday.BuildConfig.BING_IMAGE_OF_THE_DAY_AUTHORITY
import de.devmil.muzei.bingimageoftheday.worker.BingImageOfTheDayWorker
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import kotlin.math.roundToInt

class BingImageOfTheDayArtProvider : MuzeiArtProvider() {

    companion object {
        private val TAG = BingImageOfTheDayArtProvider::class.java.simpleName

        private const val COMMAND_ID_SHARE = 2

        private var displayAspect: Double = 0.0

        fun doUpdate(context: Context) {
            LogUtil.LOGD(TAG, "Received update request")
            if (MuzeiContract.Sources.isProviderSelected(context, BING_IMAGE_OF_THE_DAY_AUTHORITY)) {
                BingImageOfTheDayWorker.enqueueLoad(context)
            }
        }
    }

    override fun onCreate(): Boolean {
        // Compute device display aspect ratio
        val windowManager = context!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val displayMetrics = DisplayMetrics()
        display.getRealMetrics(displayMetrics)
        displayAspect = displayMetrics.widthPixels.toDouble() / displayMetrics.heightPixels
        if (displayAspect < 1) displayAspect = 1 / displayAspect
        LogUtil.LOGD(TAG, "Initializing with device display aspect ratio=$displayAspect")
        return super.onCreate()
    }

    override fun onLoadRequested(initial: Boolean) {
        LogUtil.LOGD(TAG, "Received load request")
        val context = context ?: return
        BingImageOfTheDayWorker.enqueueLoad(context)
    }

    override fun openFile(artwork: Artwork): InputStream {
        LogUtil.LOGD(TAG, "Opening file for artwork with token=${artwork.token}")
        val inputStream = super.openFile(artwork)
        return (if (artwork.metadata.toString().endsWith("_Cropped.jpg")) {
            // Crop to match device display aspect ratio
            var bitmap = BitmapFactory.decodeStream(inputStream)
            var width = bitmap.width
            var height = bitmap.height
            var bitmapAspect = width.toDouble() / height
            val isPortrait = bitmapAspect < 1
            if (isPortrait) bitmapAspect = 1 / bitmapAspect
            if (displayAspect > bitmapAspect) {
                if (isPortrait) {
                    width = (height / displayAspect).roundToInt()
                } else {
                    height = (width / displayAspect).roundToInt()
                }
            } else {
                if (isPortrait) {
                    height = (width * displayAspect).roundToInt()
                } else {
                    width = (height * displayAspect).roundToInt()
                }
            }
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height)
            val outputStream = ByteArrayOutputStream()
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)) {
                throw IOException("Cannot compress bitmap")
            }
            LogUtil.LOGD(TAG, "Cropped bitmap to ${width}x${height}" +
                    " for artwork with token=${artwork.token}")
            ByteArrayInputStream(outputStream.toByteArray())
        } else {
            inputStream
        })
    }

    private fun createShareIntent(context: Context, artwork: Artwork): Intent {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            val contentProviderUri = Uri.Builder()
                    .scheme(ContentResolver.SCHEME_CONTENT)
                    .authority(BuildConfig.BING_IMAGE_OF_THE_DAY_CONTENT_PROVIDER_AUTHORITY)
                    .build()
            val uri = ContentUris.withAppendedId(contentProviderUri, artwork.id)
            val items = mutableListOf<String>().apply {
                artwork.title?.let { if (it.isNotBlank()) add(it) }
                artwork.byline?.let { if (it.isNotBlank()) add(it) }
                when (artwork.webUri?.scheme) {
                    "http", "https" -> add(artwork.webUri.toString())
                }
                when (artwork.persistentUri?.scheme) {
                    "http", "https" -> add(artwork.persistentUri.toString())
                }
            }
            putExtra(Intent.EXTRA_TEXT, items.joinToString(separator = "\n\n"))
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.command_share_subject))
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newUri(context.contentResolver,
                    context.getString(R.string.command_share_title), uri)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            type = context.contentResolver.getType(uri)
        }
        return Intent.createChooser(shareIntent,
                context.getString(R.string.command_share_title))
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
                }
        )
    }

    override fun getCommands(artwork: Artwork): List<UserCommand> {
        val context = context ?: return super.getCommands(artwork)
        return listOfNotNull(
                UserCommand(COMMAND_ID_SHARE, context.getString(R.string.command_share_title)))
    }

    override fun onCommand(artwork: Artwork, id: Int) {
        val context = context ?: return super.onCommand(artwork, id)
        when (id) {
            COMMAND_ID_SHARE -> context.startActivity(createShareIntent(context, artwork))
        }
    }
}