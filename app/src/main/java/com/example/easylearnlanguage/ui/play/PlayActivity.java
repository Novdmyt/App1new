package com.example.easylearnlanguage.ui.play;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easylearnlanguage.R;
import com.example.easylearnlanguage.data.Word; // важливо: саме data.Word
import com.example.easylearnlanguage.settings.Prefs;
import com.example.easylearnlanguage.ui.word.WordsViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PlayActivity extends AppCompatActivity {

    public static final String EXTRA_GROUP_ID = "group_id";
    public static final String EXTRA_GROUP_TITLE = "group_title";

    private long groupId = -1L;
    private WordsViewModel vm;
    private TextToSpeech tts;
    private boolean ttsReady = false;

    private MaterialButton btnSpeak;
    private RecyclerView list;
    private ChoiceAdapter adapter;

    private final List<Word> pool = new ArrayList<>();
    private Word current;
    private final List<String> choices = new ArrayList<>();
    private int correctIndex = -1;

    private MaterialTextView tvScore;
    private LinearProgressIndicator progress;
    private int totalRounds = 0;
    private int correctCount = 0;

    private Prefs prefs;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        prefs = new Prefs(this);

        Intent in = getIntent();
        groupId = in.getLongExtra(EXTRA_GROUP_ID, -1);
        String title = in.getStringExtra(EXTRA_GROUP_TITLE);

        MaterialToolbar bar = findViewById(R.id.bar);
        bar.setTitle(title != null ? title : getString(R.string.title_play));
        bar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        btnSpeak = findViewById(R.id.btn_speak);
        list     = findViewById(R.id.list_choices);
        tvScore  = findViewById(R.id.tv_score);
        progress = findViewById(R.id.progress);
        progress.setMax(100);
        progress.setProgress(0);
        updateScore();

        list.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ChoiceAdapter(this::onChoiceClick);
        list.setAdapter(adapter);

        initTts(prefs.getTtsLang());

        vm = new ViewModelProvider(this).get(WordsViewModel.class);
        vm.wordsByGroup(groupId).observe(this, words -> {
            pool.clear();
            if (words != null) pool.addAll(words);
            startRound();
        });

        btnSpeak.setOnClickListener(v -> speakCurrent());
    }

    private void initTts(String bcp47) {
        ttsReady = false;
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                Locale loc = Locale.forLanguageTag(bcp47);
                int r = tts.setLanguage(loc);
                if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts.setLanguage(Locale.ENGLISH);
                }
                tts.setPitch(1.0f);
                tts.setSpeechRate(0.95f);
                ttsReady = true;
                speakCurrent(); // якщо вже є слово
            }
        });
    }

    private void startRound() {
        int N = prefs.getPlayChoices(); // 3..6
        if (pool.size() < N) {
            adapter.submit(Collections.emptyList());
            btnSpeak.setEnabled(false);
            Snackbar.make(list, R.string.coming_soon, Snackbar.LENGTH_LONG).show();
            return;
        }

        totalRounds++;
        updateScore();

        Collections.shuffle(pool);
        List<Word> pick = new ArrayList<>(pool.subList(0, N));

        current = pick.get(0);

        choices.clear();
        for (Word w : pick) choices.add(w.back);
        Collections.shuffle(choices);
        correctIndex = choices.indexOf(current.back);

        adapter.setCorrectIndex(correctIndex);
        adapter.submit(choices);

        btnSpeak.setEnabled(true);
        speakCurrent();
    }

    private void speakCurrent() {
        if (!ttsReady || current == null || tts == null) return;
        String text = current.front;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "cur");
        } else {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private void onChoiceClick(int pos, String text) {
        boolean ok = (pos == correctIndex);
        if (ok) correctCount++;
        adapter.reveal(pos, correctIndex);
        updateScore();

        list.postDelayed(this::startRound, 600);
    }

    private void updateScore() {
        int finished = Math.max(totalRounds - 1, 0);
        tvScore.setText(correctCount + "/" + finished);
        int p = finished == 0 ? 0 : Math.round(100f * correctCount / finished);
        progress.setProgress(p);
    }

    @Override protected void onDestroy() {
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }
}
