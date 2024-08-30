package com.example.holymoly;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class WordGameActivity extends AppCompatActivity {

    private TextView[][] userTextViews, aiTextViews;
    private ImageView[][] userImageViews, aiImageViews;
    private Gemini gemini;
    private String[][] aiWords;  // Gemini가 생성한 단어들을 저장할 2차원 배열
    private static final int SELECTED_IMAGE_RESOURCE = R.drawable.rect2;  // 선택된 이미지 리소스

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_game);
        gemini = new Gemini();

        // 사용자 TextView 및 ImageView 초기화
        userTextViews = new TextView[][]{
                {findViewById(R.id.tv_rect1), findViewById(R.id.tv_rect2), findViewById(R.id.tv_rect3)},
                {findViewById(R.id.tv_rect4), findViewById(R.id.tv_rect5), findViewById(R.id.tv_rect6)},
                {findViewById(R.id.tv_rect7), findViewById(R.id.tv_rect8), findViewById(R.id.tv_rect9)}
        };

        userImageViews = new ImageView[][]{
                {findViewById(R.id.rect1), findViewById(R.id.rect2), findViewById(R.id.rect3)},
                {findViewById(R.id.rect4), findViewById(R.id.rect5), findViewById(R.id.rect6)},
                {findViewById(R.id.rect7), findViewById(R.id.rect8), findViewById(R.id.rect9)}
        };

        // AI TextView 및 ImageView 초기화
        aiTextViews = new TextView[][]{
                {findViewById(R.id.tv_AIrect1), findViewById(R.id.tv_AIrect2), findViewById(R.id.tv_AIrect3)},
                {findViewById(R.id.tv_AIrect4), findViewById(R.id.tv_AIrect5), findViewById(R.id.tv_AIrect6)},
                {findViewById(R.id.tv_AIrect7), findViewById(R.id.tv_AIrect8), findViewById(R.id.tv_AIrect9)}
        };

        aiImageViews = new ImageView[][]{
                {findViewById(R.id.AIrect1), findViewById(R.id.AIrect2), findViewById(R.id.AIrect3)},
                {findViewById(R.id.AIrect4), findViewById(R.id.AIrect5), findViewById(R.id.AIrect6)},
                {findViewById(R.id.AIrect7), findViewById(R.id.AIrect8), findViewById(R.id.AIrect9)}
        };

        // Intent로부터 단어 배열을 가져옴
        String[] words = getIntent().getStringArrayExtra("words");

        if (words != null && words.length == 9) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    userTextViews[i][j].setText(words[i * 3 + j]);  // 각 TextView에 단어를 설정
                    final int row = i;
                    final int col = j;
                    userTextViews[i][j].setOnClickListener(view -> handleUserClick(row, col));
                }
            }
        } else {
            Toast.makeText(this, "사용자 단어가 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
        }

        // AI 단어 생성 요청
        generateAIWords("과일");
    }

    private void handleUserClick(int row, int col) {
        // 사용자가 클릭한 텍스트뷰의 이미지를 변경
        userImageViews[row][col].setImageResource(SELECTED_IMAGE_RESOURCE);

        // 사용자가 선택한 단어
        String selectedWord = userTextViews[row][col].getText().toString();

        // AI의 텍스트뷰에서 단어를 찾아 이미지를 변경
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (aiWords[i][j].equals(selectedWord)) {
                    aiImageViews[i][j].setImageResource(SELECTED_IMAGE_RESOURCE);
                }
            }
        }
    }

    public void generateAIWords(String theme) {
        String prompt = theme + "를 주제로 빙고게임을 하려고 합니다. 3*3 빙고이므로, 단어 9개를 생성해주세요." +
                "단어와 단어 사이에는 ', '로 띄워주세요.";

        gemini.generateText(prompt, new Gemini.Callback() {
            @Override
            public void onSuccess(String text) {
                runOnUiThread(() -> {
                    String[] aiWordsArray = text.split(",\\s*");  // 쉼표와 공백으로 단어들을 분리하여 배열에 저장
                    if (aiWordsArray.length == 9) {
                        aiWords = new String[3][3];
                        for (int i = 0; i < 3; i++) {
                            for (int j = 0; j < 3; j++) {
                                aiWords[i][j] = aiWordsArray[i * 3 + j];
                                aiTextViews[i][j].setText(aiWords[i][j]);  // 각 TextView에 단어를 설정
                            }
                        }
                    } else {
                        Toast.makeText(WordGameActivity.this, "생성된 단어가 충분하지 않습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() ->
                        Toast.makeText(WordGameActivity.this, "단어 생성 실패", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
}
