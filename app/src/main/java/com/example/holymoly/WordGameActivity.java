package com.example.holymoly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class WordGameActivity extends AppCompatActivity {

    private TextView[][] userTextViews, aiTextViews; // 사용자와 AI의 단어를 표시할 TextView 배열
    private ImageView[][] userImageViews, aiImageViews; // 사용자와 AI의 선택된 상태를 표시할 ImageView 배열
    private TextView userTextView, aiTextView; // 사용자와 AI의 추가적인 정보 표시용 TextView
    private ImageView profile, aiBingo, userBingo; // 사용자 프로필을 표시할 ImageView
    private Gemini gemini; // Gemini API와 상호작용을 위한 객체
    private String[][] aiWords;  // Gemini가 생성한 AI 단어들을 저장할 2차원 배열
    private static final int SELECTED_IMAGE_RESOURCE = R.drawable.rect2;  // 선택된 이미지를 나타내는 리소스
    private static final int BINGO_IMAGE_RESOURCE = R.drawable.rect3; // 빙고일 때 사용할 보라색 이미지 리소스

    private UserInfo userInfo = new UserInfo(); // 사용자 정보를 관리하는 객체
    private TextView name;

    private boolean[][] userSelected; // 사용자가 선택한 칸을 추적하는 배열
    private boolean[][] aiSelected;   // AI가 선택한 칸을 추적하는 배열
    private boolean userTurn;         // 현재 턴이 사용자인지 AI인지 관리하는 변수
    // 각 행, 열, 대각선의 빙고 상태를 추적하는 변수들
    private boolean[] userRowBingo = new boolean[4];
    private boolean[] userColBingo = new boolean[4];
    private boolean[] userDiagBingo = new boolean[2];

    private boolean[] aiRowBingo = new boolean[4];
    private boolean[] aiColBingo = new boolean[4];
    private boolean[] aiDiagBingo = new boolean[2];

    private int userBingoCount = 0;  // 사용자 빙고 카운트
    private int aiBingoCount = 0;    // AI 빙고 카운트

    /* DB */
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private int userWinCount = 0;

    /* 효과음 */
    private SharedPreferences pref;
    private boolean isSoundOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_game); // 레이아웃 설정
        pref = getSharedPreferences("music", MODE_PRIVATE); // 효과음 초기화
        gemini = new Gemini(); // Gemini API 객체 초기화

        // 사용자 프로필 및 정보 표시용 TextView 초기화
        profile = findViewById(R.id.mini_profile);
        name = findViewById(R.id.mini_name);  // 사용자 이름
        userTextView = findViewById(R.id.userTextView);
        aiTextView = findViewById(R.id.AITextView);
        aiBingo = findViewById(R.id.AIbingo);
        userBingo = findViewById(R.id.userbingo);
        // 사용자와 AI의 선택 상태를 추적할 배열 초기화
        userSelected = new boolean[4][4];
        aiSelected = new boolean[4][4];
        userTurn = true; // 게임 시작 시 사용자가 먼저 선택

        // 사용자 빙고판의 TextView 및 ImageView 배열 초기화
        userTextViews = new TextView[][]{
                {findViewById(R.id.tv_rect1), findViewById(R.id.tv_rect2), findViewById(R.id.tv_rect3), findViewById(R.id.tv_rect4)},
                {findViewById(R.id.tv_rect5), findViewById(R.id.tv_rect6), findViewById(R.id.tv_rect7), findViewById(R.id.tv_rect8)},
                {findViewById(R.id.tv_rect9), findViewById(R.id.tv_rect10), findViewById(R.id.tv_rect11), findViewById(R.id.tv_rect12)},
                {findViewById(R.id.tv_rect13), findViewById(R.id.tv_rect14), findViewById(R.id.tv_rect15), findViewById(R.id.tv_rect16)}
        };

        userImageViews = new ImageView[][]{
                {findViewById(R.id.rect1), findViewById(R.id.rect2), findViewById(R.id.rect3), findViewById(R.id.rect4)},
                {findViewById(R.id.rect5), findViewById(R.id.rect6), findViewById(R.id.rect7), findViewById(R.id.rect8)},
                {findViewById(R.id.rect9), findViewById(R.id.rect10), findViewById(R.id.rect11), findViewById(R.id.rect12)},
                {findViewById(R.id.rect13), findViewById(R.id.rect14), findViewById(R.id.rect15), findViewById(R.id.rect16)}
        };

        // AI 빙고판의 TextView 및 ImageView 배열 초기화
        aiTextViews = new TextView[][]{
                {findViewById(R.id.tv_AIrect1), findViewById(R.id.tv_AIrect2), findViewById(R.id.tv_AIrect3), findViewById(R.id.tv_AIrect4)},
                {findViewById(R.id.tv_AIrect5), findViewById(R.id.tv_AIrect6), findViewById(R.id.tv_AIrect7), findViewById(R.id.tv_AIrect8)},
                {findViewById(R.id.tv_AIrect9), findViewById(R.id.tv_AIrect10), findViewById(R.id.tv_AIrect11), findViewById(R.id.tv_AIrect12)},
                {findViewById(R.id.tv_AIrect13), findViewById(R.id.tv_AIrect14), findViewById(R.id.tv_AIrect15), findViewById(R.id.tv_AIrect16)}
        };

        aiImageViews = new ImageView[][]{
                {findViewById(R.id.AIrect1), findViewById(R.id.AIrect2), findViewById(R.id.AIrect3), findViewById(R.id.AIrect4)},
                {findViewById(R.id.AIrect5), findViewById(R.id.AIrect6), findViewById(R.id.AIrect7), findViewById(R.id.AIrect8)},
                {findViewById(R.id.AIrect9), findViewById(R.id.AIrect10), findViewById(R.id.AIrect11), findViewById(R.id.AIrect12)},
                {findViewById(R.id.AIrect13), findViewById(R.id.AIrect14), findViewById(R.id.AIrect15), findViewById(R.id.AIrect16)}
        };


        // Intent로부터 사용자 단어 배열을 가져옴
        String[] words = getIntent().getStringArrayExtra("words");

        // 단어 배열이 null이거나 크기가 9가 아닌 경우 임의의 단어로 초기화
        if (words == null || words.length != 16) {
            words = new String[]{"사과", "바나나", "포도", "딸기", "수박", "복숭아", "오렌지", "자두", "참외", "키위", "멜론", "망고", "블루베리", "라즈베리", "체리", "파인애플"};
            Toast.makeText(this, "임의의 단어가 설정되었습니다.", Toast.LENGTH_SHORT).show();
        }

        // 사용자의 빙고판에 단어를 설정하고 클릭 이벤트 핸들러를 설정
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                userTextViews[i][j].setText(words[i * 4 + j]);  // 각 TextView에 단어를 설정
                final int row = i;
                final int col = j;
                userTextViews[i][j].setOnClickListener(view -> handleUserClick(row, col)); // 클릭 이벤트 핸들러 설정
            }
        }

        // AI 단어 생성 요청
        generateAIWords("과일");
    }


    private void handleUserClick(int row, int col) {
        sound();
        userBingo.setVisibility(View.INVISIBLE);
        aiBingo.setVisibility(View.INVISIBLE);
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
        userTextView.setText(selectedWord + "!"); //대화창


        // AI의 텍스트뷰에서 단어를 찾아 이미지를 변경
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (aiWords[i][j].equals(selectedWord) && !aiSelected[i][j]) {
                    aiTextViews[i][j].setText(aiWords[i][j]);  // 일치하는 경우 단어를 보여줌
                    aiImageViews[i][j].setImageResource(SELECTED_IMAGE_RESOURCE);  // 일치하는 경우 이미지 변경
                    aiSelected[i][j] = true;  // AI 선택 기록
                }
            }
        }

        checkUserBingo();
        checkAIBingo();

        userTurn = false; // 턴을 AI로 변경
        new Handler().postDelayed(this::aiTurn, 3000);  // 2.5초 후 AI의 턴 실행
    }


    private void aiTurn() {
        sound();
        userBingo.setVisibility(View.INVISIBLE);
        aiBingo.setVisibility(View.INVISIBLE);
        userTextView.setText(""); //대화창

        // 랜덤으로 AI의 단어 선택
        Random random = new Random();
        int row, col;
        do {
            row = random.nextInt(4);
            col = random.nextInt(4);
        } while (aiSelected[row][col]); // 이미 선택된 칸은 다시 선택하지 않음

        // AI의 선택 처리
        aiTextViews[row][col].setText(aiWords[row][col]);  // AI가 선택한 단어를 보여줌
        aiImageViews[row][col].setImageResource(SELECTED_IMAGE_RESOURCE);
        aiSelected[row][col] = true;

        // AI가 선택한 단어에 해당하는 사용자의 텍스트뷰도 선택된 것으로 표시
        String selectedWord = aiWords[row][col];
        aiTextView.setText(selectedWord +"!");

        // Handler를 사용하여 지연 작업을 설정
        new Handler().postDelayed(() -> {
            // 지정된 시간(durationMillis) 후에 메시지를 비움
            aiTextView.setText("");
            userTextView.setText("내 차례!");
            userBingo.setVisibility(View.INVISIBLE);
            aiBingo.setVisibility(View.INVISIBLE);
        }, 1500);

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (userTextViews[i][j].getText().toString().equals(selectedWord) && !userSelected[i][j]) {
                    userImageViews[i][j].setImageResource(SELECTED_IMAGE_RESOURCE);
                    userSelected[i][j] = true;
                }
            }
        }
        checkUserBingo();
        checkAIBingo();

        userTurn = true; // 턴을 사용자로 변경

    }

    private void checkUserBingo() {
        // 행, 열, 대각선 체크
        for (int i = 0; i < 4; i++) {
            // 행 체크
            if (!userRowBingo[i] && checkLine(userSelected[i])) {
                highlightLine(userImageViews[i]);
                userRowBingo[i] = true;
                userBingoCount++;  // 빙고 카운트 증가
                userBingo.setVisibility(View.VISIBLE);
                userTextView.setText("");
            }
            // 열 체크
            boolean[] colSelected = new boolean[4];
            for (int j = 0; j < 4; j++) {
                colSelected[j] = userSelected[j][i];
            }
            if (!userColBingo[i] && checkLine(colSelected)) {
                highlightLine(new ImageView[]{userImageViews[0][i], userImageViews[1][i], userImageViews[2][i], userImageViews[3][i]});
                userColBingo[i] = true;
                userBingoCount++;  // 빙고 카운트 증가
                userBingo.setVisibility(View.VISIBLE);
                userTextView.setText("");
            }
        }

        // 대각선 체크
        boolean[] diag1Selected = new boolean[4];
        boolean[] diag2Selected = new boolean[4];
        for (int i = 0; i < 4; i++) {
            diag1Selected[i] = userSelected[i][i];
            diag2Selected[i] = userSelected[i][3 - i];
        }
        if (!userDiagBingo[0] && checkLine(diag1Selected)) {
            highlightLine(new ImageView[]{userImageViews[0][0], userImageViews[1][1], userImageViews[2][2], userImageViews[3][3]});
            userDiagBingo[0] = true;
            userBingoCount++;  // 빙고 카운트 증가
            userBingo.setVisibility(View.VISIBLE);
            userTextView.setText("");
        }
        if (!userDiagBingo[1] && checkLine(diag2Selected)) {
            highlightLine(new ImageView[]{userImageViews[0][3], userImageViews[1][2], userImageViews[2][1], userImageViews[3][0]});
            userDiagBingo[1] = true;
            userBingoCount++;  // 빙고 카운트 증가
            userBingo.setVisibility(View.VISIBLE);
            userTextView.setText("");
        }
        int bingoCount = 0;
        // 빙고 개수를 카운트
        for (boolean bingo : userRowBingo) {
            if (bingo) bingoCount++;
        }
        for (boolean bingo : userColBingo) {
            if (bingo) bingoCount++;
        }
        for (boolean bingo : userDiagBingo) {
            if (bingo) bingoCount++;
        }

        // 빙고가 3개 이상일 때 게임 종료
        if (bingoCount >= 3) {
            endGame(name.getText().toString());
        }
    }

    private void checkAIBingo() {
        // 행, 열, 대각선 체크
        for (int i = 0; i < 4; i++) {
            // 행 체크
            if (!aiRowBingo[i] && checkLine(aiSelected[i])) {
                highlightLine(aiImageViews[i]);
                aiRowBingo[i] = true;
                aiBingoCount++;  // 빙고 카운트 증가
                aiBingo.setVisibility(View.VISIBLE);
                aiTextView.setText("");
            }
            // 열 체크
            boolean[] colSelected = new boolean[4];
            for (int j = 0; j < 4; j++) {
                colSelected[j] = aiSelected[j][i];
            }
            if (!aiColBingo[i] && checkLine(colSelected)) {
                highlightLine(new ImageView[]{aiImageViews[0][i], aiImageViews[1][i], aiImageViews[2][i], aiImageViews[3][i]});
                aiColBingo[i] = true;
                aiBingoCount++;  // 빙고 카운트 증가
                aiBingo.setVisibility(View.VISIBLE);
                aiTextView.setText("");
            }
        }

        // 대각선 체크
        boolean[] diag1Selected = new boolean[4];
        boolean[] diag2Selected = new boolean[4];
        for (int i = 0; i < 4; i++) {
            diag1Selected[i] = aiSelected[i][i];
            diag2Selected[i] = aiSelected[i][3 - i];
        }
        if (!aiDiagBingo[0] && checkLine(diag1Selected)) {
            highlightLine(new ImageView[]{aiImageViews[0][0], aiImageViews[1][1], aiImageViews[2][2], aiImageViews[3][3]});
            aiDiagBingo[0] = true;
            aiBingoCount++;  // 빙고 카운트 증가
            aiBingo.setVisibility(View.VISIBLE);
            aiTextView.setText("");
        }
        if (!aiDiagBingo[1] && checkLine(diag2Selected)) {
            highlightLine(new ImageView[]{aiImageViews[0][3], aiImageViews[1][2], aiImageViews[2][1], aiImageViews[3][0]});
            aiDiagBingo[1] = true;
            aiBingoCount++;  // 빙고 카운트 증가
            aiBingo.setVisibility(View.VISIBLE);
            aiTextView.setText("");
        }
        int bingoCount = 0;

        for (boolean bingo : aiRowBingo) {
            if (bingo) bingoCount++;
        }
        for (boolean bingo : aiColBingo) {
            if (bingo) bingoCount++;
        }
        for (boolean bingo : aiDiagBingo) {
            if (bingo) bingoCount++;
        }

        // 빙고가 3개 이상일 때 게임 종료
        if (bingoCount >= 3) {
            endGame("AI");
        }
    }


    public void endGame(String winner) {
        // AlertDialog 생성
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("게임 종료");
        builder.setMessage(winner + "가 이겼습니다!"); // 승자를 표시

        // 확인 버튼 클릭 시 동작 설정
        builder.setPositiveButton("확인", (dialog, which) -> {
            if(winner.equals(name.getText().toString())) countWin();
            finish();
        });

        // 다이얼로그 보여주기
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private boolean checkLine(boolean[] line) {
        for (boolean cell : line) {
            if (!cell) {
                return false;
            }
        }
        return true;
    }

    private void highlightLine(ImageView[] line) {
        for (ImageView img : line) {
            img.setImageResource(BINGO_IMAGE_RESOURCE);
        }

    }

    public void generateAIWords(String theme) { // Gemini에 전달할 프롬프트 생성
        String prompt = theme + "를 주제로 빙고게임을 하려고 합니다. 4*4 빙고이므로, 단어 16개를 생성해주세요." +
                "단어와 단어 사이에는 ', '로 띄워주세요.";

        gemini.generateText(prompt, new Gemini.Callback() {
            @Override
            public void onSuccess(String text) {
                runOnUiThread(() -> {
                    // 생성된 단어 문자열을 쉼표와 공백으로 분리하여 배열에 저장
                    String[] aiWordsArray = text.split(", ");

                    if (aiWordsArray.length == 16) {
                        aiWords = new String[4][4];
                        for (int i = 0; i < 4; i++) {
                            for (int j = 0; j < 4; j++) {
                                aiWords[i][j] = aiWordsArray[i * 4 + j].replace("\n", "").trim();
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
        loadUserInfo(name, profile); // 사용자 정보 로드
    }

    private void countWin() {
        Map<String, Object> winData = new HashMap<>();

        db.collection("bingo").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // 데이터가 존재하면 이긴 횟수 업데이트
                        userWinCount = documentSnapshot.getLong("win") != null ? documentSnapshot.getLong("win").intValue() : 0;
                        userWinCount++;
                        db.collection("bingo").document(user.getUid()).update("win", userWinCount);
                    } else {
                        // 데이터가 없으면 새로 추가
                        userWinCount = 1;
                        winData.put("win", userWinCount);
                        db.collection("bingo").document(user.getUid()).set(winData);
                    }
                });
    }

    public void loadUserInfo(TextView name, ImageView profile) {
        userInfo.loadUserInfo(name, profile);
    }

    // 효과음
    public void sound() {
        isSoundOn = pref.getBoolean("on&off2", true);
        Intent intent = new Intent(this, SoundService.class);
        if (isSoundOn) startService(intent); // 효과음 on
        else stopService(intent);            // 효과음 off
    }
}