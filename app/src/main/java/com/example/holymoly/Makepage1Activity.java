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
    private Gemini gemini;
    private MakeStory makeStory;
    private String selectedTheme; // 테마를 저장할 변수
    private ArrayList<String> selectedCharacters; // 캐릭터 목록을 저장할 변수
    private Handler handler = new Handler(); // UI 업데이트를 위한 Handler

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_makepage1);

        // UI 요소 초기화
        karlo = new Karlo(1280, 800);
        gemini = new Gemini();
        storyTextView = findViewById(R.id.tv_pageText1);
        backgroundImageView = findViewById(R.id.background_image_view); // 배경 ImageView 초기화

        // Intent로부터 데이터 가져오기
        Intent intent = getIntent();
        selectedTheme = intent.getStringExtra("selectedTheme");
        selectedCharacters = intent.getStringArrayListExtra("selectedCharacters");

        // 동화 생성 시작
        generateStory();
    }

    private void generateStory() {
        makeStory = new MakeStory(this, selectedTheme, String.join(", ", selectedCharacters));
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

    public void onStoryGenerated(final String storyText) {
        translateThemeAndCharacters(storyText);
    }

    public void translateThemeAndCharacters(final String storyText) {
        translateTheme(selectedTheme, new Gemini.Callback() { // 테마 번역
            @Override
            public void onSuccess(String translatedTheme) {  // 테마 번역에 성공한다면
                translateCharacters(selectedCharacters, new Gemini.Callback() { // 캐릭터 이름 번역
                    @Override
                    public void onSuccess(String translatedCharacters) { // 캐릭터 번역에도 성공한다면, 번역된 테마와 캐릭터로 프롬프트 생성
                        String prompt = "Dreamy, fairytale, cute, smooth, fancy, twinkle, super bright, cartoon style. " +  translatedCharacters + "are together. the background of a " + translatedTheme;
                        generateBackgroundImage(prompt, storyText); //배경 이미지 생성
                    }

                    @Override
                    public void onFailure(Throwable t) { // 캐릭터 번역 실패시
                        Toast.makeText(Makepage1Activity.this, "캐릭터 번역 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {// 테마 번역 실패시
                Toast.makeText(Makepage1Activity.this, "테마 번역 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //배경 이미지 생성
    private void generateBackgroundImage(String prompt, String storyText) {
        String negative_prompt = "ugly, worst quality, low quality, normal quality, watermark, distorted face, poorly drawn face, framework";
        karlo.requestImage(prompt, negative_prompt, new Karlo.Callback() { // Karlo를 사용하여 이미지 요청
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
                                    backgroundImageView.setImageBitmap(bitmap); // 배경 이미지로 설정
                                    Toast.makeText(Makepage1Activity.this, prompt, Toast.LENGTH_SHORT).show();
                                    displayStoryText(storyText); // 배경 이미지가 설정되면, 텍스트를 보여줌
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


    //-----------------------------------------------------------------------------------------------
    //테마를 번역하는 함수
    private void translateTheme(String theme, Gemini.Callback callback) {
        String prompt = "Translate the following theme to English: " + theme + ". Please provide a concise, single-word or short-phrase answer.";
        gemini.generateText(prompt, new Gemini.Callback() {
            @Override
            public void onSuccess(String text) {
                callback.onSuccess(text.trim());
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    //캐릭터를 번역하는 함수
    private void translateCharacters(ArrayList<String> characters, Gemini.Callback callback) {
        StringBuilder promptBuilder = new StringBuilder("Translate the following character names to English and prepend 'a cute ' before each noun. Separate the nouns with commas: ");
        for (int i = 0; i < characters.size(); i++) {
            promptBuilder.append(characters.get(i));
            if (i < characters.size() - 1) {
                promptBuilder.append(", ");
            }
        }
        String prompt = promptBuilder.toString();

        gemini.generateText(prompt, new Gemini.Callback() {
            @Override
            public void onSuccess(String text) {
                callback.onSuccess(text.trim());
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onFailure(t);
            }
        });
    }


    // URL에서 Bitmap 객체를 생성하는 함수
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
