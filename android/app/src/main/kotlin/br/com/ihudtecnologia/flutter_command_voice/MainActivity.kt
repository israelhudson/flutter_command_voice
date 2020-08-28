package br.com.ihudtecnologia.flutter_command_voice

import android.app.Activity
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

import android.content.Intent
import android.speech.RecognizerIntent
import androidx.annotation.NonNull

class MainActivity: FlutterActivity() {
    private val SPEECH_REQUEST_CODE = 0
    private val CHANNEL_VOICE_INVOKE_RECOGNIZER = "voice_invoke_recognizer"
    private val CHANNEL_VOICE_SPEECH_TEXT = "voice_speech_text"
    private lateinit var channel_invoke_recognizer: MethodChannel
    private lateinit var channel_speech_text: MethodChannel

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        channelOpenSpeechRecognizer(flutterEngine)
        channelGetTextSpeech(flutterEngine)
    }

    private fun channelGetTextSpeech(flutterEngine: FlutterEngine) {
        channel_speech_text = MethodChannel(flutterEngine.dartExecutor.binaryMessenger,
                CHANNEL_VOICE_SPEECH_TEXT);
    }

    private fun channelOpenSpeechRecognizer(flutterEngine: FlutterEngine) {
        channel_invoke_recognizer = MethodChannel(flutterEngine.dartExecutor.binaryMessenger,
                CHANNEL_VOICE_INVOKE_RECOGNIZER);
        channel_invoke_recognizer.setMethodCallHandler { call, result ->
            if (call.method == "displaySpeechRecognizer") {
                displaySpeechRecognizer()
                result.success(true)
            } else {
                result.error("UNAVAILABLE", "ERROR", null)
            }
        }
    }

    private fun displaySpeechRecognizer() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        }
        // Start the activity, the intent will be populated with the speech text
        startActivityForResult(intent, SPEECH_REQUEST_CODE)
    }

    // This callback is invoked when the Speech Recognizer returns.
    // This is where you process the intent and extract the speech text from the intent.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val spokenText: String? =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).let { results ->
                        results[0]
                    }

                channel_speech_text.invokeMethod("voice_text", spokenText)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
