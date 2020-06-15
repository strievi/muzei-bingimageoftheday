/*
 * From: https://github.com/romannurik/muzei/blob/f929ba34a9376f5267bdaa0f8497f39671fcb1de/extensions/src/main/java/com/google/android/apps/muzei/util/ContextExt.kt
 */

package de.devmil.common.utils

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Creates and shows a [Toast] with the given [text]
 *
 * @param duration Toast duration, defaults to [Toast.LENGTH_SHORT]
 */
fun Context.toast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, duration).apply { show() }
}

/**
 * Creates and shows a [Toast] with text from a resource
 *
 * @param resId Resource id of the string resource to use
 * @param duration Toast duration, defaults to [Toast.LENGTH_SHORT]
 */
fun Context.toast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, resId, duration).apply { show() }
}

fun Context.toastFromBackground(
        @StringRes resId: Int,
        duration: Int = Toast.LENGTH_SHORT
) {
    GlobalScope.launch(Dispatchers.Main) {
        Toast.makeText(this@toastFromBackground, resId, duration).apply { show() }
    }
}
