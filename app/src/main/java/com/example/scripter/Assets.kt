package com.example.scripter

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object Assets {
    fun getTessDataPath(context: Context): String {
        // We need to return folder that contains the "tessdata" folder,
        // which is in this sample directly the app's files dir
        return context.getFilesDir().getAbsolutePath()
    }

    val language: String
        get() = "kor"

    fun getImageFile(context: Context): File {
        return File(context.getFilesDir(), "img.png")
    }

    fun getImageBitmap(context: Context): Bitmap {
        return BitmapFactory.decodeFile(getImageFile(context).getAbsolutePath())
    }

    fun extractAssets(context: Context) {
        val am: AssetManager = context.getAssets()
        val imageFile: File = getImageFile(context)
        if (!imageFile.exists()) {
            copyFile(am, "img.png", imageFile)
        }
        val tessDir = File(getTessDataPath(context), "tessdata")
        if (!tessDir.exists()) {
            tessDir.mkdir()
        }
        val korFile = File(tessDir, "kor.traineddata")
        if (!korFile.exists()) {
            copyFile(am, "kor.traineddata", korFile)
        }
    }

    private fun copyFile(
        am: AssetManager, assetName: String,
        outFile: File
    ) {
        try {
            am.open(assetName).use { `in` ->
                FileOutputStream(outFile).use { out ->
                    val buffer = ByteArray(1024)
                    var read: Int
                    while (`in`.read(buffer).also { read = it } != -1) {
                        out.write(buffer, 0, read)
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}