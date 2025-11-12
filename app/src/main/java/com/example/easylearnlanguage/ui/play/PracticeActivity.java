package com.example.easylearnlanguage.ui.play;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.easylearnlanguage.R;
import com.example.easylearnlanguage.data.Word;
import com.example.easylearnlanguage.settings.Prefs;
import com.example.easylearnlanguage.ui.word.WordsViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PracticeActivity extends AppCompatActivity {

    public static final String EXTRA_GROUP_ID = "group_id";
    public static final String EXTRA_GROUP_TITLE = "group_title";

    private WordsViewModel vm;
    private final List<Word> words = new ArrayList<>();
    private Word current;
    private int index = 0;

    private MaterialTextView tvFront, tvBack;
    private MaterialButton btnReveal, btnNext, btnSpeak;

    private TextToSpeech tts;
    private boolean ttsReady = false;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice);

        MaterialToolbar bar = findViewById(R.id.bar);
        String title = getIntent().getStringExtra(EXTRA_GROUP_TITLE);
        bar.setTitle(title != null ? title : getString(R.string.practice));
        bar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        tvFront = findViewById(R.id.tv_front);
        tvBack  = findViewById(R.id.tv_back);
        btnReveal = findViewById(R.id.btn_reveal);
        btnNext   = findViewById(R.id.btn_next);
        btnSpeak  = findViewById(R.id.btn_speak_practice);

        long groupId = getIntent().getLongExtra(EXTRA_GROUP_ID, -1L);
        vm = new ViewModelProvider(this).get(WordsViewModel.class);
        vm.wordsByGroup(groupId).observe(this, list -> {
            words.clear();
            if (list != null) words.addAll(list);
            if (words.isEmpty()) {
                Snackbar.make(findViewById(android.R.id.content), R.string.add_word, Snackbar.LENGTH_LONG).show();
                finish();
            } else {
                index = 0;
                showCard();
            }
        });

        btnReveal.setOnClickListener(v -> tvBack.setVisibility(View.VISIBLE));
        btnNext.setOnClickListener(v -> { index++; showCard(); });
        btnSpeak.setOnClickListener(v -> speak());

        // TTS
        Prefs prefs = new Prefs(this);
        String tag = prefs.getTtsLang();
        tts = new TextToSpeech(this, s -> {
            if (s == TextToSpeech.SUCCESS) {
                int r = tts.setLanguage(toLocale(tag));
                ttsReady = (r != TextToSpeech.LANG_MISSING_DATA && r != TextToSpeech.LANG_NOT_SUPPORTED);
            }
        });
    }

    private void showCard() {
        if (words.isEmpty()) return;
        current = words.get(index % words.size());
        tvFront.setText(current.front);
        tvBack.setText(current.back);
        tvBack.setVisibility(View.GONE);
        btnSpeak.setText(current.front);
        speak();
    }

    private void speak() {
        if (!ttsReady || current == null) return;
        if (Build.VERSION.SDK_INT >= 21) {
            tts.speak(current.front, TextToSpeech.QUEUE_FLUSH, null, "practice");
        } else {
            //noinspection deprecation
            tts.speak(current.front, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private static Locale toLocale(String tag) {
        if (tag == null) return Locale.ENGLISH;
        switch (tag) { case "de": return Locale.GERMAN; case "fr": return Locale.FRENCH; default: return Locale.ENGLISH; }
    }

    @Override protected void onDestroy() {
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }
}
