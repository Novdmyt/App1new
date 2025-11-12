package com.example.easylearnlanguage.temp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easylearnlanguage.R;
import com.example.easylearnlanguage.data.Group;
import com.example.easylearnlanguage.ui.group.GroupAdapter;
import com.example.easylearnlanguage.ui.group.GroupViewModel;
import com.example.easylearnlanguage.ui.play.MatchActivity;
import com.example.easylearnlanguage.ui.play.PlayActivity;
import com.example.easylearnlanguage.ui.play.PracticeActivity;
import com.example.easylearnlanguage.ui.word.WordsActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class NewGroupActivity extends AppCompatActivity {

    private GroupViewModel vm;
    private GroupAdapter   adapter;

    /** Імʼя цільової Activity (наприклад, "com.example....PlayActivity") */
    public static final String EXTRA_TARGET = "target_activity";
    private String targetClassName = null;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);

        vm = new ViewModelProvider(this).get(GroupViewModel.class);

        // читаємо цільовий екран
        targetClassName = getIntent().getStringExtra(EXTRA_TARGET);

        // Toolbar + заголовок
        MaterialToolbar bar = findViewById(R.id.bar);
        bar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        int title = R.string.title_groups;
        if (PlayActivity.class.getName().equals(targetClassName))          title = R.string.title_play;
        else if (PracticeActivity.class.getName().equals(targetClassName)) title = R.string.practice;
        else if (MatchActivity.class.getName().equals(targetClassName))    title = R.string.training;
        bar.setTitle(title);

        // список груп
        RecyclerView list = findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GroupAdapter(this::onGroupClick);
        list.setAdapter(adapter);

        // long-press меню для картки
        adapter.setOnLongClick((anchor, g, pos) -> showGroupMenu(anchor, g));

        vm.groups().observe(this, adapter::submit);

        // додати групу (діалог із вибором кольору ти вже підключив)
        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> showAddDialog());


    }

    /** Перехід по кліку на групу — на targetClassName (якщо не задано — WordsActivity) */
    private void onGroupClick(Group g) {
        if (g == null) return;

        String cls = targetClassName;
        if (cls == null || cls.isEmpty()) {
            Intent it = new Intent(this, WordsActivity.class);
            it.putExtra(WordsActivity.EXTRA_GROUP_ID, g.id);
            it.putExtra(WordsActivity.EXTRA_GROUP_TITLE, g.title);
            startActivity(it);
            return;
        }

        try {
            Class<?> target = Class.forName(cls);
            Intent it = new Intent(this, target);

            // передаємо дані для всіх режимів
            it.putExtra(PlayActivity.EXTRA_GROUP_ID, g.id);
            it.putExtra(PlayActivity.EXTRA_GROUP_TITLE, g.title);
            it.putExtra(PracticeActivity.EXTRA_GROUP_ID, g.id);
            it.putExtra(PracticeActivity.EXTRA_GROUP_TITLE, g.title);
            it.putExtra(MatchActivity.EXTRA_GROUP_ID, g.id);
            it.putExtra(MatchActivity.EXTRA_GROUP_TITLE, g.title);

            startActivity(it);
        } catch (ClassNotFoundException e) {
            Intent it = new Intent(this, WordsActivity.class);
            it.putExtra(WordsActivity.EXTRA_GROUP_ID, g.id);
            it.putExtra(WordsActivity.EXTRA_GROUP_TITLE, g.title);
            startActivity(it);
        }
    }

    /** Діалог створення групи (з твоїм вибором кольорів-кружків) */
    private void showAddDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_group, null, false);
        EditText etTitle = view.findViewById(R.id.etTitle);
        com.google.android.material.button.MaterialButtonToggleGroup groupColors =
                view.findViewById(R.id.groupColors);

        groupColors.check(R.id.btnColorNone);

        new AlertDialog.Builder(this)
                .setTitle(R.string.title_new_group)
                .setView(view)
                .setPositiveButton(android.R.string.ok, (d, w) -> {
                    String title = etTitle.getText().toString().trim();
                    if (title.isEmpty()) {
                        etTitle.setError(getString(R.string.title_new_group));
                        return;
                    }

                    int pickedColor = 0; // none
                    int checkedId = groupColors.getCheckedButtonId();
                    if (checkedId == R.id.btnColorBlue)    pickedColor = ContextCompat.getColor(this, R.color.blue);
                    else if (checkedId == R.id.btnColorYellow) pickedColor = ContextCompat.getColor(this, R.color.yellow);
                    else if (checkedId == R.id.btnColorRed)    pickedColor = ContextCompat.getColor(this, R.color.red);
                    else if (checkedId == R.id.btnColorGreen)  pickedColor = ContextCompat.getColor(this, R.color.green);
                    else if (checkedId == R.id.btnColorPurple) pickedColor = ContextCompat.getColor(this, R.color.purple);
                    else if (checkedId == R.id.btnColorOrange) pickedColor = ContextCompat.getColor(this, R.color.orange);
                    else if (checkedId == R.id.btnColorTeal)   pickedColor = ContextCompat.getColor(this, R.color.teal);
                    else if (checkedId == R.id.btnColorPink)   pickedColor = ContextCompat.getColor(this, R.color.pink);
                    else if (checkedId == R.id.btnColorGray)   pickedColor = ContextCompat.getColor(this, R.color.gray);

                    vm.add(title, "", "", pickedColor);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /** Контекстне меню картки (довге натискання) */
    private void showGroupMenu(View anchor, Group g){
        PopupMenu pm = new PopupMenu(this, anchor);
        pm.getMenu().add(0, 1, 0, "Перейменувати");
        pm.getMenu().add(0, 2, 1, "Видалити");
        pm.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) { showRenameDialog(g); return true; }
            if (item.getItemId() == 2) { confirmDelete(g);   return true; }
            return false;
        });
        pm.show();
    }

    private void showRenameDialog(Group g){
        final EditText et = new EditText(this);
        et.setSingleLine(true);
        et.setText(g.title);
        et.setSelection(et.getText().length());

        new AlertDialog.Builder(this)
                .setTitle("Перейменувати групу")
                .setView(et)
                .setPositiveButton(android.R.string.ok, (d, w) -> {
                    String t = et.getText().toString().trim();
                    if (!TextUtils.isEmpty(t) && !t.equals(g.title)) {
                        vm.rename(g.id, t);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void confirmDelete(Group g){
        new AlertDialog.Builder(this)
                .setMessage("Видалити групу «" + g.title + "»?")
                .setPositiveButton("Видалити", (d, w) -> {
                    vm.deleteCascade(g);
                    Snackbar.make(findViewById(android.R.id.content),
                                    "Групу видалено", Snackbar.LENGTH_LONG)
                            .setAction("Скасувати",
                                    v -> vm.add(g.title, g.from, g.to, g.color))
                            .show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }



}
