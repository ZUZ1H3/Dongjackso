package com.example.holymoly;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MakeDiaryActivity extends AppCompatActivity {
    private String story, name, generateStory;
    private ImageButton stopMakingBtn;
    private Gemini gemini = new Gemini();
    private TextView storyTextView, day;
    private long backPressedTime = 0;
    private ImageView backgroundimageview;

    private boolean isGenerated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_diary);
        Intent intent = getIntent();
        story = intent.getStringExtra("story");
        name = intent.getStringExtra("name");

        storyTextView = findViewById(R.id.storyTextView);
        storyTextView.setMovementMethod(new ScrollingMovementMethod());
        stopMakingBtn = findViewById(R.id.ib_stopMaking);
        day = findViewById(R.id.dayTextView);
        backgroundimageview= findViewById(R.id.background_image_view);

        String currentDate = getCurrentDate();

        day.setText(currentDate);

        if (story != null) {
            if(!isGenerated) {
                generateFairyTale(story);
            }
            else{
                storyTextView.setText(generateStory);
            }
        } else {
            storyTextView.setText("스토리가 없습니다.");
        }

        stopMakingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound();
                if (System.currentTimeMillis() - backPressedTime >= 2000) {
                    backPressedTime = System.currentTimeMillis();
                    Toast.makeText(MakeDiaryActivity.this, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
                } else {
                    finish();
                }
            }
        });
        backgroundimageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound();
                Intent intent = new Intent(MakeDiaryActivity.this, MakeDrowDiaryActivity.class);
                startActivity(intent);
            }
        });

    }

    private void generateFairyTale(String story) {
        String prompt = "어린이가 AI와 오늘 있었던 일을 주제로 대화한 내역을 제공해드리겠습니다." +
                "이 대화를 바탕으로, 이야기 형태로 다듬어 일기 동화를 만들어주세요." +
                "이야기는 대화 내역을 바탕으로 현실적인 내용이지만, 약간의 과장을 해도 좋습니다." +
                "판타지 등 추가적인 요소를 과하지 않게 섞는 것도 좋습니다." +
                "제목은 만들지 마세요. 한 문장이 끝나면 '\n'를 넣어 엔터치세요." +
                "주인공은 반드시 어린이로 설정해주세요. 어린이의 이름은 '" + name + "' 입니다.\n" +
                "다음은 대화 내역입니다. :" + story;

        gemini.generateText(prompt, new Gemini.Callback() {
            @Override
            public void onSuccess(String text) {
                runOnUiThread(() -> {
                    storyTextView.setText(text);
                    isGenerated = true;
                    generateStory = text;
                });
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() -> {
                    storyTextView.setText("동화 생성에 실패했습니다.");
                });
                Log.e("MakeDiaryActivity", "동화 생성 실패", t);
            }
        });
    }

    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("M월 d일 E", Locale.KOREAN);
        Date date = new Date();
        return dateFormat.format(date);
    }

    // 효과음
    public void sound() {
        Intent intent = new Intent(this, SoundService.class);
        startService(intent);
    }
}