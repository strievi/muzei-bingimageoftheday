/*
 * From: https://github.com/romannurik/muzei/blob/d9899b5d35336d7fdfd6007b13506b249afa754e/extensions/src/main/java/com/google/android/apps/muzei/util/BroadcastReceiverExt.kt
 */

package de.devmil.common.utils

import android.content.BroadcastReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun BroadcastReceiver.goAsync(
        coroutineScope: CoroutineScope = GlobalScope,
        block: suspend () -> Unit
) {
    val result = goAsync()
    coroutineScope.launch {
        try {
            block()
        } finally {
            // Always call finish(), even if the coroutineScope was cancelled
            result.finish()
        }
    }
}
