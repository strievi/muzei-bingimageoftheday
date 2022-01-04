package de.devmil.muzei.bingimageoftheday.worker

import android.content.Context
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

        internal fun enqueueLoad(context: Context) {
            LogUtil.LOGD(TAG, "Received enqueue request")
            val workManager = WorkManager.getInstance(context)
            workManager.enqueueUniqueWork(
                    TAG,
                    ExistingWorkPolicy.REPLACE,
                    OneTimeWorkRequestBuilder<BingImageOfTheDayWorker>()
                            .setConstraints(Constraints.Builder()
                                    .setRequiredNetworkType(NetworkType.CONNECTED)
                                    .build())
                            .build())
        }
    }

    override fun doWork(): Result {
        LogUtil.LOGD(TAG, "Starting background work")
        try {
            val settings = Settings(applicationContext)
            val isPortrait = settings.isOrientationPortrait
            val market = settings.bingMarket
            val isCropImage = settings.isCropImage
            val retriever = BingImageOfTheDayMetadataRetriever(
                    market,
                    BingImageDimension.HD,
                    isPortrait
            )
            val imagesMetadata = retriever.bingImageOfTheDayMetadata ?: listOf()
            if (imagesMetadata.isEmpty()) {
                LogUtil.LOGW(TAG, "Bing API returned no result")
                return Result.failure()
            }
            imagesMetadata.maxByOrNull { it.fullStartDate }?.let { latestMetadata ->
                val latestToken = "${latestMetadata.fullStartDate}-${market.marketCode}-${isPortrait}-${isCropImage}"
                ProviderContract.getProviderClient(
                        applicationContext, BING_IMAGE_OF_THE_DAY_AUTHORITY).run {
                    if (!lastAddedArtwork?.token.equals(latestToken)) {
                        val defaultFilename = latestMetadata.uri.toString().substringAfterLast("id=OHR.")
                        val filename = if (settings.isCropImage)
                            defaultFilename.removeSuffix(".jpg").plus("_Cropped.jpg")
                        else
                            defaultFilename
                        Artwork(
                                token = latestToken,
                                attribution = "bing.com",
                                title = latestMetadata.title,
                                byline = latestMetadata.copyright,
                                persistentUri = latestMetadata.uri,
                                webUri = latestMetadata.copyrightLink,
                                metadata = filename).let { artwork ->
                            LogUtil.LOGD(TAG, "Setting artwork with token=${artwork.token}")
                            setArtwork(artwork)
                        }
                    }
                }
            }
            return Result.success()
        } catch (e: Exception) {
            LogUtil.LOGE(TAG, "Error executing background work", e)
            return Result.retry()
        }
    }
}
