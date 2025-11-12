package com.example.easylearnlanguage.ui.play;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.easylearnlanguage.R;
import com.example.easylearnlanguage.temp.NewGroupActivity;
import com.google.android.material.appbar.MaterialToolbar;

public class ModeSelectActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_mode_select);

        MaterialToolbar bar = findViewById(R.id.bar);
        bar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // ИСПРАВЛЕНО: новый протокол – передаём имя класса через EXTRA_TARGET
        findViewById(R.id.btnTraining).setOnClickListener(v -> {
            Intent it = new Intent(this, NewGroupActivity.class);
            it.putExtra(NewGroupActivity.EXTRA_TARGET, MatchActivity.class.getName());   // Matching
            startActivity(it);
        });

        findViewById(R.id.btnPractice).setOnClickListener(v -> {
            Intent it = new Intent(this, NewGroupActivity.class);
            it.putExtra(NewGroupActivity.EXTRA_TARGET, PracticeActivity.class.getName()); // Practice
            startActivity(it);
        });
    }
}
