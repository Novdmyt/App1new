package com.example.easylearnlanguage.temp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
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
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

public class NewGroupActivity extends AppCompatActivity {

    private GroupViewModel vm;
    private GroupAdapter adapter;

    /** Имя класса целевого экрана (String, например "com.example....PlayActivity") */
    public static final String EXTRA_TARGET = "target_activity";

    private String targetClassName = null;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);

        vm = new ViewModelProvider(this).get(GroupViewModel.class);

        // читаем целевой экран
        targetClassName = getIntent().getStringExtra(EXTRA_TARGET);

        // Toolbar + заголовок по целевому экрану
        MaterialToolbar bar = findViewById(R.id.bar);
        bar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        int title = R.string.title_groups;
        if (PlayActivity.class.getName().equals(targetClassName))          title = R.string.title_play;
        else if (PracticeActivity.class.getName().equals(targetClassName)) title = R.string.practice;
        else if (MatchActivity.class.getName().equals(targetClassName))    title = R.string.training;
        bar.setTitle(title);

        // список групп
        RecyclerView list = findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GroupAdapter(this::onGroupClick);
        list.setAdapter(adapter);
        vm.groups().observe(this, adapter::submit);

        // добавление группы
        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> showAddDialog());

        attachSwipeToDelete(list);
    }

    /** Переход по клику на группу — строго на targetClassName (если не задан — WordsActivity) */
    private void onGroupClick(Group g) {
        if (g == null) return;

        String cls = targetClassName;
        if (cls == null || cls.isEmpty()) {
            // управление словами по умолчанию
            Intent it = new Intent(this, WordsActivity.class);
            it.putExtra(WordsActivity.EXTRA_GROUP_ID, g.id);
            it.putExtra(WordsActivity.EXTRA_GROUP_TITLE, g.title);
            startActivity(it);
            return;
        }

        try {
            Class<?> target = Class.forName(cls);
            Intent it = new Intent(this, target);

            // Передаём все возможные ключи, чтобы целевая Activity точно получила данные
            it.putExtra(PlayActivity.EXTRA_GROUP_ID, g.id);
            it.putExtra(PlayActivity.EXTRA_GROUP_TITLE, g.title);
            it.putExtra(PracticeActivity.EXTRA_GROUP_ID, g.id);
            it.putExtra(PracticeActivity.EXTRA_GROUP_TITLE, g.title);
            it.putExtra(MatchActivity.EXTRA_GROUP_ID, g.id);
            it.putExtra(MatchActivity.EXTRA_GROUP_TITLE, g.title);

            startActivity(it);
        } catch (ClassNotFoundException e) {
            // фолбэк на управление словами
            Intent it = new Intent(this, WordsActivity.class);
            it.putExtra(WordsActivity.EXTRA_GROUP_ID, g.id);
            it.putExtra(WordsActivity.EXTRA_GROUP_TITLE, g.title);
            startActivity(it);
        }
    }

    private void showAddDialog() {
        var view = LayoutInflater.from(this).inflate(R.layout.dialog_add_group, null, false);
        EditText etTitle = view.findViewById(R.id.etTitle);
        com.google.android.material.button.MaterialButtonToggleGroup groupColors =
                view.findViewById(R.id.groupColors);

        // за замовчуванням — "без кольору"
        groupColors.check(R.id.btnColorNone);

        new androidx.appcompat.app.AlertDialog.Builder(this)
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
                    if (checkedId == R.id.btnColorBlue)   pickedColor = getColorCompat(R.color.blue);
                    else if (checkedId == R.id.btnColorYellow) pickedColor = getColorCompat(R.color.yellow);
                    else if (checkedId == R.id.btnColorRed)    pickedColor = getColorCompat(R.color.red);
                    else if (checkedId == R.id.btnColorGreen)  pickedColor = getColorCompat(R.color.green);
                    else if (checkedId == R.id.btnColorPurple)  pickedColor = getColorCompat(R.color.purple);
                    else if (checkedId == R.id.btnColorOrange)  pickedColor = getColorCompat(R.color.orange);
                    else if (checkedId == R.id.btnColorTeal)  pickedColor = getColorCompat(R.color.teal);
                    else if (checkedId == R.id.btnColorPink)  pickedColor = getColorCompat(R.color.pink);
                    else if (checkedId == R.id.btnColorGray)  pickedColor = getColorCompat(R.color.gray);
                    vm.add(title, "", "", pickedColor);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
    private int getColorCompat(int resId) {
        return androidx.core.content.ContextCompat.getColor(this, resId);
    }

    private void attachSwipeToDelete(RecyclerView rv) {
        ItemTouchHelper.SimpleCallback cb = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override public boolean onMove(RecyclerView r, RecyclerView.ViewHolder v, RecyclerView.ViewHolder t){ return false; }
            @Override public void onSwiped(RecyclerView.ViewHolder vh, int dir) {
                int pos = vh.getBindingAdapterPosition();
                Group g = adapter.getItem(pos);
                if (g == null) return;

                adapter.removeAt(pos);
                vm.deleteCascade(g);

                // ИСПРАВЛЕНО: используем реальные поля модели Group: from / to
                Snackbar.make(rv, R.string.group_deleted, Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo, v -> vm.add(g.title, g.from, g.to, g.color))
                        .show();
            }
        };
        new ItemTouchHelper(cb).attachToRecyclerView(rv);
    }
}
