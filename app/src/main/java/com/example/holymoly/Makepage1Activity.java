package com.example.holymoly;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class Makepage1Activity extends AppCompatActivity {
    private TextView storyTextView; // 동화 내용을 표시할 TextView
    private ImageView backgroundImageView; // 배경 이미지를 표시할 ImageView
    private Karlo karlo;
    private MakeStory makeStory;
    private String theme; // 테마를 저장할 변수
    private String characters; // 캐릭터 목록을 문자열로 저장할 변수
    private Handler handler = new Handler(); // UI 업데이트를 위한 Handler

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_makepage1);

        // UI 요소 초기화
        karlo = new Karlo(1280, 800);
        storyTextView = findViewById(R.id.tv_pageText1);
        backgroundImageView = findViewById(R.id.background_image_view); // 배경 ImageView 초기화

        // Intent로부터 데이터 가져오기
        Intent intent = getIntent();
        theme = intent.getStringExtra("selectedTheme");
        ArrayList<String> selectedCharacters = intent.getStringArrayListExtra("selectedCharacters");

        // 선택된 캐릭터 목록을 문자열로 변환
        characters = String.join(", ", selectedCharacters);

        // 동화 생성 시작
        generateStory();
    }

    private void generateStory() {
        // MakeStory 인스턴스 생성
        makeStory = new MakeStory(this, theme, characters);

        // 동화 도입부 생성 시작
        makeStory.generateInitialStory();
    }

    /* 동화 텍스트를 한번에 표시
    public void updateStoryTextView(final String newText) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                // 현재 동화 내용을 업데이트
                storyTextView.setText(newText);
            }
        });
    }
    */

    // 동화 생성 완료 후 한 글자씩 표시
    public void displayStoryText(final String storyText) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                final int delay = 120; // 글자 출력 간격 (밀리초)
                final int length = storyText.length();
                storyTextView.setText(""); // TextView 초기화

                for (int i = 0; i <= length; i++) {
                    final int index = i;
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // 한 글자씩 텍스트 설정
                            storyTextView.setText(storyText.substring(0, index));
                        }
                    }, delay * i); // 각 글자마다 딜레이 적용
                }
            }
        });
    }

    public void onStoryGenerated(final String storyText) {
        // 동화가 생성된 후 이미지 생성 및 설정
        generateBackgroundImage(storyText);
        // 동화가 생성된 후 텍스트를 한 글자씩 표시]
        displayStoryText(storyText);

    }

    private void generateBackgroundImage(String storyText) {
        String prompt = "Dreamy, fairytale, cute, characteristic, fancy, twinkle, super bright, sea, A Little Mermaid, a tropical fish, a shark.";

        // KarloImageGenerator를 사용하여 이미지 요청
        karlo.requestImage(prompt, new Karlo.Callback() {
            @Override
            public void onSuccess(String imageUrl) {
                // 이미지 URL에서 Bitmap 객체를 생성하고 배경에 설정
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bitmap = getBitmapFromURL(imageUrl);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (bitmap != null) {
                                    // 배경 이미지로 설정
                                    backgroundImageView.setImageBitmap(bitmap);
                                } else {
                                    Toast.makeText(Makepage1Activity.this, "이미지 로드 실패", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }).start();
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Makepage1Activity.this, "이미지 요청 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    // URL에서 Bitmap 객체를 생성하는 메서드
    private Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
