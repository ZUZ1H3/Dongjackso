package com.example.holymoly;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MakeStoryAloneActivity extends AppCompatActivity {

    ImageView bookmark_AI, bookmark_Mic, bookmark_OK, Mic;
    ImageButton before, next, stop;
    TextView chapter, story_txt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_make_story_alone);

        bookmark_AI = findViewById(R.id.iv_bookmark_ai);
        bookmark_Mic = findViewById(R.id.iv_bookmark_mic);
        bookmark_OK = findViewById(R.id.iv_bookmark_ok);
        Mic = findViewById(R.id.iv_alone_mic);
        before = findViewById(R.id.ib_before);
        next = findViewById(R.id.ib_next);
        chapter = findViewById(R.id.tv_chapter);
        story_txt = findViewById(R.id.tv_story_txt);

        bookmark_AI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound();

            }
        });

        bookmark_Mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound();
            }
        });

        bookmark_OK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound();
            }
        });
    }

    // 효과음
    public void sound() {
        Intent intent = new Intent(this, SoundService.class);
        startService(intent);
    }
}