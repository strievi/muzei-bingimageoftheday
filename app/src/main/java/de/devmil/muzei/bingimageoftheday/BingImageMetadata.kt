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

/**
 * Created by devmil on 17.02.14.

 * This class represents one Bing image containing all data that this app needs
 */
class BingImageMetadata(uri: Uri, copyright: String?, fullStartDate: String, copyrightLink: Uri?, title: String?) {
    var uri: Uri = uri
    var copyright: String? = copyright
    var fullStartDate: String = fullStartDate
    var copyrightLink: Uri? = copyrightLink
    var title: String? = title
}
