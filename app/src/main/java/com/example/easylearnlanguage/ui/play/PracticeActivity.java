package com.example.easylearnlanguage.ui.play;

import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.easylearnlanguage.R;
import com.example.easylearnlanguage.data.Word;
import com.example.easylearnlanguage.settings.Prefs;
import com.example.easylearnlanguage.ui.word.WordsViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.Normalizer;
import java.util.*;
import java.util.Locale;

public class PracticeActivity extends AppCompatActivity {
    public static final String EXTRA_GROUP_ID = "group_id";
    public static final String EXTRA_GROUP_TITLE = "group_title";

    private TextInputEditText etPrompt, etAnswer;
    private TextInputLayout tilAnswer;
    private MaterialButton btnSpeak, btnCheck, btnHelp;

    private final List<Word> words = new ArrayList<>();
    private int index = 0;
    private Word current;

    private WordsViewModel vm;
    private TextToSpeech tts; private boolean ttsReady = false;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice);

        MaterialToolbar bar = findViewById(R.id.bar);
        bar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        String title = getIntent().getStringExtra(EXTRA_GROUP_TITLE);
        bar.setTitle(title != null ? title : getString(R.string.practice));

        etPrompt = findViewById(R.id.etPrompt);
        etAnswer = findViewById(R.id.etAnswer);
        tilAnswer = findViewById(R.id.tilAnswer);
        btnSpeak = findViewById(R.id.btnSpeak);
        btnCheck = findViewById(R.id.btnCheck);
        btnHelp  = findViewById(R.id.btnHelp);

        vm = new ViewModelProvider(this).get(WordsViewModel.class);
        long groupId = getIntent().getLongExtra(EXTRA_GROUP_ID, -1L);
        vm.wordsByGroup(groupId).observe(this, ws -> {
            if (ws == null || ws.isEmpty()) {
                Snackbar.make(tilAnswer, R.string.add_word, Snackbar.LENGTH_LONG).show();
                finish();
                return;
            }
            words.clear(); words.addAll(ws);
            Collections.shuffle(words);
            index = 0; showCurrent();
        });

        Prefs prefs = new Prefs(this);
        String tag = prefs.getTtsLang();
        tts = new TextToSpeech(this, s -> {
            if (s == TextToSpeech.SUCCESS) {
                int r = tts.setLanguage(toLocale(tag));
                ttsReady = (r != TextToSpeech.LANG_MISSING_DATA && r != TextToSpeech.LANG_NOT_SUPPORTED);
            }
        });

        btnSpeak.setOnClickListener(v -> speak());
        btnHelp.setOnClickListener(v -> reveal());
        btnCheck.setOnClickListener(v -> check());
        etAnswer.setOnEditorActionListener((tv, actionId, ev) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) { check(); return true; }
            return false;
        });
    }

    private void showCurrent() {
        if (words.isEmpty()) return;
        if (index >= words.size()) index = 0;
        current = words.get(index);
        etPrompt.setText(current.back);
        etAnswer.setText("");
        tilAnswer.setError(null);
        etAnswer.requestFocus();
    }

    private void check() {
        if (current == null) return;
        String user = norm(etAnswer.getText());
        if (TextUtils.isEmpty(user)) { tilAnswer.setError(getString(R.string.word_front)); return; }
        boolean ok = false;
        for (String variant : splitVariants(current.front)) {
            if (equalsSoft(user, norm(variant))) { ok = true; break; }
            String woArticle = dropGermanArticle(norm(variant));
            if (!woArticle.isEmpty() && equalsSoft(user, woArticle)) { ok = true; break; }
        }
        if (ok) { index++; showCurrent(); }
        else    { tilAnswer.setError(getString(R.string.title_correct)); }
    }

    private void reveal() {
        if (current == null) return;
        etAnswer.setText(current.front);
        Editable e = etAnswer.getText();
        if (e != null) etAnswer.setSelection(e.length());
        tilAnswer.setError(null);
        speak();
    }

    private void speak() {
        if (!ttsReady || current == null) return;
        if (Build.VERSION.SDK_INT >= 21) tts.speak(current.front, TextToSpeech.QUEUE_FLUSH, null, "practice");
        else /*noinspection deprecation*/ tts.speak(current.front, TextToSpeech.QUEUE_FLUSH, null);
    }

    private static String norm(CharSequence s) {
        if (s == null) return "";
        String out = Normalizer.normalize(s.toString(), Normalizer.Form.NFKC).trim();
        return out.replaceAll("\\s+", " ");
    }
    private static String[] splitVariants(String s) { return s == null ? new String[0] : s.split("[/;|,]"); }
    private static boolean equalsSoft(String a, String b) { return a.equalsIgnoreCase(b); }
    private static String dropGermanArticle(String s) {
        String t = s.toLowerCase(Locale.ROOT).trim();
        if (t.startsWith("der ")) return s.substring(4).trim();
        if (t.startsWith("die ")) return s.substring(4).trim();
        if (t.startsWith("das ")) return s.substring(4).trim();
        return s;
    }
    private static Locale toLocale(String tag) {
        if (tag == null) return Locale.ENGLISH;
        switch (tag) { case "de": return Locale.GERMAN; case "fr": return Locale.FRENCH; default: return Locale.ENGLISH; }
    }
    @Override protected void onDestroy() { if (tts != null) { tts.stop(); tts.shutdown(); } super.onDestroy(); }
}
