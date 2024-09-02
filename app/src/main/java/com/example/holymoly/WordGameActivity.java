package com.example.holymoly;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class WordGameActivity extends AppCompatActivity {

    private TextView[][] userTextViews, aiTextViews; // 사용자와 AI의 단어를 표시할 TextView 배열
    private ImageView[][] userImageViews, aiImageViews; // 사용자와 AI의 선택된 상태를 표시할 ImageView 배열
    private TextView userTextView, aiTextView; // 사용자와 AI의 추가적인 정보 표시용 TextView
    private ImageView profile; // 사용자 프로필을 표시할 ImageView
    private Gemini gemini; // Gemini API와 상호작용을 위한 객체
    private String[][] aiWords;  // Gemini가 생성한 AI 단어들을 저장할 2차원 배열
    private static final int SELECTED_IMAGE_RESOURCE = R.drawable.rect2;  // 선택된 이미지를 나타내는 리소스

    private UserInfo userInfo = new UserInfo(); // 사용자 정보를 관리하는 객체

    private boolean[][] userSelected; // 사용자가 선택한 칸을 추적하는 배열
    private boolean[][] aiSelected;   // AI가 선택한 칸을 추적하는 배열
    private boolean userTurn;         // 현재 턴이 사용자인지 AI인지 관리하는 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_game); // 레이아웃 설정
        gemini = new Gemini(); // Gemini API 객체 초기화

        // 사용자 프로필 및 정보 표시용 TextView 초기화
        profile = findViewById(R.id.mini_profile);
        userTextView = findViewById(R.id.userTextView);
        aiTextView = findViewById(R.id.AITextView);

        // 사용자와 AI의 선택 상태를 추적할 배열 초기화
        userSelected = new boolean[3][3];
        aiSelected = new boolean[3][3];
        userTurn = true; // 게임 시작 시 사용자가 먼저 선택

        // 사용자 빙고판의 TextView 및 ImageView 배열 초기화
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

        // AI 빙고판의 TextView 및 ImageView 배열 초기화
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

        // Intent로부터 사용자 단어 배열을 가져옴
        String[] words = getIntent().getStringArrayExtra("words");

        // 단어 배열이 null이거나 크기가 9가 아닌 경우 임의의 단어로 초기화
        if (words == null || words.length != 9) {
            words = new String[]{"사과", "바나나", "포도", "딸기", "수박", "복숭아", "오렌지", "자두", "참외"};
            Toast.makeText(this, "임의의 단어가 설정되었습니다.", Toast.LENGTH_SHORT).show();
        }

        // 사용자의 빙고판에 단어를 설정하고 클릭 이벤트 핸들러를 설정
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                userTextViews[i][j].setText(words[i * 3 + j]);  // 각 TextView에 단어를 설정
                final int row = i;
                final int col = j;
                userTextViews[i][j].setOnClickListener(view -> handleUserClick(row, col)); // 클릭 이벤트 핸들러 설정
            }
        }

        // AI 단어 생성 요청
        generateAIWords("과일");
    }


    private void handleUserClick(int row, int col) {
        if (!userTurn) {
            Toast.makeText(this, "AI의 턴입니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userSelected[row][col]) {
            Toast.makeText(this, "이미 선택된 칸입니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (aiWords == null) {
            Toast.makeText(this, "AI 단어가 아직 생성되지 않았습니다.", Toast.LENGTH_SHORT).show();
            return;
        }


        userImageViews[row][col].setImageResource(SELECTED_IMAGE_RESOURCE); // 사용자가 클릭한 텍스트뷰의 이미지를 변경
        userSelected[row][col] = true;  // 사용자 선택 기록

        String selectedWord = userTextViews[row][col].getText().toString(); // 사용자가 선택한 단어

        // AI의 텍스트뷰에서 단어를 찾아 이미지를 변경
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (aiWords[i][j].equals(selectedWord) && !aiSelected[i][j]) {
                    aiTextViews[i][j].setText(aiWords[i][j]);  // 일치하는 경우 단어를 보여줌
                    aiImageViews[i][j].setImageResource(SELECTED_IMAGE_RESOURCE);  // 일치하는 경우 이미지 변경
                    aiSelected[i][j] = true;  // AI 선택 기록
                }
            }
        }

        userTurn = false; // 턴을 AI로 변경
        new Handler().postDelayed(this::aiTurn, 1000);  // 1초 후 AI의 턴 실행
    }


    private void aiTurn() {
        // 랜덤으로 AI의 단어 선택
        Random random = new Random();
        int row, col;
        do {
            row = random.nextInt(3);
            col = random.nextInt(3);
        } while (aiSelected[row][col]); // 이미 선택된 칸은 다시 선택하지 않음

        // AI의 선택 처리
        aiTextViews[row][col].setText(aiWords[row][col]);  // AI가 선택한 단어를 보여줌
        aiImageViews[row][col].setImageResource(SELECTED_IMAGE_RESOURCE);
        aiSelected[row][col] = true;

        // AI가 선택한 단어에 해당하는 사용자의 텍스트뷰도 선택된 것으로 표시
        String selectedWord = aiWords[row][col];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (userTextViews[i][j].getText().toString().equals(selectedWord) && !userSelected[i][j]) {
                    userImageViews[i][j].setImageResource(SELECTED_IMAGE_RESOURCE);
                    userSelected[i][j] = true;
                }
            }
        }

        userTurn = true; // 턴을 사용자로 변경
    }


    public void generateAIWords(String theme) { // Gemini에 전달할 프롬프트 생성
        String prompt = theme + "를 주제로 빙고게임을 하려고 합니다. 3*3 빙고이므로, 단어 9개를 생성해주세요." +
                "단어와 단어 사이에는 ', '로 띄워주세요.";

        gemini.generateText(prompt, new Gemini.Callback() {
            @Override
            public void onSuccess(String text) {
                runOnUiThread(() -> {
                    // 생성된 단어 문자열을 쉼표와 공백으로 분리하여 배열에 저장
                    String[] aiWordsArray = text.split(",\\s*");
                    if (aiWordsArray.length == 9) {
                        aiWords = new String[3][3];
                        for (int i = 0; i < 3; i++) {
                            for (int j = 0; j < 3; j++) {
                                aiWords[i][j] = aiWordsArray[i * 3 + j];
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

    @Override
    public void onStart() {
        super.onStart();
        loadUserInfo(profile); // 사용자 정보 로드
    }


    public void loadUserInfo(ImageView profile) {
        userInfo.loadUserInfo(profile);
    }

    public void sound() {
        Intent intent = new Intent(this, SoundService.class);
        startService(intent); // SoundService를 시작하여 효과음 재생
    }
}
