package com.example.holymoly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashSet;
import java.util.Set;

public class WordGameReadyActivity extends AppCompatActivity {

    private EditText[] editTexts;
    private ImageButton nextBtn, AI;
    private TextView AITextView, themeTextView;
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
        themeTextView = findViewById(R.id.theme);
        gemini = new Gemini();
        AITextView.setMovementMethod(new ScrollingMovementMethod()); //스크롤 가능하도록

        String theme = getIntent().getStringExtra("Theme");

        // 가져온 테마 값을 TextView에 설정
        if (theme != null) {
            themeTextView.setText("제시어 : " + theme);
        }


        AI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateAIWords(theme);
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isAnyFieldEmpty()) {
                    Toast.makeText(WordGameReadyActivity.this, "모든 칸을 채워주세요.", Toast.LENGTH_SHORT).show();
                } else if (checkForDuplicates()) {
                    // 중복 단어가 있을 때 처리
                    Toast.makeText(WordGameReadyActivity.this, "중복된 단어가 있습니다. 수정해 주세요.", Toast.LENGTH_SHORT).show();
                }// 관련된 단어만 있을 경우
                else {
                    StringBuilder wordsBuilder = new StringBuilder();
                    for (EditText editText : editTexts) {
                        String word = editText.getText().toString().trim();
                        if (!word.isEmpty()) {
                            wordsBuilder.append(word).append(", ");
                        }
                    }

                    if (wordsBuilder.length() > 0) {
                        wordsBuilder.setLength(wordsBuilder.length() - 2);
                    }

                    String words = wordsBuilder.toString();
                    Intent intent = new Intent(WordGameReadyActivity.this, WordGameActivity.class);
                    intent.putExtra("words", words.split(", "));
                    intent.putExtra("theme", theme);
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

    private boolean checkForDuplicates() {
        Set<String> uniqueWords = new HashSet<>();
        Set<String> duplicateWords = new HashSet<>();

        for (EditText editText : editTexts) {
            String word = editText.getText().toString().trim();
            if (!word.isEmpty()) {
                // 중복된 단어가 있는지 확인
                if (!uniqueWords.add(word)) {
                    duplicateWords.add(word); // 중복된 단어를 추가
                }
            }
        }

        // 중복된 단어가 있다면
        if (!duplicateWords.isEmpty()) {
            // 중복된 단어를 포함한 메시지 작성
            StringBuilder message = new StringBuilder("");
            for (String word : duplicateWords) {
                message.append(word).append(", ");
            }
            // 마지막 쉼표와 공백 제거
            message.setLength(message.length() - 2);
            message.append("라는 단어가 중복 돼! 다시 작성해볼래?");

            // 메시지를 AITextView에 설정
            AITextView.setText(message.toString());
            return true;
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

        if (existingWords.length() > 0) {
            existingWords.setLength(existingWords.length() - 2);
        }

        //프롬프트
        String prompt = "사용자는 " + theme + "를 주제로 빙고게임을 하려고 합니다. " +
                "그런데 단어가 생각이 안 난다고 합니다. " +
                "주제에 맞는 단어를 2개만 추천해주세요." +
                "단답으로 대답하세요. " +
                "단어와 단어 사이에는 ', '로 띄어주세요. ";

        if (!(existingWords.toString().isEmpty())) {
            prompt += existingWords.toString() + "'는 제외해서 추천해주세요.";
        }

        //AI가 추천하는 단어 생성
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
