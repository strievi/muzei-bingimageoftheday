package de.devmil.muzei.bingimageoftheday.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.webkit.URLUtil
import androidx.work.*
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.ProviderContract
import de.devmil.common.utils.LogUtil
import de.devmil.muzei.bingimageoftheday.*
import de.devmil.muzei.bingimageoftheday.BuildConfig.BING_IMAGE_OF_THE_DAY_AUTHORITY
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class BingImageOfTheDayWorker(
        val context: Context,
        workerParams: WorkerParameters
) : Worker(context, workerParams) {
    companion object {
        private const val TAG = "BingImageOfTheDayWorker"

        internal fun enqueueLoad() {
            Log.d(TAG, "Loading enqued")
            val workManager = WorkManager.getInstance()
            workManager.enqueue(OneTimeWorkRequestBuilder<BingImageOfTheDayWorker>()
                    .setConstraints(Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build())
                    .build())
        }

        private val lockObject = Object()
        private var lastArtworkUpdate: Calendar? = null
    }

    override fun doWork(): Result {
        LogUtil.LOGD(TAG, "Request: Loading new Bing images")

        synchronized(lockObject) {

            val now = Calendar.getInstance()

            val settings = Settings(applicationContext)
            val isPortrait = settings.isOrientationPortrait
            val isCurrentArtworkPortrait = settings.isCurrentOrientationPortrait

            val market = settings.bingMarket
            val currentArtworkMarket = settings.currentBingMarket

            //when there are settings changes then the request for new images
            //gets overwritten
            val requestOverride = market != currentArtworkMarket
                    || isPortrait != isCurrentArtworkPortrait

            //Check if the last update is more than 2 minutes away.
            //If not then return early (exception: when settings changed that lead to an update)
            lastArtworkUpdate?.let {
                val millisDiff = now.timeInMillis - it.timeInMillis
                val minDiff = 1000 /* seconds */ * 60 /* minutes */ * 2
                if(!requestOverride && millisDiff < minDiff) {
                    LogUtil.LOGD(TAG, "Last update was less than 2 minutes ago => ignoring")
                    return Result.SUCCESS
                }
            }
            lastArtworkUpdate = now

            //Default = request the image list from Bing
            var requestNewImages = true
            val providerClient = ProviderContract.getProviderClient(
                    applicationContext, BING_IMAGE_OF_THE_DAY_AUTHORITY)
            val lastArtwork = providerClient.lastAddedArtwork
            if (lastArtwork != null) {
                LogUtil.LOGD(TAG, "Found last artwork")
                val timeInMillis = lastArtwork.metadata?.toLongOrNull()
                if (timeInMillis != null) {
                    val token = getToken(Date(timeInMillis), market, isPortrait)
                    LogUtil.LOGD(TAG, "Metadata is correct")
                    if (token == lastArtwork.token && isNewestBingImage(Date(timeInMillis))) {
                        //when the current artwork matches the settings and is the newest, then don't load that Bing list
                        LogUtil.LOGD(TAG, "We have the latest image => do nothing")
                        requestNewImages = false
                        requestNextImageUpdate(Date(timeInMillis))
                    }
                }
            }

            if (requestOverride) {
                LogUtil.LOGD(TAG, "Settings changed! reloading anyways!")
                requestNewImages = true
            }
            if (!requestNewImages) {
                return Result.SUCCESS
            }

            LogUtil.LOGD(TAG, "Reloading Bing images")

            val retriever = BingImageOfTheDayMetadataRetriever(
                    market,
                    BingImageDimension.HD,
                    isPortrait
            )

            val photosMetadata = try {
                retriever.bingImageOfTheDayMetadata ?: listOf()
            } catch (e: IOException) {
                Log.w(TAG, "Error reading Bing response", e)
                return Result.RETRY
            }

            if (photosMetadata.isEmpty()) {
                Log.w(TAG, "No photos returned from Bing API.")
                return Result.FAILURE
            }

            photosMetadata.asSequence().map { metadata ->
                Artwork(
                    token = getToken(metadata.startDate, market, isPortrait),
                    attribution = "bing.com",
                    title = metadata.title?: "",
                    byline = metadata.copyright ?: "",
                    persistentUri = metadata.uri,
                    webUri = if (URLUtil.isNetworkUrl(metadata.copyrightLink)) Uri.parse(metadata.copyrightLink) else null,
                    metadata = metadata.startDate?.time.toString())
            }.sortedByDescending { aw ->
                aw.metadata?.toLongOrNull() ?: 0
            }.firstOrNull()
            ?.let { artwork ->
                Log.d(TAG, "Got artworks. Selected this one: ${artwork.title} valid on: ${Date(artwork.metadata!!.toLong())}")
                requestNextImageUpdate(Date(artwork.metadata!!.toLong()))
                providerClient.setArtwork(artwork);
                settings.isCurrentOrientationPortrait = isPortrait
                settings.currentBingMarket = market
            }
            return Result.SUCCESS
        }
    }

    private fun getToken(startDate: Date?, market: BingMarket, isPortrait: Boolean): String {
        val result = "$startDate-$market-${if(isPortrait) "portrait" else "landscape"}"
        LogUtil.LOGD(TAG, "Token: $result")
        return result
    }

    private fun isNewestBingImage(newestBingImageDate: Date) : Boolean {
        val now = Calendar.getInstance().time
        val nextBingImageDate = getNextBingImageDate(newestBingImageDate)

        return now < nextBingImageDate
    }

    private fun getNextBingImageDate(newestBingImageDate: Date): Date {
        val nextBingImageDate = Calendar.getInstance()
        nextBingImageDate.timeInMillis = newestBingImageDate.time
        nextBingImageDate.add(Calendar.DAY_OF_YEAR, 1)
        return nextBingImageDate.time
    }

    private fun requestNextImageUpdate(currentImageDate: Date): Calendar {
        val nextBingImageDate = getNextBingImageDate(currentImageDate)
        val nextUpdate = Calendar.getInstance()
        nextUpdate.time = nextBingImageDate
        nextUpdate.add(Calendar.MINUTE, 1)

        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.US)

        LogUtil.LOGD(TAG, String.format("next update: %s", sdf.format(nextUpdate.time)))

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

        val updateIntent = Intent(context,  UpdateReceiver::class.java)
        val pendingUpdateIntent = PendingIntent.getBroadcast(context, 1, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        alarmManager?.set(AlarmManager.RTC_WAKEUP, nextUpdate.timeInMillis, pendingUpdateIntent)

        return nextUpdate
    }
}
