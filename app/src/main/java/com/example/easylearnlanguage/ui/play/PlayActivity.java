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
import com.example.easylearnlanguage.data.Word;
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

    private static final int DEFAULT_CHOICES = 4; // максимум вариантов, если слов достаточно

    private long groupId = -1L;
    private WordsViewModel vm;

    private RecyclerView list;
    private ChoiceAdapter adapter;
    private MaterialTextView tvScore;
    private LinearProgressIndicator progress;
    private MaterialButton btnSpeak;

    private final List<Word> words = new ArrayList<>();
    private Word current;
    private int roundIndex = 0;
    private int correctCount = 0;
    private int totalRounds = 0;
    private int correctIndex = -1;

    private TextToSpeech tts;
    private boolean ttsReady = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        MaterialToolbar bar = findViewById(R.id.bar);
        if (bar != null) {
            String title = getIntent().getStringExtra(EXTRA_GROUP_TITLE);
            bar.setTitle(title != null ? title : getString(R.string.title_play));
            bar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        }

        tvScore  = findViewById(R.id.tv_score);
        progress = findViewById(R.id.progress);
        btnSpeak = findViewById(R.id.btn_speak);

        list = findViewById(R.id.list_choices);
        GridLayoutManager glm = new GridLayoutManager(this, 2);
        list.setLayoutManager(glm);
        adapter = new ChoiceAdapter((pos, text) -> onChoiceClicked(pos));
        list.setAdapter(adapter);

        btnSpeak.setOnClickListener(v -> speakCurrent());

        vm = new ViewModelProvider(this).get(WordsViewModel.class);
        Intent it = getIntent();
        groupId = it.getLongExtra(EXTRA_GROUP_ID, -1L);
        if (groupId <= 0L) {
            Snackbar.make(list, R.string.coming_soon, Snackbar.LENGTH_LONG).show();
            finish();
            return;
        }

        vm.wordsByGroup(groupId).observe(this, ws -> {
            words.clear();
            if (ws != null) words.addAll(ws);
            startGameOrExit();
        });

        Prefs prefs = new Prefs(this);
        String ttsTag = prefs.getTtsLang();
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                Locale locale = toLocale(ttsTag);
                int r = tts.setLanguage(locale);
                ttsReady = (r != TextToSpeech.LANG_MISSING_DATA && r != TextToSpeech.LANG_NOT_SUPPORTED);
            } else {
                ttsReady = false;
            }
        });
    }

    private void startGameOrExit() {
        if (words.isEmpty()) {
            Snackbar.make(list, R.string.add_word, Snackbar.LENGTH_LONG).show();
            finish();
            return;
        }
        correctCount = 0;
        roundIndex   = 0;
        totalRounds  = Math.max(words.size(), 1);

        updateScore();
        startRound();
    }

    private void startRound() {
        if (words.isEmpty()) return;

        if (roundIndex == 0) Collections.shuffle(words);
        current = words.get(roundIndex % words.size());

        int available = words.size();
        int choicesCount = Math.max(1, Math.min(DEFAULT_CHOICES, available));

        List<String> choices = new ArrayList<>();
        choices.add(current.back);

        List<Word> shuffled = new ArrayList<>(words);
        Collections.shuffle(shuffled);
        for (Word w : shuffled) {
            if (w.id == current.id) continue;
            if (choices.size() >= choicesCount) break;
            choices.add(w.back);
        }

        Collections.shuffle(choices);
        correctIndex = choices.indexOf(current.back);

        GridLayoutManager glm = (GridLayoutManager) list.getLayoutManager();
        if (glm != null) glm.setSpanCount(choices.size() >= 4 ? 2 : 1);

        adapter.setCorrectIndex(correctIndex);
        adapter.submit(choices);

        // Показываем слово на большой кнопке (без изменения дизайна)
        btnSpeak.setText(current.front);

        btnSpeak.setEnabled(true);
        speakCurrent();
    }

    private void onChoiceClicked(int pos) {
        boolean ok = (pos == correctIndex);
        if (ok) correctCount++;

        roundIndex++;
        if (roundIndex >= totalRounds) {
            roundIndex = 0;
            correctCount = 0;
        }
        updateScore();
        list.postDelayed(this::startRound, 500);
    }

    private void updateScore() {
        int finished = Math.max(roundIndex, 0);
        tvScore.setText(correctCount + "/" + finished);
        int p = finished == 0 ? 0 : Math.round(100f * correctCount / finished);
        progress.setProgress(p);
    }

    private void speakCurrent() {
        if (!ttsReady || current == null || tts == null) return;
        String text = current.front;
        if (Build.VERSION.SDK_INT >= 21) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "word");
        } else {
            //noinspection deprecation
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private static Locale toLocale(String tag) {
        if (tag == null) return Locale.ENGLISH;
        switch (tag) {
            case "de": return Locale.GERMAN;
            case "fr": return Locale.FRENCH;
            case "en":
            default:   return Locale.ENGLISH;
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }
}
