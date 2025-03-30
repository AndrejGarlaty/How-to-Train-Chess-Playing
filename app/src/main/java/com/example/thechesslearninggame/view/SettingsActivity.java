package com.example.thechesslearninggame.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.example.thechesslearninggame.utils.enums.Language;
import com.example.thechesslearninggame.utils.enums.Preferences;
import com.example.thechesslearninggame.R;
import com.example.thechesslearninggame.utils.enums.VoiceInput;

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

        SharedPreferences prefs = getSharedPreferences(Preferences.NAME.getValue(), MODE_PRIVATE);
        spinnerLanguage.setSelection(prefs.getString(Preferences.LANGUAGE.getValue(), Locale.getDefault().getLanguage()).equals(Language.SLOVAK.getCode()) ? 0 : 1);

        RadioGroup radioGroup = findViewById(R.id.rg_voice_input);
        String savedMode = prefs.getString(Preferences.VOICE_INPUT.getValue(), VoiceInput.NONE.name());
        if (VoiceInput.PUSH_TO_TALK.name().equals(savedMode)) {
            radioGroup.check(R.id.rb_push_to_talk);
        } else if (VoiceInput.CONTINOUS.name().equals(savedMode)) {
            radioGroup.check(R.id.rb_continuous);
        } else {
            radioGroup.check(R.id.rb_none);
        }

        btnSave.setOnClickListener(v -> {
            int position = spinnerLanguage.getSelectedItemPosition();
            String newLanguageCode = languageCodes[position];

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(Preferences.LANGUAGE.getValue(), newLanguageCode);

            VoiceInput chosenMode;
            int checkedId = radioGroup.getCheckedRadioButtonId();
            if (checkedId == R.id.rb_push_to_talk) {
                chosenMode = VoiceInput.PUSH_TO_TALK;
            } else if (checkedId == R.id.rb_continuous) {
                chosenMode = VoiceInput.CONTINOUS;
            } else {
                chosenMode = VoiceInput.NONE;
            }
            editor.putString(Preferences.VOICE_INPUT.getValue(), chosenMode.name());

            editor.apply();
            setResult(RESULT_OK);
            finish();
        });
    }
}
