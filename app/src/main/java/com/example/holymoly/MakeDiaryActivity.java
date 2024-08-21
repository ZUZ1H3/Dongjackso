package com.example.holymoly;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MakeDiaryActivity extends AppCompatActivity {
    private String story, name;
    private Gemini gemini = new Gemini(); // Gemini 인스턴스 생성
    private TextView storyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_diary);

        // Intent에서 story 값을 받아오기
        Intent intent = getIntent();
        story = intent.getStringExtra("story");
        name = intent.getStringExtra("name");

        // story 값을 사용하여 동화 제작 로직을 구현
        storyTextView = findViewById(R.id.storyTextView);
        storyTextView.setMovementMethod(new ScrollingMovementMethod());

        if (story != null) {
            // Gemini를 사용하여 동화 생성
            generateFairyTale(story);
        } else {
            // 스토리가 없는 경우 처리
            storyTextView.setText("스토리를 받을 수 없습니다.");
        }
    }

    private void generateFairyTale(String story) {
        // 동화 생성 프롬프트 구성
        String prompt = "어린이가 AI와 오늘 있었던 일을 주제로 대화한 내역을 제공해드리겠습니다." +
                "이 대화를 바탕으로, 이야기 형태로 다듬어 일기 동화를 만들어주세요." +
                "이야기는 대화 내역을 바탕으로 현실적인 내용이지만, 약간의 과장을 해도 좋습니다." +
                "판타지 등 추가적인 요소를 과하지 않게 섞는 것도 좋습니다." +
                "주인공은 반드시 어린이로 설정해주세요. 어린이의 이름은 '" + name +"' 입니다.\n" +
                "다음은 대화 내역입니다. :" + story;

        gemini.generateText(prompt, new Gemini.Callback() {
            @Override
            public void onSuccess(String text) {
                runOnUiThread(() -> {
                    // 동화 생성 결과를 TextView에 설정
                    storyTextView.setText(text);
                });
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() -> {
                    // 동화 생성 실패 시 메시지 표시
                    storyTextView.setText("동화 생성에 실패했습니다.");
                });
                Log.e("MakeDiaryActivity", "동화 생성 실패", t);
            }
        });
    }
}
