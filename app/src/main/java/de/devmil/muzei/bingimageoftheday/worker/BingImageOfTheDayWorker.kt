package de.devmil.muzei.bingimageoftheday.worker

import android.content.Context
import android.net.Uri
import android.webkit.URLUtil
import androidx.work.*
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.ProviderContract
import de.devmil.common.utils.LogUtil
import de.devmil.muzei.bingimageoftheday.*
import de.devmil.muzei.bingimageoftheday.BuildConfig.BING_IMAGE_OF_THE_DAY_AUTHORITY

class BingImageOfTheDayWorker(
        val context: Context,
        workerParams: WorkerParameters
) : Worker(context, workerParams) {
    companion object {
        private val TAG = BingImageOfTheDayWorker::class.java.simpleName

        internal fun enqueueLoad() {
            LogUtil.LOGD(TAG, "Received enqueue request")
            val workManager = WorkManager.getInstance()
            workManager.enqueue(OneTimeWorkRequestBuilder<BingImageOfTheDayWorker>()
                    .setConstraints(Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build())
                    .build())
        }

        private val lockObject = Object()
    }

    override fun doWork(): Result {
        LogUtil.LOGD(TAG, "Starting background work")
        synchronized(lockObject) {
            val settings = Settings(applicationContext)
            val isPortrait = settings.isOrientationPortrait
            val market = settings.bingMarket
            val retriever = BingImageOfTheDayMetadataRetriever(
                    market,
                    BingImageDimension.HD,
                    isPortrait
            )
            val imagesMetadata = try {
                retriever.bingImageOfTheDayMetadata ?: listOf()
            } catch (e: Exception) {
                LogUtil.LOGE(TAG, "Error querying Bing API", e)
                return Result.RETRY
            }
            if (imagesMetadata.isEmpty()) {
                LogUtil.LOGW(TAG, "Bing API returned no result")
                return Result.FAILURE
            }
            imagesMetadata.maxBy { it.fullStartDate!! }?.let { latestMetadata ->
                ProviderContract.getProviderClient(
                        applicationContext, BING_IMAGE_OF_THE_DAY_AUTHORITY).run {
                    Artwork(
                            token = "${latestMetadata.fullStartDate}-${market.marketCode}-${isPortrait}",
                            attribution = "bing.com",
                            title = latestMetadata.title ?: "",
                            byline = latestMetadata.copyright ?: "",
                            persistentUri = latestMetadata.uri,
                            webUri = if (URLUtil.isNetworkUrl(latestMetadata.copyrightLink)) Uri.parse(latestMetadata.copyrightLink) else null,
                            metadata = latestMetadata.uri.toString().substringAfterLast("id=OHR.")).let { artwork ->
                        if (lastAddedArtwork?.token.equals(artwork.token)) {
                            LogUtil.LOGD(TAG, "Dropping artwork with token=${artwork.token}")
                        } else {
                            LogUtil.LOGD(TAG, "Setting artwork with token=${artwork.token}")
                            setArtwork(artwork)
                        }
                    }
                }
            }
            return Result.SUCCESS
        }
    }
}
