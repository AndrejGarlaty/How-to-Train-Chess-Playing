package com.example.thechesslearninggame;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import java.util.Locale;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Spinner spinnerLanguage = findViewById(R.id.spinner_language);
        Button btnSave = findViewById(R.id.btn_save_language);

        String[] languages = {"Slovenský", "English"};
        String[] languageCodes = {Language.SLOVAK.getCode(), Language.ENGLISH.getCode()};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, languages);
        spinnerLanguage.setAdapter(adapter);

        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        spinnerLanguage.setSelection(prefs.getString("selected_language", Locale.getDefault().getLanguage()).equals(Language.SLOVAK.getCode()) ? 0 : 1);

        btnSave.setOnClickListener(v -> {
            int position = spinnerLanguage.getSelectedItemPosition();
            String newLanguageCode = languageCodes[position];

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("selected_language", newLanguageCode);
            editor.apply();

            setResult(RESULT_OK);
            finish();
        });
    }
}
