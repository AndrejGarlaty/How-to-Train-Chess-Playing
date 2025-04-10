package com.example.thechesslearninggame.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatActivity;

import com.example.thechesslearninggame.model.enums.Language;
import com.example.thechesslearninggame.model.enums.Preferences;

import java.util.Locale;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Preferences.NAME.getValue(), Context.MODE_PRIVATE);
        String languageCode = prefs.getString(Preferences.LANGUAGE.getValue(), null);
        if (languageCode==null) {
            SharedPreferences.Editor editor = prefs.edit();

            if (Locale.getDefault().getLanguage().equals(Language.SLOVAK.getCode())) {
                languageCode = Language.SLOVAK.getCode();
            } else {
                languageCode = Language.ENGLISH.getCode();
            }
            editor.putString(Preferences.LANGUAGE.getValue(), languageCode);
            editor.apply();
        }
        Locale newLocale = new Locale(languageCode);
        Locale.setDefault(newLocale);
        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(newLocale);
        super.attachBaseContext(context.createConfigurationContext(configuration));
    }
}
