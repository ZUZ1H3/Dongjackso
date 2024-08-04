package com.example.holymoly;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MakeStoryActivity extends AppCompatActivity {
    private boolean isImageLoaded = false; // 이미지 로드 상태를 추적하는 변수
    private TextView storyTextView, pageTextView;
    private Button choice1, choice2;
    private ImageButton stopMakingBtn;
    private ImageView backgroundImageView, loading;
    private String selectedTheme;
    private ArrayList<String> selectedCharacters;
    private Handler handler = new Handler();
    private Karlo karlo;
    private Gemini gemini;
    private MakeStory makeStory;
    private int num = 1;
    private long backPressedTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_makestory);

        // UI 요소 초기화
        storyTextView = findViewById(R.id.tv_pageText);
        pageTextView = findViewById(R.id.tv_page);
        backgroundImageView = findViewById(R.id.background_image_view);
        loading = findViewById(R.id.ib_loading);
        choice1 = findViewById(R.id.btn_choice1);
        choice2 = findViewById(R.id.btn_choice2);
        stopMakingBtn = findViewById(R.id.ib_stopMaking);
        storyTextView.setMovementMethod(new ScrollingMovementMethod());

        MainActivity mainActivity = new MainActivity();

        //지금까지 ArrayList에 저장한 액티비티 전부를 for문을 돌려서 finish한다.
        for (int i = 0; i < mainActivity.actList().size(); i++) {
            mainActivity.actList().get(i).finish();
        }

        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate);
        loading.setAnimation(animation);

        // Intent로부터 데이터 가져오기
        Intent intent = getIntent();
        selectedTheme = intent.getStringExtra("selectedTheme");
        selectedCharacters = intent.getStringArrayListExtra("selectedCharacters");

        // 동화 생성 시작
        gemini = new Gemini();
        karlo = new Karlo(1280, 800);
        makeStory = new MakeStory(this, selectedTheme, selectedCharacters, gemini);

        makeStory.generateInitialStory();


        choice1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choice1.setVisibility(View.INVISIBLE);
                choice2.setVisibility(View.INVISIBLE);
                if (num < 5) {
                    makeStory.generateNextStoryPart(choice1.getText().toString());
                    ++num;
                    pageTextView.setText(num + " / 6");
                } else {
                    makeStory.generateEndStoryPart(choice1.getText().toString());
                    ++num;
                    pageTextView.setText(num + " / 6");
                }
            }
        });

        choice2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choice1.setVisibility(View.INVISIBLE);
                choice2.setVisibility(View.INVISIBLE);
                if (num < 5) {
                    makeStory.generateNextStoryPart(choice2.getText().toString());
                    ++num;
                    pageTextView.setText(num + " / 6");
                } else {
                    makeStory.generateEndStoryPart(choice2.getText().toString());
                    ++num;
                    pageTextView.setText(num + " / 6");
                }

            }
        });

        stopMakingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (System.currentTimeMillis() - backPressedTime >= 2000) {
                    backPressedTime = System.currentTimeMillis();
                    Toast.makeText(MakeStoryActivity.this, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
                } else {
                    finish();
                }
            }
        });
    }


    public void translate(final String storyText) {
        //선택한 테마 번역
        makeStory.translateTheme(selectedTheme, new TranslationCallback() {
            @Override
            public void onSuccess(String translatedTheme) { //테마 번역에 성공하면
                //선택한 캐릭터들 번역
                makeStory.translateCharacters(selectedCharacters, new TranslationCallback() {
                    public void onSuccess(String translatedCharacters) { //캐릭터 번역 성공하면, 프롬프트 생성
                        String prompt = "Dreamy, fairytale, cute, smooth, fancy, twinkle, super bright, cartoon style. " + translatedCharacters + " are together. the background of a " + translatedTheme;
                        generateBackgroundImage(prompt, storyText); //이미지 생성
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        showToast("캐릭터 번역 실패: " + t.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {
                showToast("테마 번역 실패: " + t.getMessage());
            }
        });
    }

    private void generateBackgroundImage(String prompt, String storyText) {
        String negativePrompt = "ugly, worst quality, low quality, normal quality, watermark, distorted face, poorly drawn face, framework";
        karlo.requestImage(prompt, negativePrompt, new Karlo.Callback() { //karlo로 이미지 생성
            @Override
            public void onSuccess(String imageUrl) { //성공한 경우
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bitmap = getBitmapFromURL(imageUrl);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (bitmap != null) { //이미지가 업로드 된 경우
                                    backgroundImageView.setImageBitmap(bitmap);
                                    loading.getAnimation().cancel();
                                    loading.clearAnimation();
                                    loading.setVisibility(View.INVISIBLE);
                                    isImageLoaded = true;
                                    //showToast(prompt);
                                    displayStoryText(storyText); //동화 출력
                                } else {
                                    showToast("이미지 로드 실패");
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
                        showToast("이미지 요청 실패: " + e.getMessage());
                    }
                });
            }
        });
    }

    public void displayStoryText(final String storyText) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                final int delay = 60;
                final int length = storyText.length();
                storyTextView.setText("");

                for (int i = 0; i <= length; i++) {
                    final int index = i;
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            storyTextView.setText(storyText.substring(0, index));
                            if (index == length) {
                                if (isImageLoaded && num <= 5) {
                                    makeStory.generateChoices(); // 이미지가 로드된 후에 선택지 생성
                                }
                            }
                        }
                    }, delay * i);
                }

            }
        });
    }

    public void showChoices(String choicesText) {
        String[] choices = choicesText.split("/");
        if (choices.length >= 2) {
            choice1.setText(choices[0].trim());
            choice2.setText(choices[1].trim());
        } else {
            showToast("선택지가 부족합니다.");
        }
        choice1.setVisibility(View.VISIBLE);
        choice2.setVisibility(View.VISIBLE);
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

    private void showToast(String message) {
        Toast.makeText(MakeStoryActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
