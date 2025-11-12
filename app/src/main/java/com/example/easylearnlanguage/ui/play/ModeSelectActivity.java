package com.example.easylearnlanguage.ui.play;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.easylearnlanguage.R;
import com.example.easylearnlanguage.temp.NewGroupActivity;
import com.google.android.material.appbar.MaterialToolbar;

public class ModeSelectActivity extends AppCompatActivity {

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_select);

        MaterialToolbar bar = findViewById(R.id.bar);
        bar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // Навчання -> вибір групи -> НОВИЙ екран (MatchActivity)
        findViewById(R.id.btnTraining).setOnClickListener(v -> {
            Intent it = new Intent(this, NewGroupActivity.class);
            it.putExtra(NewGroupActivity.EXTRA_TARGET,
                    com.example.easylearnlanguage.ui.play.MatchActivity.class.getName());
            startActivity(it);
        });

        // Практика -> вибір групи -> PracticeActivity (картки)
        findViewById(R.id.btnPractice).setOnClickListener(v -> {
            Intent it = new Intent(this, NewGroupActivity.class);
            it.putExtra(NewGroupActivity.EXTRA_TARGET,
                    com.example.easylearnlanguage.ui.play.PracticeActivity.class.getName());
            startActivity(it);
        });
    }
}
