package com.example.holymoly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class MaketitleActivity extends AppCompatActivity {
    private ImageView backgroundImageView;
    private ImageButton nextBtn;
    private EditText title;
    private static final int MAX_RETRY_COUNT = 3; // 최대 재시도 횟수
    private int retryCount = 0; // 현재 재시도 횟수
    private String bookTitle;
    private TextView name, AItitle;
    private Gemini gemini;
    private String selectedTheme;
    private ArrayList<String> selectedCharacters, story;

    /* DB */
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    /* 효과음 */
    private SharedPreferences pref;
    private boolean isSoundOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maketitle);
        pref = getSharedPreferences("music", MODE_PRIVATE); // 효과음 초기화

        gemini = new Gemini();
        backgroundImageView = findViewById(R.id.background_image_view);
        nextBtn = findViewById(R.id.ib_nextStep);
        title = findViewById(R.id.tv_booktitle);
        name = findViewById(R.id.tv_writername);
        AItitle = findViewById(R.id.tv_AItitle);

        Intent intent = getIntent();
        byte[] imageBytes = intent.getByteArrayExtra("backgroundImageBytes");
        story = intent.getStringArrayListExtra("story");
        selectedTheme = intent.getStringExtra("selectedTheme");
        selectedCharacters = intent.getStringArrayListExtra("selectedCharacters");

        if (imageBytes != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            if (bitmap != null) {
                backgroundImageView.setImageBitmap(bitmap);
            } else {
                Toast.makeText(this, "이미지 로드 실패", Toast.LENGTH_SHORT).show();
            }
        }

        // Gemini를 사용하여 제목 생성
        generateAItitle(String.join(" ", story));

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound();
                bookTitle = title.getText().toString();
                Intent intent = new Intent(MaketitleActivity.this, MakeBookcoverActivity.class);
                intent.putExtra("bookTitle", bookTitle);
                intent.putExtra("selectedTheme", selectedTheme); // 작업이 완료되었을 때 MakeBookcoverActivity로 이동
                intent.putStringArrayListExtra("selectedCharacters", selectedCharacters);
                startActivity(intent);
                finish();
            }
        });

        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String userName = document.getString("name");
                        name.setText(userName);
                    }
                });
    }

    // Gemini를 사용하여 AI 제목 생성
    public void generateAItitle(String story) {
        String prompt = "이 동화 스토리에 대한 제목을 2가지 지어서 단답형으로 대답하세요." +
                "지어진 제목과 제목 사이에는 ', '로 띄어주세요. 아래는 동화 스토리입니다. \n 이야기: " + story;
        gemini.generateText(prompt, new Gemini.Callback() {
            @Override
            public void onSuccess(String text) {
                // UI 업데이트를 메인 스레드에서 실행
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AItitle.setText("이런 제목은 어때요? -> " + text);  // 생성된 제목을 AItitle 텍스트뷰에 설정
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MaketitleActivity.this, "AI 동화 생성 실패", Toast.LENGTH_SHORT).show();
                    }
                });
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
