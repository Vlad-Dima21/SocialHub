package com.vladima.socialhub.ui.main.home

import android.graphics.Bitmap

data class RVUserPost(
    var fileName: String = "",
    var imageBitmap: Bitmap,
    var imageDescription: String = ""
)
