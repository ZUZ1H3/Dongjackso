package com.example.holymoly;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class Makepage1Activity extends AppCompatActivity {
    private TextView storyTextView; // TextView를 멤버 변수로 선언
    private MakeStory makeStory;
    private String theme; // 테마를 멤버 변수로 선언
    private String characters; // 캐릭터 목록을 문자열로 선언

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_makepage1);

        // UI 요소 초기화
        storyTextView = findViewById(R.id.tv_pageText1);

        // Intent로부터 데이터 가져오기
        Intent intent = getIntent();
        theme = intent.getStringExtra("selectedTheme");
        ArrayList<String> selectedCharacters = intent.getStringArrayListExtra("selectedCharacters");

        // 선택된 캐릭터 목록을 문자열로 변환
        characters = String.join(", ", selectedCharacters);

        // 선택된 캐릭터 목록을 UI에 표시
        //storyTextView.setText("선택한 테마: " + theme + "\n 등장인물: " + selectedCharacters.toString());

        // 동화 생성 시작
        generateStory();
    }

    private void generateStory() {
        // MakeStory 인스턴스 생성
        makeStory = new MakeStory(this, theme, characters);

        // 동화 도입부 생성 시작
        makeStory.generateInitialStory();
    }

    public void updateStoryTextView(final String newText) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 현재 동화 내용을 업데이트
                String existingText = storyTextView.getText().toString();
                storyTextView.setText(existingText + " " + newText);
            }
        });
    }
}
