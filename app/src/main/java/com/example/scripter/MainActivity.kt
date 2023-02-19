package com.example.scripter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.scripter.Assets.getImageFile
import com.example.scripter.Assets.getTessDataPath
import com.example.scripter.Assets.language
import com.example.scripter.ui.theme.ScripterTheme
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.File


class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var text by remember { mutableStateOf("") }
            Assets.extractAssets(this@MainActivity)
            if (!viewModel.isInitialized) {
                val dataPath = getTessDataPath(this@MainActivity)
                val language = language
                viewModel.initTesseract(dataPath, language, TessBaseAPI.OEM_LSTM_ONLY)
            }

            viewModel.getProcessing().observe(
                this@MainActivity
            ) { processing: Boolean? ->
                println("processing: $processing")
            }
            viewModel.getProgress().observe(
                this@MainActivity
            ) { progress: String? ->
                println("progress: $progress")
            }
            viewModel.getResult().observe(
                this@MainActivity
            ) { result: String? ->
                if (result != null) {
                    text = result
                }
                println("result: $result")
            }

            ScripterTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Column {
                        Button(onClick = { val imageFile: File = getImageFile(this@MainActivity)
                            viewModel.recognizeImage(imageFile)}) {
                        }
                        Greeting(text)
                    }

                }
            }
        }
    }
}


@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ScripterTheme {
        Greeting("Android")
    }
}