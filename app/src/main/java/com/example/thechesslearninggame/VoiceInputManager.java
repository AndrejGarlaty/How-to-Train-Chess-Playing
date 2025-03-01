package com.example.thechesslearninggame;

import android.content.Context;
import android.content.Intent;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class VoiceInputManager {

    public interface VoiceInputCallback {
        void onVoiceInputResult(String text);
        void onVoiceInputError(String error);
    }
    private final String TAG = "VoiceInputManager";
    private final SpeechRecognizer speechRecognizer;
    private final Intent speechRecognizerIntent;
    private final Context context;
    private final VoiceInputCallback callback;

    public VoiceInputManager(Context context, VoiceInputCallback callback) {
        this.context = context;
        this.callback = callback;
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "sk-SK");

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(android.os.Bundle params) {}

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {}

            @Override
            public void onError(int error) {
                callback.onVoiceInputError(getErrorText(error));
            }

            @Override
            public void onResults(android.os.Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String text = matches.get(0);
                    callback.onVoiceInputResult(text);
                }
            }

            @Override
            public void onPartialResults(android.os.Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, android.os.Bundle params) {

            }
        });
    }

    public void startListening() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer.startListening(speechRecognizerIntent);
        } else {
            String msg = "Speech recognition unavailable";
            Log.i(TAG, msg);
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        }
    }

    public void destroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }

    private String getErrorText(int errorCode) {
        return switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO -> "Audio recording error";
            case SpeechRecognizer.ERROR_CLIENT -> "Client side error";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions";
            case SpeechRecognizer.ERROR_NETWORK -> "Network error";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout";
            case SpeechRecognizer.ERROR_NO_MATCH -> "No match found";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy";
            case SpeechRecognizer.ERROR_SERVER -> "Error from server";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input";
            default -> "Didn't understand, please try again.";
        };
    }
}
