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

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by devmil on 18.02.14.

 * Interface for the Bing Image Of the day REST API
 */
interface IBingImageService {

    @GET("/HPImageArchive.aspx?format=js&idx=0")
    fun getImageOfTheDayMetadata(@Query("n") number: Int, @Query("mkt") market: String): Call<BingImageResponse>

    data class BingImageResponse(
            val images: List<BingImage>?
    )

    data class BingImage(
            val startdate: String? = null,
            val fullstartdate: String? = null,
            val enddate: String? = null,
            val url: String? = null,
            val urlbase: String? = null,
            val copyright: String? = null,
            val copyrightlink: String? = null,
            val title: String? = null,
            val quiz: String? = null,
            val wp: Boolean? = true,
            val hsh: String? = null,
            val drk: Int? = 1,
            val top: Int? = 1,
            val bot: Int? = 1
    )
}
