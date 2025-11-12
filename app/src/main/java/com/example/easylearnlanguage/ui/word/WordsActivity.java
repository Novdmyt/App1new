package com.example.easylearnlanguage.ui.word;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easylearnlanguage.R;
import com.example.easylearnlanguage.data.Word;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class WordsActivity extends AppCompatActivity {

    public static final String EXTRA_GROUP_ID = "group_id";
    public static final String EXTRA_GROUP_TITLE = "group_title";

    private WordsViewModel vm;
    private long groupId;

    // 🔹 поточні слова цієї групи
    private final List<Word> currentWords = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_words);

        Intent i = getIntent();
        groupId = i.getLongExtra(EXTRA_GROUP_ID, -1);
        String title = i.getStringExtra(EXTRA_GROUP_TITLE);
        if (groupId <= 0) { finish(); return; }

        MaterialToolbar bar = findViewById(R.id.bar);
        bar.setTitle(getString(R.string.title_new_words));
        bar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        RecyclerView list = findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(this));
        WordAdapter adapter = new WordAdapter();
        adapter.setOnLongClick((anchor, w, pos) -> showWordMenu(anchor, w));
        list.setAdapter(adapter);

        vm = new ViewModelProvider(this).get(WordsViewModel.class);

        // 🔹 зберігаємо список слів у currentWords для перевірок
        vm.wordsByGroup(groupId).observe(this, words -> {
            currentWords.clear();
            if (words != null) currentWords.addAll(words);
            adapter.submit(words);
        });

        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> showAddWordDialog());
    }

    private void showAddWordDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_word, null, false);

        EditText etFront = view.findViewById(R.id.etFront);
        EditText etBack  = view.findViewById(R.id.etBack);

        MaterialAutoCompleteTextView dropSrc = view.findViewById(R.id.dropSrcLang);
        MaterialAutoCompleteTextView dropDst = view.findViewById(R.id.dropDstLang);

        String[] labels = new String[] {
                getString(R.string.english),
                getString(R.string.ukrainian),
                getString(R.string.german)
        };
        String[] tags = new String[] { "en", "uk", "de" };

        dropSrc.setSimpleItems(labels);
        dropDst.setSimpleItems(labels);

        int srcIdx = 2; // de
        int dstIdx = 1; // uk
        dropSrc.setText(labels[srcIdx], false);
        dropDst.setText(labels[dstIdx], false);

        applyLocale(etFront, tags[srcIdx]);
        applyLocale(etBack,  tags[dstIdx]);

        dropSrc.setOnItemClickListener((p, v, pos, id) -> applyLocale(etFront, tags[pos]));
        dropDst.setOnItemClickListener((p, v, pos, id) -> applyLocale(etBack,  tags[pos]));

        etFront.setOnFocusChangeListener((v, has) -> {
            if (has) {
                int pos = Arrays.asList(labels).indexOf(dropSrc.getText().toString());
                if (pos >= 0) applyLocale(etFront, tags[pos]);
            }
        });
        etBack.setOnFocusChangeListener((v, has) -> {
            if (has) {
                int pos = Arrays.asList(labels).indexOf(dropDst.getText().toString());
                if (pos >= 0) applyLocale(etBack, tags[pos]);
            }
        });

        new AlertDialog.Builder(this)
                .setTitle(R.string.add_word)
                .setView(view)
                .setPositiveButton(android.R.string.ok, (d, w) -> {
                    String front = etFront.getText().toString().trim();
                    String back  = etBack.getText().toString().trim();
                    if (front.isEmpty() || back.isEmpty()) return;

                    // 🔹 перевірка на дубль
                    if (isDuplicateInGroup(groupId, -1, front, back)) {
                        Snackbar.make(findViewById(android.R.id.content),
                                R.string.word_exists, Snackbar.LENGTH_LONG).show();
                        return;
                    }

                    vm.add(groupId, front, back);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void applyLocale(EditText edit, String bcp47Tag) {
        Locale loc = Locale.forLanguageTag(bcp47Tag);
        if (Build.VERSION.SDK_INT >= 24) {
            android.os.LocaleList ll = new android.os.LocaleList(loc);
            edit.setTextLocales(ll);
            edit.setImeHintLocales(ll);
        } else if (Build.VERSION.SDK_INT >= 21) {
            edit.setTextLocale(loc);
        }
    }

    private void showWordMenu(View anchor, Word w) {
        android.widget.PopupMenu pm = new android.widget.PopupMenu(this, anchor);
        pm.getMenu().add(0, 1, 0, getString(R.string.rename));
        pm.getMenu().add(0, 2, 1, getString(R.string.delete));

        pm.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) { showRenameDialog(w); return true; }
            if (item.getItemId() == 2) {
                vm.delete(w);
                Snackbar.make(findViewById(android.R.id.content),
                                R.string.word_deleted, Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo, v -> vm.add(w.groupId, w.front, w.back))
                        .show();
                return true;
            }
            return false;
        });
        pm.show();
    }

    private void showRenameDialog(Word w) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_rename_word, null, false);
        TextInputEditText etFront = view.findViewById(R.id.etFront);
        TextInputEditText etBack  = view.findViewById(R.id.etBack);
        etFront.setText(w.front);
        etBack.setText(w.back);

        new AlertDialog.Builder(this)
                .setTitle(R.string.rename_word)
                .setView(view)
                .setPositiveButton(android.R.string.ok, (d, which) -> {
                    String nf = etFront.getText().toString().trim();
                    String nb = etBack.getText().toString().trim();
                    if (nf.isEmpty() || nb.isEmpty()) return;

                    // 🔹 при перейменуванні також не даємо зробити дубль
                    if (isDuplicateInGroup(w.groupId, w.id, nf, nb)) {
                        Snackbar.make(findViewById(android.R.id.content),
                                R.string.word_exists, Snackbar.LENGTH_LONG).show();
                        return;
                    }

                    vm.rename(w.id, nf, nb);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /**
     * Перевіряє, чи є вже в цій групі слово з такими ж front/back.
     *
     * @param ignoreId id слова, яке ігноруємо (для rename), для нового слова передаємо -1
     */
    private boolean isDuplicateInGroup(long groupId, long ignoreId, String front, String back) {
        String f = front.trim();
        String b = back.trim();

        for (Word word : currentWords) {
            if (word.groupId == groupId && word.id != ignoreId) {
                if (word.front != null && word.back != null &&
                        word.front.trim().equalsIgnoreCase(f) &&
                        word.back.trim().equalsIgnoreCase(b)) {
                    return true;
                }
            }
        }
        return false;
    }
}
