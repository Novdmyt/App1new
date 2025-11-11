package com.example.easylearnlanguage.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.example.easylearnlanguage.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

public class SettingsActivity extends AppCompatActivity {

    private Prefs prefs;
    private MaterialAutoCompleteTextView dropLang;
    private View root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = new Prefs(this);
        root  = findViewById(android.R.id.content);

        // Toolbar back
        MaterialToolbar bar = findViewById(R.id.topAppBar);
        setSupportActionBar(bar);
        bar.setNavigationOnClickListener(v ->
                getOnBackPressedDispatcher().onBackPressed()
        );

        // ---------------------------
        // 1) Мова інтерфейсу
        // ---------------------------
        dropLang = findViewById(R.id.drop_lang);

        String[] uiLabels = new String[] {
                getString(R.string.system_default),
                getString(R.string.ukrainian),
                getString(R.string.german),
                getString(R.string.english)
        };
        String[] uiTags = new String[] { "system", "uk", "de", "en" };

        dropLang.setSimpleItems(uiLabels);

        String savedTag = prefs.getLangTag();
        int uiIdx = 0;
        for (int i = 0; i < uiTags.length; i++) if (uiTags[i].equals(savedTag)) { uiIdx = i; break; }
        dropLang.setText(uiLabels[uiIdx], false);

        dropLang.setOnItemClickListener((parent, view, position, id) -> {
            dropLang.dismissDropDown();
            dropLang.clearFocus();
            root.requestFocus();

            String tag = uiTags[position];
            prefs.setLangTag(tag);
            if ("system".equals(tag)) {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList());
            } else {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag));
            }
            restartWithoutAnimation();
        });

        // ---------------------------
        // 2) Мова озвучки (TTS)
        // ---------------------------
        MaterialAutoCompleteTextView dropTts = findViewById(R.id.drop_tts_voice);

        String[] voiceLabels = new String[] {
                getString(R.string.voice_english),
                getString(R.string.voice_german),
                getString(R.string.voice_french)
        };
        String[] voiceTags = new String[] { "en", "de", "fr" };

        dropTts.setSimpleItems(voiceLabels);

        String savedVoice = prefs.getTtsLang();
        int vIdx = 0;
        for (int i = 0; i < voiceTags.length; i++) if (voiceTags[i].equals(savedVoice)) { vIdx = i; break; }
        dropTts.setText(voiceLabels[vIdx], false);

        dropTts.setOnItemClickListener((p, v, pos, id) -> prefs.setTtsLang(voiceTags[pos]));

        // ---------------------------
        // 3) Кількість варіантів у грі
        // ---------------------------
        MaterialAutoCompleteTextView dropChoices = findViewById(R.id.drop_choices);

        String[] choiceLabels = new String[] {
                getString(R.string.choices_3),
                getString(R.string.choices_4),
                getString(R.string.choices_5),
                getString(R.string.choices_6)
        };
        int[] choiceValues = new int[] { 3, 4, 5, 6 };

        dropChoices.setSimpleItems(choiceLabels);

        int savedChoices = prefs.getPlayChoices();
        int cIdx = 1; // дефолт 4
        for (int i = 0; i < choiceValues.length; i++) if (choiceValues[i] == savedChoices) { cIdx = i; break; }
        dropChoices.setText(choiceLabels[cIdx], false);

        dropChoices.setOnItemClickListener((p, v, pos, id) -> prefs.setPlayChoices(choiceValues[pos]));
    }

    private void restartWithoutAnimation() {
        Intent i = getIntent();
        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(i);
        overridePendingTransition(0, 0);
    }
}
