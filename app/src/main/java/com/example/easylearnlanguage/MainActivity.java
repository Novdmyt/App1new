package com.example.easylearnlanguage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.easylearnlanguage.settings.SettingsActivity;
import com.example.easylearnlanguage.temp.NewGroupActivity;
import com.example.easylearnlanguage.ui.play.ModeSelectActivity;
import com.example.easylearnlanguage.ui.word.WordsActivity;
import com.google.android.material.card.MaterialCardView;

public class MainActivity extends AppCompatActivity {

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialCardView tileNewGroup  = findViewById(R.id.tile_new_group);
        MaterialCardView tilePlay      = findViewById(R.id.tile_play);
        MaterialCardView tileNewWords  = findViewById(R.id.tile_new_words);
        MaterialCardView tileCorrect   = findViewById(R.id.tile_correct);
        ImageButton      btnSettings   = findViewById(R.id.btn_settings);

        // "Нова група" – менеджер груп (без таргету)
        tileNewGroup.setOnClickListener(v ->
                startActivity(new Intent(this, NewGroupActivity.class))
        );

        // "Нові слова" – вибір групи, потім WordsActivity
        tileNewWords.setOnClickListener(v -> {
            Intent it = new Intent(this, NewGroupActivity.class);
            it.putExtra(NewGroupActivity.EXTRA_TARGET, WordsActivity.class.getName());
            startActivity(it);
        });

        // "Грати" – екран вибору режиму (Практика / Навчання)
        tilePlay.setOnClickListener(v ->
                startActivity(new Intent(this, ModeSelectActivity.class))
        );

        // Поки що заглушка
        View.OnClickListener comingSoon = vv ->
                Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show();
        tileCorrect.setOnClickListener(comingSoon);

        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class))
        );
    }
}
