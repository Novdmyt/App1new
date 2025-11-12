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
import com.google.android.material.snackbar.Snackbar;                           // ✅ правильний Snackbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;               // ✅ імпорт для TextInputEditText

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
        adapter.setOnLongClick((anchor, w, pos) -> showWordMenu(anchor, w));
        list.setAdapter(adapter);                                             // ✅ не забудь setAdapter

        vm = new ViewModelProvider(this).get(WordsViewModel.class);
        vm.wordsByGroup(groupId).observe(this, adapter::submit);

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
        pm.getMenu().add(0, 1, 0, getString(R.string.rename));  // додай рядки у strings.xml за бажанням
        pm.getMenu().add(0, 2, 1, getString(R.string.delete));

        pm.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) { showRenameDialog(w); return true; }
            if (item.getItemId() == 2) {
                vm.delete(w);
                Snackbar.make(findViewById(android.R.id.content),          // ✅ Material Snackbar
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
        TextInputEditText etFront = view.findViewById(R.id.etFront);         // ✅ ті самі id
        TextInputEditText etBack  = view.findViewById(R.id.etBack);
        etFront.setText(w.front);
        etBack.setText(w.back);

        new AlertDialog.Builder(this)
                .setTitle(R.string.rename_word)
                .setView(view)
                .setPositiveButton(android.R.string.ok, (d, which) -> {
                    String nf = etFront.getText().toString().trim();
                    String nb = etBack.getText().toString().trim();
                    if (!nf.isEmpty() && !nb.isEmpty()) vm.rename(w.id, nf, nb);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
