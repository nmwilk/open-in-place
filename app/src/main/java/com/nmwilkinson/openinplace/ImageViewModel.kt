package com.nmwilkinson.openinplace

import android.net.Uri
import androidx.lifecycle.ViewModel
import timber.log.Timber

class ImageViewModel : ViewModel() {
    private var _sourceUri: Uri? = null

    val sourceUri get() = _sourceUri

    init {
        Timber.plant(Timber.DebugTree())
    }

    fun setSourceUri(sourceUri: Uri?) {
        _sourceUri = sourceUri
    }
}
