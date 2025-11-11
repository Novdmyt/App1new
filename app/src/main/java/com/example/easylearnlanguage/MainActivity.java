package com.example.easylearnlanguage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.easylearnlanguage.settings.SettingsActivity;
import com.example.easylearnlanguage.temp.NewGroupActivity;
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

        // "Нова група" – менеджер груп
        tileNewGroup.setOnClickListener(v ->
                startActivity(new Intent(this, NewGroupActivity.class))
        );

        // "Нові слова" – вибір групи, потім WordsActivity
        tileNewWords.setOnClickListener(v -> {
            Intent it = new Intent(this, NewGroupActivity.class);
            it.putExtra(NewGroupActivity.EXTRA_MODE, NewGroupActivity.MODE_WORDS);
            startActivity(it);
        });

        // "Грати" – вибір групи, після кліку запускається PlayActivity
        tilePlay.setOnClickListener(v -> {
            Intent it = new Intent(this, NewGroupActivity.class);
            it.putExtra(NewGroupActivity.EXTRA_MODE, NewGroupActivity.MODE_PLAY);
            startActivity(it);
        });

        // Заглушка для "Коригувати" (якщо ще не реалізовано)
        View.OnClickListener comingSoon = vv ->
                Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show();
        tileCorrect.setOnClickListener(comingSoon);

        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class))
        );
    }
}
