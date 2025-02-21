package com.example.thechesslearninggame;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import java.util.ArrayList;

public class VoiceInputManager {
    private Context context;
    private SpeechRecognizer speechRecognizer;
    private Intent speechIntent;
    private VoiceInputListener listener;

    public interface VoiceInputListener {
        void onVoiceCommandRecognized(String command);
        void onError(int errorCode);
    }

    public VoiceInputManager(Context context, VoiceInputListener listener) {
        this.context = context;
        this.listener = listener;
        initializeSpeechRecognizer();
    }

    private void initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) { }

                @Override
                public void onBeginningOfSpeech() { }

                @Override
                public void onRmsChanged(float rmsdB) { }

                @Override
                public void onBufferReceived(byte[] buffer) { }

                @Override
                public void onEndOfSpeech() { }

                @Override
                public void onError(int error) {
                    // Pass error code back to the Activity/Caller
                    if (listener != null) {
                        listener.onError(error);
                    }
                }

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        String voiceInput = matches.get(0);
                        if (listener != null) {
                            listener.onVoiceCommandRecognized(voiceInput);
                        }
                    }
                }

                @Override
                public void onPartialResults(Bundle partialResults) { }

                @Override
                public void onEvent(int eventType, Bundle params) { }
            });

            speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            // Optionally set additional parameters (e.g., language, prompt, etc.)
        }
    }

    public void startListening() {
        if (speechRecognizer != null) {
            speechRecognizer.startListening(speechIntent);
        }
    }

    public void stopListening() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
        }
    }

    public void destroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
    }
}
