package com.example.thechesslearninggame;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;

public class VoiceOutputManager implements TextToSpeech.OnInitListener {
    private final String TAG = "VoiceOutputManager";
    private final TextToSpeech tts;
    private final Context context;
    private boolean isInitialized = false;

    public VoiceOutputManager(Context context) {
        this.context = context;
        tts = new TextToSpeech(context, this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(new Locale("sk"));
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                String msg = "Language not supported";
                Log.e(TAG, "onInit: " + msg);
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            } else {
                isInitialized = true;
            }
        } else {
            String msg = "TTS initialization failed: " + status;
            Log.e(TAG, msg);
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        }
    }

    public void speak(String text) {
        if (isInitialized) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}
