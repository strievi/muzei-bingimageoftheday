package de.devmil.muzei.bingimageoftheday

import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.ProviderContract
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import de.devmil.common.utils.LogUtil
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class BingImageOfTheDayBroadcastReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "BingImageOfTheDayBroadcastReceiver"

        const val INTENT_ACTION_SHARE = "de.devmil.muzei.bingimageoftheday.action.SHARE"
        const val INTENT_ACTION_OPEN = "de.devmil.muzei.bingimageoftheday.action.OPEN"

        fun createPendingIntent(
                context: Context,
                action: String
        ): PendingIntent {
            val intent = Intent(context, BingImageOfTheDayBroadcastReceiver::class.java).apply {
                setAction(action)
            }
            return PendingIntent.getBroadcast(context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val providerClient = ProviderContract.getProviderClient(
                context, BuildConfig.BING_IMAGE_OF_THE_DAY_AUTHORITY)
        providerClient.lastAddedArtwork?.let {
            when (intent.action) {
                INTENT_ACTION_SHARE -> {
                    shareCurrentImage(context, it)
                }
                INTENT_ACTION_OPEN -> {
                    openCurrentImage(context, it)
                }
            }
        }
    }

    @TargetApi(26)
    fun getLocalBitmapUri(bmp: Bitmap?, context: Context?): Uri? {
        var bmpUri: Uri? = null
        bmp?.let { bitmap ->
            try {
                context?.let { ctx ->
                    val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-kkmmss"))
                    val imagePath = File(context.cacheDir, "images")
                    imagePath.mkdirs()
                    val outputFile = File(imagePath, "output-$timestamp.png")
                    val out = FileOutputStream(outputFile)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
                    out.close()
                    bmpUri = FileProvider.getUriForFile(context, "de.devmil.muzei.bingimageoftheday.ImageFileProvider", outputFile)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(context, "Error downloading the image to share", Toast.LENGTH_LONG).show()
            }
        }
        return bmpUri
    }

    private fun shareCurrentImage(context: Context, artwork: Artwork?) {
        LogUtil.LOGD(TAG, "got share request")
        artwork?.let {
            var shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            val shareMessage = context?.getString(R.string.command_share_message, it.byline)
            shareIntent.putExtra(Intent.EXTRA_TEXT, "$shareMessage - ${it.persistentUri.toString()}")
            shareIntent.type = "text/plain"
            //shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            LogUtil.LOGD(TAG, "Sharing ${it.persistentUri}")

            //For API > 26: download image and attach that
            if(Build.VERSION.SDK_INT >= 26) {
                val uiHandler = Handler(Looper.getMainLooper())
                uiHandler.post {
                    Picasso.with(context).load(it.persistentUri).into(object : Target {
                        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                            LogUtil.LOGD(TAG, "Downloading ${it.persistentUri}")
                        }

                        override fun onBitmapFailed(errorDrawable: Drawable?) {
                            Toast.makeText(context, "Error downloading the image to share", Toast.LENGTH_LONG).show()
                        }

                        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                            shareIntent.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(bitmap, context))
                            shareIntent.type = "image/png"

                            executeIntentSharing(context, shareIntent)
                        }
                    })
                }
            } else { // SDK < 26 => directly share (the URL)
                executeIntentSharing(context, shareIntent)
            }
        }
    }

    private fun executeIntentSharing(context: Context, intent: Intent) {
        var shareIntent = Intent.createChooser(intent, context?.getString(R.string.command_share_title) ?: "")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        context?.startActivity(shareIntent)
    }

    private fun openCurrentImage(context: Context, artwork: Artwork?) {
        LogUtil.LOGD(TAG, "got open request")
        artwork?.let {
            var openIntent = Intent(Intent.ACTION_VIEW)

            LogUtil.LOGD(TAG, "Opening ${it.persistentUri}")

            openIntent.data = it.persistentUri
            openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            context?.startActivity(openIntent)
        }
    }
}