package com.example.holymoly;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class WordGameReadyActivity extends AppCompatActivity {

    private EditText[] editTexts;
    private ImageButton nextBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_game_ready);

        // EditText 배열 초기화
        editTexts = new EditText[]{
                findViewById(R.id.tv_rect1), findViewById(R.id.tv_rect2),
                findViewById(R.id.tv_rect3), findViewById(R.id.tv_rect4),
                findViewById(R.id.tv_rect5), findViewById(R.id.tv_rect6),
                findViewById(R.id.tv_rect7), findViewById(R.id.tv_rect8),
                findViewById(R.id.tv_rect9)
        };

        nextBtn = findViewById(R.id.next_btn);

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isAnyFieldEmpty()) {
                    Toast.makeText(WordGameReadyActivity.this, "모든 칸을 채워주세요.", Toast.LENGTH_SHORT).show();
                } else {
                    String[] words = new String[editTexts.length];// EditText에 입력된 단어들을 배열에 담기
                    for (int i = 0; i < editTexts.length; i++) {
                        words[i] = editTexts[i].getText().toString().trim();
                    }

                    // Intent를 사용하여 WordGameActivity로 넘어가기
                    Intent intent = new Intent(WordGameReadyActivity.this, WordGameActivity.class);
                    intent.putExtra("words", words);
                    startActivity(intent);
                }
            }
        });
    }

    // 하나라도 비어있는 EditText가 있는지 확인하는 메소드
    private boolean isAnyFieldEmpty() {
        for (EditText editText : editTexts) {
            if (editText.getText().toString().trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
