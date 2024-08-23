package com.example.holymoly;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ReadBookActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView storyTextView, pageTextView;
    private ImageButton stopReadingBtn, backBtn, nextBtn;
    private ImageView backgroundImageView;
    private int currentPage = 1;
    private long backPressedTime = 0;
    private String imgName;
    // 페이지 내용과 선택지를 추적하기 위한 변수 추가
    private ArrayList<String> pageContents = new ArrayList<>();

    /* firebase 초기화 */
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
        backBtn = findViewById(R.id.ib_backStep);
        nextBtn = findViewById(R.id.ib_nextStep);
        storyTextView.setMovementMethod(new ScrollingMovementMethod());

        backBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        stopReadingBtn.setOnClickListener(this);

        // 이전 액티비티에서 imgName 가져오기
        imgName = getIntent().getStringExtra("imgName");

        // 이미지 및 텍스트 로드
        loadImage(imgName);
        loadText(imgName);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.ib_backStep) { backPage();}        // 이전 페이지로
        else if(v.getId() == R.id.ib_nextStep) { nextPage(); }  // 다음 페이지로
        else if(v.getId() == R.id.ib_stopReading) { // 그만 읽기
            if (System.currentTimeMillis() - backPressedTime >= 2000) {
                backPressedTime = System.currentTimeMillis();
                Toast.makeText(this, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
            } else {
                finish(); // 액티비티 종료
            }
        }
    }
    private void loadImage(String imgName) {
        String[] parts = imgName.split("_");
        String uid = parts[0];
        String theme = parts[1];
        String index = parts[2];
        String uidIndex = uid + "_" + index + ".png";

        StorageReference imgRef = storageRef.child("background/" + theme + "/" + uidIndex);
        // 이미지 로드
        imgRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(this).load(uri).into(backgroundImageView);
        });
    }
    // 이전 페이지로 이동
    private void backPage() {
        if (currentPage > 1) {
            currentPage--;
            showPage(currentPage); // 페이지 내용 업데이트
        } else {
            Toast.makeText(this, "첫 번째 페이지입니다.", Toast.LENGTH_SHORT).show();
        }
    }
    // 다음 페이지로 이동
    private void nextPage() {
        if (currentPage < pageContents.size()) {
            currentPage++;
            showPage(currentPage); // 페이지 내용 업데이트
        } else {
            Toast.makeText(this, "마지막 페이지입니다.", Toast.LENGTH_SHORT).show();
        }
    }
    // Storage에서 텍스트 로드
    private void loadText(String imgName) {
        String uidThemeIndex = compareFiles(imgName);
        // Storage에서 txt 파일 목록 가져오기
        StorageReference txtRef = storageRef.child("stories/" + uidThemeIndex);
        txtRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
            String textContent = new String(bytes);
            bookTextContent(textContent);
            showPage(currentPage);
        });
    }
    // imgName으로 파일명을 txt 파일로 반환
    private String compareFiles(String imgName) {
        String[] parts = imgName.split("_");
        String uid = parts[0];
        String theme = parts[1];
        String index = parts[2];

        return uid + "_" + theme + "_" + index + ".txt";
    }
    // 전체 텍스트를 페이지 + 숫자 별로 분리
    private void bookTextContent(String textContent) {
        String[] pages = textContent.split("(?=페이지 \\d+)");
        for (String page : pages) {
            // "페이지"와 숫자 부분을 제거하고, 나머지 텍스트만 추가
            String cleanedPage = page.replaceAll("페이지 \\d+", "").trim();
            pageContents.add(cleanedPage);
        }
    }
    private void showPage(int num) {
        if (num > 0 && num <= pageContents.size()) {
            storyTextView.setText(pageContents.get(num - 1));
            pageTextView.setText(currentPage + " / " + pageContents.size());
            storyTextView.scrollTo(0, 0); // 스크롤뷰를 맨 위로 초기화
        }
    }
}