package com.example.easylearnlanguage.ui.word;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easylearnlanguage.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.Arrays;
import java.util.Locale;

public class WordsActivity extends AppCompatActivity {

    public static final String EXTRA_GROUP_ID = "group_id";
    public static final String EXTRA_GROUP_TITLE = "group_title";

    private WordsViewModel vm;
    private long groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_words);

        Intent i = getIntent();
        groupId = i.getLongExtra(EXTRA_GROUP_ID, -1);
        String title = i.getStringExtra(EXTRA_GROUP_TITLE);
        if (groupId <= 0) { finish(); return; }

        MaterialToolbar bar = findViewById(R.id.bar);
        bar.setTitle(title != null ? title : getString(R.string.title_new_words));
        bar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        RecyclerView list = findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(this));
        WordAdapter adapter = new WordAdapter();
        list.setAdapter(adapter);

        vm = new ViewModelProvider(this).get(WordsViewModel.class);
        vm.wordsByGroup(groupId).observe(this, adapter::submit);

        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> showAddWordDialog());
    }

    private void showAddWordDialog() {
        // інфлейтимо діалог
        android.view.View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_word, null, false);

        EditText etFront = view.findViewById(R.id.etFront);
        EditText etBack  = view.findViewById(R.id.etBack);

        MaterialAutoCompleteTextView dropSrc = view.findViewById(R.id.dropSrcLang);
        MaterialAutoCompleteTextView dropDst = view.findViewById(R.id.dropDstLang);

        // ярлики та BCP-47 теги (підлаштуй під свій набір мов)
        String[] labels = new String[] {
                getString(R.string.english),     // "English"
                getString(R.string.ukrainian),   // "Українська"
                getString(R.string.german)       // "Deutsch"
        };
        String[] tags = new String[] { "en", "uk", "de" };

        dropSrc.setSimpleItems(labels);
        dropDst.setSimpleItems(labels);

        // дефолти: слово німецькою, переклад українською
        int srcIdx = 2; // de
        int dstIdx = 1; // uk
        dropSrc.setText(labels[srcIdx], false);
        dropDst.setText(labels[dstIdx], false);

        // застосувати локалі на старті
        applyLocale(etFront, tags[srcIdx]);
        applyLocale(etBack,  tags[dstIdx]);

        // зміна мови у випадайках
        dropSrc.setOnItemClickListener((p, v, pos, id) -> applyLocale(etFront, tags[pos]));
        dropDst.setOnItemClickListener((p, v, pos, id) -> applyLocale(etBack,  tags[pos]));

        // додатково — при фокусі ще раз підказати IME потрібну локаль
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
                    vm.add(groupId, front, back);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /** Підказуємо клавіатурі (IME) й системі бажану мову для поля вводу. */
    private void applyLocale(EditText edit, String bcp47Tag) {
        Locale loc = Locale.forLanguageTag(bcp47Tag);

        // API 24+: повна підтримка LocaleList та imeHintLocales
        if (Build.VERSION.SDK_INT >= 24) {
            android.os.LocaleList ll = new android.os.LocaleList(loc);
            // Вказуємо бажану мову для автокорекції/орфографії
            edit.setTextLocales(ll);
            // Підказуємо клавіатурі потрібну розкладку
            edit.setImeHintLocales(ll);
        } else if (Build.VERSION.SDK_INT >= 21) {
            // API 21–23: доступне тільки setTextLocale(Locale)
            edit.setTextLocale(loc);
        }
        // На ще старших (не актуально при minSdk=23) — нічого не робимо
    }
}
