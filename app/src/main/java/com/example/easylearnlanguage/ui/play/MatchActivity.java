package com.example.easylearnlanguage.ui.play;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easylearnlanguage.R;
import com.example.easylearnlanguage.data.Word;
import com.example.easylearnlanguage.settings.Prefs;
import com.example.easylearnlanguage.ui.word.WordsViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MatchActivity extends AppCompatActivity {

    public static final String EXTRA_GROUP_ID = "group_id";
    public static final String EXTRA_GROUP_TITLE = "group_title";

    private RecyclerView listLeft, listRight;
    private MaterialButton btnRestart, btnShuffle;
    private LeftAdapter leftAdapter;
    private RightAdapter rightAdapter;

    private final List<Word> leftWords = new ArrayList<>();
    private final List<Word> rightWords = new ArrayList<>();
    private final Set<Long> matched = new HashSet<>();

    private int selectedLeft = -1;
    private int selectedRight = -1;

    private WordsViewModel vm;
    private TextToSpeech tts;
    private boolean ttsReady = false;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);

        MaterialToolbar bar = findViewById(R.id.bar);
        bar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        String title = getIntent().getStringExtra(EXTRA_GROUP_TITLE);
        if (title != null) bar.setTitle(R.string.training);

        listLeft  = findViewById(R.id.list_left);
        listRight = findViewById(R.id.list_right);
        btnRestart = findViewById(R.id.btn_restart);
        btnShuffle = findViewById(R.id.btn_shuffle);

        listLeft.setLayoutManager(new LinearLayoutManager(this));
        listRight.setLayoutManager(new LinearLayoutManager(this));

        leftAdapter = new LeftAdapter();
        rightAdapter = new RightAdapter();
        listLeft.setAdapter(leftAdapter);
        listRight.setAdapter(rightAdapter);

        leftAdapter.setOnClick(this::onLeftClick);
        rightAdapter.setOnClick(this::onRightClick);

        btnRestart.setOnClickListener(v -> resetAll(false));
        btnShuffle.setOnClickListener(v -> resetAll(true));

        // data
        vm = new ViewModelProvider(this).get(WordsViewModel.class);
        long groupId = getIntent().getLongExtra(EXTRA_GROUP_ID, -1L);
        vm.wordsByGroup(groupId).observe(this, ws -> {
            if (ws == null || ws.isEmpty()) {
                Snackbar.make(listLeft, R.string.add_word, Snackbar.LENGTH_LONG).show();
                finish();
                return;
            }
            leftWords.clear();
            rightWords.clear();
            leftWords.addAll(ws);
            rightWords.addAll(ws);
            resetAll(true);
        });

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

    private void onLeftClick(int pos) {
        if (pos < 0 || pos >= leftWords.size()) return;
        Word w = leftWords.get(pos);
        if (matched.contains(w.id)) return;

        selectedLeft = pos;
        leftAdapter.setSelected(pos);

        if (ttsReady && w != null) {
            if (Build.VERSION.SDK_INT >= 21) tts.speak(w.front, TextToSpeech.QUEUE_FLUSH, null, "match");
            else /*noinspection deprecation*/ tts.speak(w.front, TextToSpeech.QUEUE_FLUSH, null);
        }
        checkIfPairReady();
    }

    private void onRightClick(int pos) {
        if (pos < 0 || pos >= rightWords.size()) return;
        Word w = rightWords.get(pos);
        if (matched.contains(w.id)) return;

        selectedRight = pos;
        rightAdapter.setSelected(pos);
        checkIfPairReady();
    }

    private void checkIfPairReady() {
        if (selectedLeft == -1 || selectedRight == -1) return;

        long idL = leftWords.get(selectedLeft).id;
        long idR = rightWords.get(selectedRight).id;

        if (idL == idR) {
            matched.add(idL);
            leftAdapter.markMatched(selectedLeft, true);
            rightAdapter.markMatched(selectedRight, true);
            clearSelections();
            if (matched.size() == leftWords.size()) {
                Snackbar.make(listLeft, R.string.done, Snackbar.LENGTH_SHORT).show();
            }
        } else {
            leftAdapter.flashWrong(selectedLeft);
            rightAdapter.flashWrong(selectedRight);
            listLeft.postDelayed(this::clearSelections, 500);
        }
    }

    private void clearSelections() {
        selectedLeft = -1;
        selectedRight = -1;
        leftAdapter.setSelected(-1);
        rightAdapter.setSelected(-1);
    }

    private void resetAll(boolean shuffle) {
        matched.clear();
        clearSelections();

        if (shuffle) {
            Collections.shuffle(leftWords);
            Collections.shuffle(rightWords);
        } else {
            Collections.sort(leftWords, (a,b) -> Long.compare(a.id, b.id));
            Collections.sort(rightWords, (a,b) -> Long.compare(a.id, b.id));
        }

        leftAdapter.submit(leftWords, matched);
        rightAdapter.submit(rightWords, matched);
    }

    private static Locale toLocale(String tag) {
        if (tag == null) return Locale.ENGLISH;
        switch (tag) { case "de": return Locale.GERMAN; case "fr": return Locale.FRENCH; default: return Locale.ENGLISH; }
    }

    @Override protected void onDestroy() {
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }

    // ---------------------- ADAPTERS ----------------------

    private static class LeftAdapter extends RecyclerView.Adapter<LeftVH> {
        private final List<Word> data = new ArrayList<>();
        private final Set<Long> matched = new HashSet<>();
        private int selected = -1;
        private int wrong = -1;
        private OnPosClick onClick;

        void submit(List<Word> items, Set<Long> matchedIds) {
            data.clear(); data.addAll(items);
            matched.clear(); matched.addAll(matchedIds);
            selected = wrong = -1;
            notifyDataSetChanged();
        }
        void setSelected(int pos){ selected = pos; wrong = -1; notifyDataSetChanged(); }
        void markMatched(int pos, boolean m){ matched.add(data.get(pos).id); notifyItemChanged(pos); }
        void flashWrong(int pos){ wrong = pos; notifyItemChanged(pos); }
        void setOnClick(OnPosClick cb){ onClick = cb; }

        @Override public LeftVH onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View v = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_match_left, parent, false);
            return new LeftVH(v);
        }
        @Override public void onBindViewHolder(LeftVH h, int pos) {
            Word w = data.get(pos);
            MaterialButton b = h.btn;
            b.setEnabled(!matched.contains(w.id));
            b.setText(R.string.tap_to_listen);
            b.setStrokeWidth(dp(h.btn, 1));

            if (matched.contains(w.id)) {
                tintButton(b, android.R.color.holo_green_dark);
            } else if (pos == wrong) {
                tintButton(b, android.R.color.holo_red_dark);
            } else if (pos == selected) {
                tintButton(b, R.color.md_primary);
            } else {
                resetTint(b);
            }
            b.setOnClickListener(v -> { if (onClick!=null) onClick.onClick(h.getAdapterPosition()); });
        }
        @Override public int getItemCount(){ return data.size(); }
    }

    private static class RightAdapter extends RecyclerView.Adapter<RightVH> {
        private final List<Word> data = new ArrayList<>();
        private final Set<Long> matched = new HashSet<>();
        private int selected = -1;
        private int wrong = -1;
        private OnPosClick onClick;

        void submit(List<Word> items, Set<Long> matchedIds) {
            data.clear(); data.addAll(items);
            matched.clear(); matched.addAll(matchedIds);
            selected = wrong = -1;
            notifyDataSetChanged();
        }
        void setSelected(int pos){ selected = pos; wrong = -1; notifyDataSetChanged(); }
        void markMatched(int pos, boolean m){ matched.add(data.get(pos).id); notifyItemChanged(pos); }
        void flashWrong(int pos){ wrong = pos; notifyItemChanged(pos); }
        void setOnClick(OnPosClick cb){ onClick = cb; }

        @Override public RightVH onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View v = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_match_right, parent, false);
            return new RightVH(v);
        }
        @Override public void onBindViewHolder(RightVH h, int pos) {
            Word w = data.get(pos);
            MaterialButton b = h.btn;
            b.setText(w.back);
            b.setEnabled(!matched.contains(w.id));
            b.setStrokeWidth(dp(h.btn, 1));

            if (matched.contains(w.id)) {
                tintButton(b, android.R.color.holo_green_dark);
            } else if (pos == wrong) {
                tintButton(b, android.R.color.holo_red_dark);
            } else if (pos == selected) {
                tintButton(b, R.color.md_primary);
            } else {
                resetTint(b);
            }
            b.setOnClickListener(v -> { if (onClick!=null) onClick.onClick(h.getAdapterPosition()); });
        }
        @Override public int getItemCount(){ return data.size(); }
    }

    private static class LeftVH extends RecyclerView.ViewHolder {
        MaterialButton btn;
        LeftVH(View v){ super(v); btn = v.findViewById(R.id.btn_left); }
    }
    private static class RightVH extends RecyclerView.ViewHolder {
        MaterialButton btn;
        RightVH(View v){ super(v); btn = v.findViewById(R.id.btn_right); }
    }

    interface OnPosClick { void onClick(int pos); }

    private static void tintButton(MaterialButton b, int colorRes) {
        int c = ContextCompat.getColor(b.getContext(), colorRes);
        b.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        b.setStrokeColor(ColorStateList.valueOf(c));
        b.setTextColor(c);
    }
    private static void resetTint(MaterialButton b) {
        // колір обводки і тексту беремо з поточної теми Material3
        int outline = MaterialColors.getColor(b, com.google.android.material.R.attr.colorOutline);
        int text    = MaterialColors.getColor(b, com.google.android.material.R.attr.colorOnSurface);
        b.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        b.setStrokeColor(ColorStateList.valueOf(outline));
        b.setTextColor(text);
    }
    private static int dp(View v, int d){ float s=v.getResources().getDisplayMetrics().density; return Math.round(d*s); }
}
