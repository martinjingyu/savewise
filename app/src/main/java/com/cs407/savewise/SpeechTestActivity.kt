package com.cs407.savewise

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cs407.savewise.service.WavAudioRecorder
import com.cs407.savewise.service.WhisperApi
import com.cs407.savewise.ui.component.AnimatedRecordButton
import kotlinx.coroutines.launch
import java.io.File

class SpeechTestActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            val scope = rememberCoroutineScope()
            var resultText by remember { mutableStateOf("Press to record...") }
            val context = LocalContext.current
            // 录音文件
            val audioFile = File(cacheDir, "test_audio.wav")

            // 录音器
            val recorder = remember {
                WavAudioRecorder(
                    context = context,
                    file = audioFile,
                    onError = { msg ->
                        resultText = "Error: $msg"
                        println("❌ $msg")
                    }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = resultText,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                AnimatedRecordButton(
                    onStart = {
                        resultText = "Recording..."
                        recorder.start()
                    },
                    onStop = {
                        resultText = "Processing..."
                        recorder.stop()

                        // 调用 Whisper
                        scope.launch {
                            val text = WhisperApi.transcribe(audioFile)
                            resultText = "Result:\n$text"
                        }
                    }
                )
            }
        }
    }
}