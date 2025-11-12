package com.example.easylearnlanguage.ui.word;

import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easylearnlanguage.R;
import com.example.easylearnlanguage.data.Word;
import com.example.easylearnlanguage.settings.Prefs;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.text.Normalizer;
import java.util.*;
import java.util.Locale;

public class DictionaryActivity extends AppCompatActivity {

    private WordsViewModel vm;
    private DictionaryAdapter adapter;
    private final List<Word> all = new ArrayList<>();
    private TextToSpeech tts; private boolean ttsReady = false;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);

        MaterialToolbar bar = findViewById(R.id.bar);
        bar.setTitle(R.string.title_dictionary);
        bar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        RecyclerView list = findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DictionaryAdapter(this::speak);
        list.setAdapter(adapter);

        TextInputEditText etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { filter(s); }
            @Override public void afterTextChanged(Editable s) {}
        });

        vm = new ViewModelProvider(this).get(WordsViewModel.class);
        vm.allWords().observe(this, ws -> {
            all.clear();
            if (ws != null) all.addAll(ws);
            adapter.submit(all);
            if (all.isEmpty()) Snackbar.make(list, R.string.add_word, Snackbar.LENGTH_LONG).show();
        });

        Prefs prefs = new Prefs(this);
        String tag = prefs.getTtsLang();
        tts = new TextToSpeech(this, s -> {
            if (s == TextToSpeech.SUCCESS) {
                int r = tts.setLanguage(toLocale(tag));
                ttsReady = (r != TextToSpeech.LANG_MISSING_DATA && r != TextToSpeech.LANG_NOT_SUPPORTED);
            }
        });
    }

    private void filter(CharSequence qs) {
        String q = norm(qs);
        if (q.isEmpty()) { adapter.submit(all); return; }
        List<Word> out = new ArrayList<>();
        for (Word w : all) {
            if (norm(w.front).contains(q) || norm(w.back).contains(q)) out.add(w);
        }
        adapter.submit(out);
    }

    private static String norm(CharSequence s){
        if(s==null) return "";
        return Normalizer.normalize(s.toString(), Normalizer.Form.NFKC)
                .trim().toLowerCase(Locale.ROOT);
    }

    private void speak(Word w, View a){
        if(!ttsReady || w==null) return;
        if (Build.VERSION.SDK_INT >= 21) tts.speak(w.front, TextToSpeech.QUEUE_FLUSH, null, "dict");
        else /*noinspection deprecation*/ tts.speak(w.front, TextToSpeech.QUEUE_FLUSH, null);
    }
    private static Locale toLocale(String tag){
        if(tag==null) return Locale.ENGLISH;
        switch(tag){ case "de": return Locale.GERMAN; case "fr": return Locale.FRENCH; default: return Locale.ENGLISH; }
    }
    @Override protected void onDestroy(){ if(tts!=null){ tts.stop(); tts.shutdown(); } super.onDestroy(); }
}
