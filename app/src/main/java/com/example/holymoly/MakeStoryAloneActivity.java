package com.example.holymoly;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.flexbox.FlexboxLayout;

public class MakeStoryAloneActivity extends AppCompatActivity {

    ImageView bookmark_AI, bookmark_Mic, bookmark_OK, bookmark_write, Mic, alertIc, scriptBg, touch;
    ImageButton before, next, stop, again, create;
    TextView howabout, alertTxt, scriptTxt;
    EditText story_txt;
    FlexboxLayout keywordsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_make_story_alone);

        bookmark_write = findViewById(R.id.iv_bookmark_write);
        bookmark_AI = findViewById(R.id.iv_bookmark_ai);
        bookmark_Mic = findViewById(R.id.iv_bookmark_mic);
        bookmark_OK = findViewById(R.id.iv_bookmark_ok);
        Mic = findViewById(R.id.iv_alone_mic);
        before = findViewById(R.id.ib_before);
        next = findViewById(R.id.ib_next);
        stop = findViewById(R.id.ib_stop);
        again = findViewById(R.id.ib_again);
        create = findViewById(R.id.ib_create);
        story_txt = findViewById(R.id.et_story_txt);
        howabout = findViewById(R.id.tv_howabout);
        alertIc = findViewById(R.id.iv_alert);
        alertTxt = findViewById(R.id.tv_alert);
        scriptBg = findViewById(R.id.iv_create_back);
        scriptTxt = findViewById(R.id.tv_create_txt);
        keywordsLayout = findViewById(R.id.fl_keywords);
        touch = findViewById(R.id.iv_touch);

        bookmark_write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound();
                touch.setVisibility(View.VISIBLE);
                story_txt.setVisibility(View.VISIBLE);
            }
        });

        bookmark_AI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound();
                howabout.setVisibility(View.VISIBLE);
                alertIc.setVisibility(View.VISIBLE);
                alertTxt.setVisibility(View.VISIBLE);
                howabout.setVisibility(View.VISIBLE);
                again.setVisibility(View.VISIBLE);
                create.setVisibility(View.VISIBLE);
                keywordsLayout.setVisibility(View.VISIBLE);
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

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound();
                scriptBg.setVisibility(View.VISIBLE);
                scriptTxt.setVisibility(View.VISIBLE);
            }
        });

    }

    // 효과음
    public void sound() {
        Intent intent = new Intent(this, SoundService.class);
        startService(intent);
    }
}