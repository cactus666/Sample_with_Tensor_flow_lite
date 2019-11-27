package com.pickingwords.mushroomchecker.action_with_bitmap

import android.graphics.Bitmap

fun croppedBitmap(originalBitmap: Bitmap, top: Int, bottom: Int, left: Int, right: Int): Bitmap {
    return Bitmap.createBitmap(originalBitmap, left, top, right - left, bottom - top)
}

fun croppedBitmap(originalBitmap: Bitmap, topPercent: Float, bottomPercent: Float, leftPercent: Float, rightPercent: Float): Bitmap {
    return Bitmap.createBitmap(originalBitmap,
        (originalBitmap.width * leftPercent).toInt(),
        (originalBitmap.height * topPercent).toInt(),
        (originalBitmap.width * rightPercent).toInt(),
        (originalBitmap.height * bottomPercent).toInt()
    )
}

fun resizeBitmap(originalBitmap: Bitmap, newWidth: Int = 480, newHeight: Int = 480): Bitmap {
    return Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, false)
}