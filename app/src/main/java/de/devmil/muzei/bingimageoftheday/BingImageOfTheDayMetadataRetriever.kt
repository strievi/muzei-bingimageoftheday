/*
 * Copyright 2014 Devmil Solutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.devmil.muzei.bingimageoftheday

import android.net.Uri
import android.webkit.URLUtil
import de.devmil.common.utils.LogUtil

import java.util.ArrayList

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by devmil on 16.02.14.

 * Uses the Bing REST API to get the metadata for the current (and the last few) images of the day
 */
class BingImageOfTheDayMetadataRetriever(private val market: BingMarket, private val dimension: BingImageDimension, private val portrait: Boolean) {

    val bingImageOfTheDayMetadata: List<BingImageMetadata>?
        get() {
            LogUtil.LOGD(TAG, "Querying Bing API")
            val restAdapter = Retrofit.Builder()
                    .baseUrl(BING_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

            val service = restAdapter.create(IBingImageService::class.java)
            val response = service.getImageOfTheDayMetadata(MAXIMUM_BING_IMAGE_NUMBER, market.marketCode).execute().body()

            if (response?.images == null)
                return null

            return getMetadata(response.images)
        }

    private fun getMetadata(bingImages: List<IBingImageService.BingImage>): List<BingImageMetadata> {
        val result = ArrayList<BingImageMetadata>()
        for (bingImage in bingImages) {
            if (bingImage.urlbase.isNullOrBlank()) throw IllegalArgumentException("urlbase cannot be null or blank")
            if (bingImage.fullstartdate.isNullOrBlank()) throw IllegalArgumentException("fullstartdate cannot be null or blank")
            val uri = Uri.parse(BING_URL + bingImage.urlbase + "_" + dimension.getStringRepresentation(portrait) + ".jpg")
            val copyrightLink = if (URLUtil.isNetworkUrl(bingImage.copyrightlink)) Uri.parse(bingImage.copyrightlink) else null
            result.add(BingImageMetadata(uri, bingImage.copyright, bingImage.fullstartdate, copyrightLink, bingImage.title))
        }
        return result
    }

    companion object {
        private val TAG = BingImageOfTheDayMetadataRetriever::class.java.simpleName

        private const val BING_URL = "https://www.bing.com"

        private const val MAXIMUM_BING_IMAGE_NUMBER = 1
    }
}
