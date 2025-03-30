package com.example.thechesslearninggame.modules;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;

public class VoiceOutputManager implements TextToSpeech.OnInitListener {
    private final String TAG = "VoiceOutputManager";
    private final TextToSpeech tts;
    private final Context context;
    private boolean isInitialized = false;
    private OnTtsCompleteListener onTtsCompleteListener;

    public interface OnTtsCompleteListener {
        void onTtsComplete();
    }

    public void setOnTtsCompleteListener(OnTtsCompleteListener listener) {
        this.onTtsCompleteListener = listener;
    }

    public VoiceOutputManager(Context context) {
        this.context = context;
        tts = new TextToSpeech(context, this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            String languageValue = context.getSharedPreferences("AppSettings", MODE_PRIVATE)
                    .getString("selected_language", Locale.getDefault().getLanguage());
            int result = tts.setLanguage(new Locale(languageValue));
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                String msg = "Language not supported";
                Log.e(TAG, "onInit: " + msg);
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            } else {
                isInitialized = true;
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            if (onTtsCompleteListener != null) {
                                onTtsCompleteListener.onTtsComplete();
                            }
                        }, 1000);
                    }

                    @Override
                    public void onError(String utteranceId) {
                    }
                });
            }
        } else {
            String msg = "TTS initialization failed: " + status;
            Log.e(TAG, msg);
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        }
    }

    public void speak(String text) {
        if (isInitialized) {
            String utteranceId = "TTS_UTTERANCE";
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
        }
    }

    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}
