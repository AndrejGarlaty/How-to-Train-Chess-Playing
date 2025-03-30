package com.example.thechesslearninggame.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        String languageCode = prefs.getString("selected_language", Locale.getDefault().getLanguage());
        Locale newLocale = new Locale(languageCode);
        Locale.setDefault(newLocale);
        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(newLocale);
        super.attachBaseContext(context.createConfigurationContext(configuration));
    }
}
