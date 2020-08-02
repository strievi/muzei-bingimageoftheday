package de.devmil.muzei.bingimageoftheday

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Binder
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.ProviderContract
import java.io.FileNotFoundException

class BingImageOfTheDayContentProvider : ContentProvider() {

    companion object {
        private val DEFAULT_PROJECTION = arrayOf(
                OpenableColumns.DISPLAY_NAME,
                OpenableColumns.SIZE)
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(
            uri: Uri,
            projection: Array<String>?,
            selection: String?,
            selectionArgs: Array<String>?,
            sortOrder: String?
    ): Cursor? {
        val context = context ?: return null
        val result = MatrixCursor(projection ?: DEFAULT_PROJECTION)
        // Clear the calling app's identity as it cannot be used when
        // accessing the non-exported BingImageOfTheDayArtProvider
        val token = Binder.clearCallingIdentity()
        try {
            val id = ContentUris.parseId(uri)
            val contentUri = ProviderContract.getContentUri(BuildConfig.BING_IMAGE_OF_THE_DAY_AUTHORITY)
            val artworkUri = ContentUris.withAppendedId(contentUri, id)
            val artwork = context.contentResolver.query(artworkUri, null, null, null, null)?.use {
                if (it.moveToFirst()) {
                    Artwork.fromCursor(it)
                } else {
                    null
                }
            }
            if (artwork != null && artwork.data.exists()) {
                result.newRow().apply {
                    add(OpenableColumns.DISPLAY_NAME, artwork.metadata)
                    add(OpenableColumns.SIZE, artwork.data.length())
                }
            }
        } finally {
            Binder.restoreCallingIdentity(token)
        }
        return result
    }

    override fun getType(uri: Uri): String? {
        val context = context ?: return null
        // Clear the calling app's identity as it cannot be used when
        // accessing the non-exported BingImageOfTheDayArtProvider
        val token = Binder.clearCallingIdentity()
        try {
            val id = ContentUris.parseId(uri)
            val contentUri = ProviderContract.getContentUri(BuildConfig.BING_IMAGE_OF_THE_DAY_AUTHORITY)
            val artworkUri = ContentUris.withAppendedId(contentUri, id)
            val artwork = context.contentResolver.query(artworkUri, null, null, null, null)?.use {
                if (it.moveToFirst()) {
                    Artwork.fromCursor(it)
                } else {
                    null
                }
            }
            if (artwork?.metadata != null) {
                // Taken from FileProvider's source
                val lastDot: Int = artwork.metadata.toString().lastIndexOf('.')
                if (lastDot >= 0) {
                    val extension: String = artwork.metadata.toString().substring(lastDot + 1)
                    val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                    if (mime != null) {
                        return mime
                    }
                }
                return "application/octet-stream"
            }
        } finally {
            Binder.restoreCallingIdentity(token)
        }
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    @Throws(FileNotFoundException::class)
    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val context = context ?: return null
        // Clear the calling app's identity as it cannot be used when
        // accessing the non-exported BingImageOfTheDayArtProvider
        val token = Binder.clearCallingIdentity()
        try {
            val id = ContentUris.parseId(uri)
            val contentUri = ProviderContract.getContentUri(BuildConfig.BING_IMAGE_OF_THE_DAY_AUTHORITY)
            val artworkUri = ContentUris.withAppendedId(contentUri, id)
            return context.contentResolver.openFileDescriptor(artworkUri, mode)
        } finally {
            Binder.restoreCallingIdentity(token)
        }
    }
}