package com.example.scripter

import android.app.Application
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.googlecode.tesseract.android.TessBaseAPI
import com.googlecode.tesseract.android.TessBaseAPI.ProgressValues
import java.io.File
import java.util.*


class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val tessApi: TessBaseAPI
    private val processing = MutableLiveData(false)
    private val progress = MutableLiveData<String>()
    private val result = MutableLiveData<String>()
    var isInitialized = false
        private set
    private var stopped = false

    init {
        tessApi = TessBaseAPI { progressValues: ProgressValues ->
            progress.postValue(
                "Progress: " + progressValues.percent + " %"
            )
        }

        // Show Tesseract version and library flavor at startup
        progress.value = String.format(
            Locale.KOREAN, "Tesseract %s (%s)",
            tessApi.version, tessApi.libraryFlavor
        )
    }

    override fun onCleared() {
        if (isProcessing()) {
            tessApi.stop()
        }
        // Don't forget to release TessBaseAPI
        tessApi.recycle()
    }

    fun initTesseract(dataPath: String, language: String, engineMode: Int) {
        Log.i(
            TAG, "Initializing Tesseract with: dataPath = [" + dataPath + "], " +
                    "language = [" + language + "], engineMode = [" + engineMode + "]"
        )
        try {
            this.isInitialized = tessApi.init(dataPath, language, engineMode)
        } catch (e: IllegalArgumentException) {
            this.isInitialized = false
            Log.e(TAG, "Cannot initialize Tesseract:", e)
        }
    }

    fun recognizeImage(imagePath: File) {
        if (!this.isInitialized) {
            Log.e(TAG, "recognizeImage: Tesseract is not initialized")
            return
        }
        if (isProcessing()) {
            Log.e(TAG, "recognizeImage: Processing is in progress")
            return
        }
        result.value = ""
        processing.value = true
        progress.value = "Processing..."
        stopped = false

        // Start process in another thread
        Thread {
            tessApi.setImage(imagePath)
            // Or set it as Bitmap, Pix,...
            // tessApi.setImage(imageBitmap);
            val startTime = SystemClock.uptimeMillis()

            // Use getHOCRText(0) method to trigger recognition with progress notifications and
            // ability to cancel ongoing processing.
            tessApi.getHOCRText(0)

            // Then get just normal UTF8 text as result. Using only this method would also trigger
            // recognition, but would just block until it is completed.
            val text = tessApi.utF8Text
            result.postValue(text)
            processing.postValue(false)
            if (stopped) {
                progress.postValue("Stopped.")
            } else {
                val duration = SystemClock.uptimeMillis() - startTime
                progress.postValue(
                    String.format(
                        Locale.ENGLISH,
                        "Completed in %.3fs.", duration / 1000f
                    )
                )
            }
        }.start()
    }

    fun stop() {
        if (!isProcessing()) {
            return
        }
        tessApi.stop()
        progress.value = "Stopping..."
        stopped = true
    }

    fun isProcessing(): Boolean {
        return java.lang.Boolean.TRUE == processing.value
    }

    fun getProcessing(): LiveData<Boolean> {
        return processing
    }

    fun getProgress(): LiveData<String> {
        return progress
    }

    fun getResult(): LiveData<String> {
        return result
    }

    companion object {
        private const val TAG = "MainViewModel"
    }
}