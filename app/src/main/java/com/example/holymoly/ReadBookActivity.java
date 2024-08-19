package com.example.holymoly;

import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class ReadBookActivity extends AppCompatActivity {
    private boolean isImageLoaded = false; // 이미지 로드 상태를 추적하는 변수
    private TextView storyTextView, pageTextView;
    private ImageButton stopReadingBtn, nextBtn;
    private ImageView backgroundImageView, loading;
    private String selectedTheme;
    private ArrayList<String> selectedCharacters;
    private Handler handler = new Handler();
    private Karlo karlo;
    private Gemini gemini;
    private MakeStory makeStory;
    private int num = 1;
    private long backPressedTime = 0;

    // 테마 경로 및 최종적으로 선택된 테마
    private String themePath, finalSelectedTheme;
    // 페이지 내용과 선택지를 추적하기 위한 변수 추가
    private ArrayList<String> pageContents = new ArrayList<>();

    // firebase 초기화
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_readbook);

        // UI 요소 초기화
        storyTextView = findViewById(R.id.tv_pageText);
        pageTextView = findViewById(R.id.tv_page);
        backgroundImageView = findViewById(R.id.background_image_view);
        stopReadingBtn = findViewById(R.id.ib_stopReading);
        nextBtn = findViewById(R.id.ib_nextStep);
        storyTextView.setMovementMethod(new ScrollingMovementMethod());

    }
}