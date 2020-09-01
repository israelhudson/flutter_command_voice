package br.com.ihudtecnologia.flutter_command_voice

import android.Manifest
import android.app.Activity
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*

class MainActivity: FlutterActivity() {
    private val CHANNEL_VOICE_INVOKE_RECOGNIZER = "voice_invoke_recognizer"
    private val CHANNEL_VOICE_SPEECH_TEXT = "voice_speech_text"
    private val CHANNEL_VOICE_LISTENING_FEEDBACK = "voice_listening_feedback"

    private lateinit var channel_invoke_recognizer: MethodChannel
    private lateinit var channel_speech_text: MethodChannel
    private lateinit var channel_listening_feedback: MethodChannel

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechRecognizerIntent: Intent

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        channelOpenSpeechRecognizer(flutterEngine)
        channelGetTextSpeech(flutterEngine)
        channelListeningFeedback(flutterEngine)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            checkPermission()
        }

        init()
    }

    private fun init() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(bundle: Bundle) {}
            override fun onBeginningOfSpeech() {
                //editText.setText("")
                //editText.setHint("Listening...")
                Log.i("VOZ", "ouvindo")

                channel_listening_feedback.invokeMethod("voice_listening", true)
            }

            override fun onRmsChanged(v: Float) {}
            override fun onBufferReceived(bytes: ByteArray) {}
            override fun onEndOfSpeech() {}
            override fun onError(i: Int) {}
            override fun onResults(bundle: Bundle) {
                //micButton.setImageResource(R.drawable.ic_mic_black_off)
                val data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                //editText.setText(data!![0])
                Log.i("VOZ", data!![0])
                channel_speech_text.invokeMethod("voice_text", data!![0])
                channel_listening_feedback.invokeMethod("voice_listening", false)

                Log.i("VOZ", "ouvindo parou")

            }

            override fun onPartialResults(bundle: Bundle) {}
            override fun onEvent(i: Int, bundle: Bundle) {}
        })
    }

    private fun channelGetTextSpeech(flutterEngine: FlutterEngine) {
        channel_speech_text = MethodChannel(flutterEngine.dartExecutor.binaryMessenger,
                CHANNEL_VOICE_SPEECH_TEXT);
    }

    private fun channelListeningFeedback(flutterEngine: FlutterEngine) {
        channel_listening_feedback = MethodChannel(flutterEngine.dartExecutor.binaryMessenger,
                CHANNEL_VOICE_LISTENING_FEEDBACK);
        channel_listening_feedback.invokeMethod("voice_listening", false)
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
        speechRecognizer.startListening(speechRecognizerIntent)

    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer!!.destroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RecordAudioRequestCode && grantResults.size > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), RecordAudioRequestCode)
        }
    }

    companion object {
        const val RecordAudioRequestCode = 1
    }
}
