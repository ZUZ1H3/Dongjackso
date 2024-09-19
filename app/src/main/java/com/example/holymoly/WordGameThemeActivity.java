package com.example.holymoly;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class WordGameThemeActivity extends AppCompatActivity {

    private TextView fruitBtn, animalBtn, alphabetBtn, numberBtn, marineBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_game_theme);

        // Initialize TextViews
        fruitBtn = findViewById(R.id.fruit);
        animalBtn = findViewById(R.id.animal);
        alphabetBtn = findViewById(R.id.alphabet);
        numberBtn = findViewById(R.id.number);
        marineBtn = findViewById(R.id.marine);

        // Set click listeners
        setClickListener(fruitBtn);
        setClickListener(animalBtn);
        setClickListener(alphabetBtn);
        setClickListener(numberBtn);
        setClickListener(marineBtn);
    }

    private void setClickListener(TextView textView) {
        textView.setOnClickListener(view -> {
            Intent intent = new Intent(WordGameThemeActivity.this, WordGameActivity.class);
            intent.putExtra("Theme", textView.getText().toString());
            startActivity(intent);
        });
    }
}
