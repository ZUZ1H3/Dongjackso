package com.example.holymoly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class WordGameReadyActivity extends AppCompatActivity {

    private EditText[] editTexts;
    private ImageButton nextBtn, AI;
    private TextView AITextView;
    private Gemini gemini;

    /* 효과음 */
    private SharedPreferences pref;
    private boolean isSoundOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_game_ready);
        pref = getSharedPreferences("music", MODE_PRIVATE); // 효과음 초기화

        // EditText 배열 초기화
        editTexts = new EditText[]{
                findViewById(R.id.tv_rect1), findViewById(R.id.tv_rect2),
                findViewById(R.id.tv_rect3), findViewById(R.id.tv_rect4),
                findViewById(R.id.tv_rect5), findViewById(R.id.tv_rect6),
                findViewById(R.id.tv_rect7), findViewById(R.id.tv_rect8),
                findViewById(R.id.tv_rect9), findViewById(R.id.tv_rect10),
                findViewById(R.id.tv_rect11), findViewById(R.id.tv_rect12),
                findViewById(R.id.tv_rect13), findViewById(R.id.tv_rect14),
                findViewById(R.id.tv_rect15), findViewById(R.id.tv_rect16),
        };

        nextBtn = findViewById(R.id.next_btn);
        AI = findViewById(R.id.AI);
        AITextView = findViewById(R.id.AITextView);
        gemini = new Gemini();

        AI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateAIWords("과일");
            }
        });

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

    public void generateAIWords(String theme) {
        StringBuilder existingWords = new StringBuilder();
        for (EditText editText : editTexts) {
            String word = editText.getText().toString().trim();
            if (!word.isEmpty()) {
                existingWords.append(word).append(", ");
            }
        }

        // Remove the last comma and space
        if (existingWords.length() > 0) {
            existingWords.setLength(existingWords.length() - 2);
        }

        // Create prompt for AI
        String prompt = "사용자는 " + theme + "를 주제로 빙고게임을 하려고 합니다. " +
                "그런데 단어가 생각이 안 난다고 합니다. " +
                "다음 단어들을 제외한 주제에 맞는 단어를 랜덤으로 2가지 추천해주세요. " +
                "추천할 단어는 '" + existingWords.toString() + "'을 제외해야 합니다. 단답으로 대답하세요. " +
                "단어와 단어 사이에는 ', '로 띄어주세요.";

        gemini.generateText(prompt, new Gemini.Callback() {
            @Override
            public void onSuccess(String text) {
                runOnUiThread(() ->
                        AITextView.setText("이런 단어는 어때? \n" + text)
                );
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() ->
                        Toast.makeText(WordGameReadyActivity.this, "단어 생성 실패", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }


    // 효과음
    public void sound() {
        isSoundOn = pref.getBoolean("on&off2", true);
        Intent intent = new Intent(this, SoundService.class);
        if (isSoundOn) startService(intent); // 효과음 on
        else stopService(intent);            // 효과음 off
    }
}
