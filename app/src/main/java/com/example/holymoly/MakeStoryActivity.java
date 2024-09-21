package com.example.holymoly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MakeStoryActivity extends AppCompatActivity {
    private LinearLayout loadingLayout;
    private ConstraintLayout constraintLayout;
    private ProgressBar progressBar;
    TextView progresstextView;
    private ImageView character;
    private int progressStatus = 0;
    private float characterPosition = 0f;  // 캐릭터의 X축 위치를 저장할 변수
    private final int progressBarUpdateInterval = 200;  // ProgressBar 업데이트 주기 (ms)
    private final int characterMoveInterval = 7;      // 캐릭터 이동 주기 (ms)
    private final float screenLimit = 900f;            // 화면의 최대 X 좌표
    private final float initialPosition = 0f;          // 캐릭터의 초기 위치
    private boolean isImageLoaded = false; // 이미지 로드 상태를 추적하는 변수
    private TextView nextTextView, storyTextView, pageTextView, selectText1, selectText2, selectMic3;
    private ImageButton stopMakingBtn, nextBtn, retryBtn;
    private ImageView backgroundImageView, selectImage1, selectImage2, selectMic1, selectMic2, background;
    private String selectedTheme;
    private ArrayList<String> selectedCharacters;
    private Handler handler = new Handler();
    private Karlo karlo;
    private Gemini gemini;
    private MakeStory makeStory;
    private int num = 1;
    private long backPressedTime = 0;
    private String recognizedText;
    private boolean choicesVisible = false;
    private boolean isTextLoaded = false; // 텍스트 다 호출됐는지 확인

    // 테마 경로 및 최종적으로 선택된 테마
    private String themePath, finalSelectedTheme, fileName;
    // 페이지 내용과 선택지를 추적하기 위한 변수 추가
    private ArrayList<String> pageContents = new ArrayList<>();

    // firebase 초기화
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();

    /* 효과음 */
    private SharedPreferences pref;
    private boolean isSoundOn;

    //bubble제작
    private int screenWidth, screenHeight;
    private List<ImageView> bubbles = new ArrayList<>(); // 버블 리스트

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_makestory);
        pref = getSharedPreferences("music", MODE_PRIVATE); // 효과음 초기화

        // UI 요소 초기화
        nextTextView = findViewById(R.id.nextTextView);
        storyTextView = findViewById(R.id.tv_pageText);
        pageTextView = findViewById(R.id.tv_page);
        backgroundImageView = findViewById(R.id.background_image_view);
        selectText1 = findViewById(R.id.tv_select1);
        selectText2 = findViewById(R.id.tv_select2);
        selectImage1 = findViewById(R.id.iv_select1);
        selectImage2 = findViewById(R.id.iv_select2);
        selectMic1 = findViewById(R.id.iv_mic1);
        selectMic2 = findViewById(R.id.iv_mic2);
        selectMic3 = findViewById(R.id.iv_mic3);

        stopMakingBtn = findViewById(R.id.ib_stopMaking);
        nextBtn = findViewById(R.id.ib_nextStep);
        retryBtn = findViewById(R.id.ib_retry);
        background = findViewById(R.id.iv_writingZone);
        progressBar = findViewById(R.id.progressBar);
        progresstextView = findViewById(R.id.textView);
        character = findViewById(R.id.characterImage);
        storyTextView.setMovementMethod(new ScrollingMovementMethod()); //스크롤 가능하도록
        MainActivity mainActivity = new MainActivity();

        //로딩 레이아웃
        constraintLayout = findViewById(R.id.constraintLayout);
        loadingLayout = findViewById(R.id.loadingLayout);


        //지금까지 ArrayList에 저장한 액티비티 전부를 for문을 돌려서 finish한다.
        for (int i = 0; i < mainActivity.actList().size(); i++) {
            mainActivity.actList().get(i).finish();
        }

        // Intent로부터 데이터 가져오기
        Intent intent = getIntent();
        selectedTheme = intent.getStringExtra("selectedTheme");
        selectedCharacters = intent.getStringArrayListExtra("selectedCharacters");

        // 페이지 초기화
        for (int i = 0; i < 6; i++) {
            pageContents.add(""); // 페이지 내용 초기화
        }

        // 동화 생성 시작
        gemini = new Gemini();
        karlo = new Karlo(1280, 800);
        makeStory = new MakeStory(this, selectedTheme, selectedCharacters, gemini);

        makeStory.generateInitialStory();//동화 도입부 생성

        // VoiceActivity에서 전달된 recognizedText를 받아오기
        intent = getIntent();
        String recognizedText = intent.getStringExtra("recognizedText");

        // recognizedText가 null이 아니면 selectMic3에 텍스트 설정
        if (recognizedText != null && !recognizedText.isEmpty()) {
            selectMic3.setText(recognizedText);  // 음성 인식 텍스트를 텍스트뷰에 설정
        }

        //레이아웃 폭 높이 체크 - bubble생성용으로 쓰임
        constraintLayout.post(new Runnable() {
            @Override
            public void run() {
                // ic_bubble.png 이미지를 10개 추가
                generateBubbles(); // 이전의 addRandomBubbles를 generateBubbles로 변경
            }
        });

        // ProgressBar 업데이트를 위한 Thread
        new Thread(new Runnable() {
            public void run() {
                while (progressStatus < 100) {
                    progressStatus += 1;

                    handler.post(new Runnable() {
                        public void run() {
                            // ProgressBar 업데이트
                            progressBar.setProgress(progressStatus);
                            progresstextView.setText(progressStatus + "%");
                        }
                    });
                    try {
                        // ProgressBar 업데이트 주기
                        Thread.sleep(progressBarUpdateInterval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                // 캐릭터 이동 멈추기
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        character.setTranslationX(characterPosition); // 마지막 위치 고정
                    }
                });

            }
        }).start();

        // 캐릭터 이동을 위한 Thread
        new Thread(new Runnable() {
            public void run() {
                while (progressStatus < 100) {
                    try {
                        // 캐릭터 이동 주기
                        Thread.sleep(characterMoveInterval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handler.post(new Runnable() {
                        public void run() {
                            // 캐릭터의 X축 이동
                            characterPosition += 0.6f; // 원하는 이동 속도

                            // 캐릭터가 화면의 최대 X 좌표를 넘으면 초기 위치로 이동
                            if (characterPosition > screenLimit) {
                                characterPosition = initialPosition; // 초기 위치로 리셋
                            }
                            character.setTranslationX(characterPosition); // X축 위치 업데이트
                        }
                    });
                }
            }
        }).start();


        selectText1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choicesVisible = false;
                sound();
                nextTextView.setVisibility(View.INVISIBLE);
                selectImage1.setVisibility(View.INVISIBLE);
                selectImage2.setVisibility(View.INVISIBLE);
                selectText1.setVisibility(View.INVISIBLE);
                selectText2.setVisibility(View.INVISIBLE);
                selectMic1.setVisibility(View.INVISIBLE);
                selectMic2.setVisibility(View.INVISIBLE);
                selectMic3.setVisibility(View.INVISIBLE);

                String selectedChoice = selectText1.getText().toString(); // 선택지1 가져오기

                if (num < 6) {
                    // 현재 페이지 내용, 선택지 저장
                    pageContents.set(num - 1, storyTextView.getText().toString() + selectedChoice);

                    if (num < 5) {
                        makeStory.generateNextStoryPart(selectedChoice, num);
                    } else if (num == 5) {
                        makeStory.generateEndStoryPart(selectedChoice);
                    }
                    ++num;
                }
                if (num > 5) nextBtn.setVisibility(View.VISIBLE);

            }
        });

        selectText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choicesVisible = false;
                sound();
                nextTextView.setVisibility(View.INVISIBLE);
                selectImage1.setVisibility(View.INVISIBLE);
                selectImage2.setVisibility(View.INVISIBLE);
                selectText1.setVisibility(View.INVISIBLE);
                selectText2.setVisibility(View.INVISIBLE);
                selectMic1.setVisibility(View.INVISIBLE);
                selectMic2.setVisibility(View.INVISIBLE);
                selectMic3.setVisibility(View.INVISIBLE);

                String selectedChoice = selectText2.getText().toString(); // 선택지2 가져오기

                if (num < 6) {
                    // 현재 페이지 내용, 선택지 저장
                    pageContents.set(num - 1, storyTextView.getText().toString() + selectedChoice);

                    if (num < 5) {
                        makeStory.generateNextStoryPart(selectedChoice, num);
                    } else if (num == 5) {
                        makeStory.generateEndStoryPart(selectedChoice);
                    }
                    ++num;
                }
                if (num > 5) nextBtn.setVisibility(View.VISIBLE);
            }
        });

        selectMic2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choicesVisible = false;
                sound();
                Intent intent = new Intent(MakeStoryActivity.this, VoiceActivity.class);

                // 이미지 데이터 전달
                byte[] imageBytes = (byte[]) nextBtn.getTag();
                intent.putExtra("backgroundImageBytes", imageBytes);

                startActivity(intent);
            }
        });

        selectMic3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choicesVisible = false;
                sound(); // 효과음
                // recognizedText 가져오기
                String selectedChoice = selectMic3.getText().toString();

                // recognizedText가 null이거나 빈 문자열일 경우 처리
                if (!TextUtils.isEmpty(selectedChoice)) {
                    nextTextView.setVisibility(View.INVISIBLE);
                    selectImage1.setVisibility(View.INVISIBLE);
                    selectImage2.setVisibility(View.INVISIBLE);
                    selectText1.setVisibility(View.INVISIBLE);
                    selectText2.setVisibility(View.INVISIBLE);
                    selectMic1.setVisibility(View.INVISIBLE);
                    selectMic2.setVisibility(View.INVISIBLE);
                    selectMic3.setVisibility(View.INVISIBLE);

                    // num에 따라 다음 스토리 생성
                    if (num < 6) {
                        // 현재 페이지 내용 저장
                        pageContents.set(num - 1, storyTextView.getText().toString() + selectedChoice);
                        if (num < 5) {
                            makeStory.generateNextStoryPart(selectedChoice, num);
                        } else if (num == 5) {
                            makeStory.generateEndStoryPart(selectedChoice);
                        }
                        ++num;
                    }
                    // num이 5보다 클 때 nextBtn 표시
                    if (num > 5) nextBtn.setVisibility(View.VISIBLE);
                } else {
                    // recognizedText가 비어 있으면 음성 인식 재시작
                    Intent intent = new Intent(MakeStoryActivity.this, VoiceActivity.class);

                    // 이미지 데이터가 있는 경우 전달
                    byte[] imageBytes = (byte[]) nextBtn.getTag();
                    if (imageBytes != null) {
                        intent.putExtra("backgroundImageBytes", imageBytes);
                    }
                    startActivity(intent);
                }
            }
        });

        stopMakingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound();
                if (System.currentTimeMillis() - backPressedTime >= 2000) {
                    backPressedTime = System.currentTimeMillis();
                    deleteImage(); // 업로드된 배경 이미지 삭제
                    Toast.makeText(MakeStoryActivity.this, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
                } else {
                    finish();
                }
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound();
                if (num == 6) {
                    pageContents.set(num - 1, storyTextView.getText().toString());
                }
                if (isImageLoaded && isTextLoaded) {
                    byte[] imageBytes = (byte[]) nextBtn.getTag();
                    Intent intent = new Intent(MakeStoryActivity.this, MaketitleActivity.class);
                    intent.putExtra("backgroundImageBytes", imageBytes);
                    // 표지 제작할 때 테마 별로 구분하기 위한 용도
                    intent.putExtra("selectedTheme", finalSelectedTheme);
                    intent.putStringArrayListExtra("selectedCharacters", selectedCharacters);
                    intent.putStringArrayListExtra("story", pageContents);
                    saveStory(); // 내용 저장
                    startActivity(intent);
                }
                else if(!isTextLoaded) {
                    showToast("이야기가 진행 중입니다.");
                    return;
                }
                else {
                    showToast("이미지가 로드되지 않았습니다.");
                }
                finish();
            }
        });

    }

    //bubble생성
    private void generateBubbles() {
        final int[] currentBubbleNumber = {1};
        int layoutWidth = constraintLayout.getWidth();
        int layoutHeight = constraintLayout.getHeight();

        int bubbleSize = (int) (140 * getResources().getDisplayMetrics().density); // 버블 크기를 140dp로 설정
        int spacing = bubbleSize / 4; // 추가 간격을 더 넓게 설정

        List<Rect> placedBubbles = new ArrayList<>(); // 생성된 버블의 위치를 저장

        for (int i = 0; i < 10; i++) {
            FrameLayout bubbleLayout = new FrameLayout(this);
            ImageView bubble = new ImageView(this);
            bubble.setImageResource(R.drawable.ic_bubble);

            // 커스텀 폰트를 불러오기
            Typeface customFont = ResourcesCompat.getFont(this, R.font.meetme);

            FrameLayout.LayoutParams bubbleParams = new FrameLayout.LayoutParams(bubbleSize, bubbleSize);

            TextView bubbleNumber = new TextView(this);
            bubbleNumber.setText(String.valueOf(i + 1));
            bubbleNumber.setTextColor(Color.WHITE);
            bubbleNumber.setTextSize(70);
            bubbleNumber.setGravity(Gravity.CENTER);

            // 커스텀 폰트를 적용하는 부분
            bubbleNumber.setTypeface(customFont);

            FrameLayout.LayoutParams textParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            textParams.gravity = Gravity.CENTER;

            int x, y;
            Rect newBubbleRect;
            int attempts = 0;

            // 겹치지 않는 좌표를 찾을 때까지 시도
            do {
                x = new Random().nextInt(layoutWidth - bubbleSize - spacing);
                y = new Random().nextInt((int) (layoutHeight * 0.75) - bubbleSize - spacing); // 60%까지만

                newBubbleRect = new Rect(x, y, x + bubbleSize + spacing, y + bubbleSize + spacing);
                attempts++;

                if (attempts > 100) {
                    break; // 너무 많은 시도를 하면 무한 루프 방지
                }
            } while (!isNonOverlapping(newBubbleRect, placedBubbles));

            if (attempts > 100) {
                continue; // 좌표 찾기를 포기하고 넘어감
            }

            bubbleLayout.setX(x);
            bubbleLayout.setY(y);

            bubbleLayout.addView(bubble, bubbleParams);
            bubbleLayout.addView(bubbleNumber, textParams);
            constraintLayout.addView(bubbleLayout);

            // 클릭 리스너 추가
            bubbleLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int number = Integer.parseInt(bubbleNumber.getText().toString());
                    if (number == currentBubbleNumber[0]) {
                        // 클릭한 숫자가 맞으면 해당 버블을 제거하고 다음 숫자로 증가
                        sound();
                        constraintLayout.removeView(bubbleLayout);
                        currentBubbleNumber[0]++; // 다음 숫자로 증가
                    }
                }
            });

            // 생성된 버블의 위치 저장
            placedBubbles.add(newBubbleRect);
        }
    }

    private boolean isNonOverlapping(Rect newBubbleRect, List<Rect> placedBubbles) {
        for (Rect existingBubbleRect : placedBubbles) {
            if (Rect.intersects(newBubbleRect, existingBubbleRect)) {
                return false; // 겹치면 false 반환
            }
        }
        return true; // 겹치지 않으면 true 반환
    }

    // 선택한 테마와 캐릭터를 번역 하는 함수. 이미지를 만들 때 사용함
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

    //동화 배경 이미지를 생성하는 함수
    private void generateBackgroundImage(String prompt, String storyText) {
        String negativePrompt = "ugly, worst quality, low quality, normal quality, watermark, distorted face, poorly drawn face, framework";
        karlo.requestImage(prompt, negativePrompt, new Karlo.Callback() {
            @Override
            public void onSuccess(String imageUrl) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bitmap = getBitmapFromURL(imageUrl);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (bitmap != null) {
                                    backgroundImageView.setImageBitmap(bitmap);
                                    isImageLoaded = true;
                                    storyTextView.setVisibility(View.VISIBLE);
                                    retryBtn.setVisibility(View.VISIBLE);
                                    stopMakingBtn.setVisibility(View.VISIBLE);
                                    background.setVisibility(View.VISIBLE);
                                    //loadingLayout.setVisibility(View.INVISIBLE);
                                    constraintLayout.setVisibility(View.INVISIBLE);
                                    uploadImage(bitmap); // Storage에 배경 업로드
                                    displayStoryText(storyText); //동화 출력

                                    // 비트맵을 ByteArray로 변환하여 인텐트에 저장
                                    byte[] imageBytes = convertBitmapToByteArray(bitmap);
                                    nextBtn.setTag(imageBytes);
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


    // 텍스트 타이핑 효과 및 터치 시 전체 텍스트 표시
    public void displayStoryText(final String storyText) {
        pageTextView.setText(num + " / 6");

        // 터치하면 전체 텍스트를 즉시 표시하는 플래그
        final boolean[] textFullyDisplayed = {false};
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
                            // 텍스트가 아직 전부 표시되지 않았으면 타이핑 효과 진행
                            if (!textFullyDisplayed[0]) {
                                storyTextView.setText(storyText.substring(0, index));
                            }
                            if (index == length) { //텍스트가 전부 표시되면
                                if (isImageLoaded && num <= 5) { //5장 이하일 때
                                    makeStory.generateChoices(num); // 이미지가 로드된 후에 선택지 생성
                                }
                                if(num == 1) nextTextView.setVisibility(View.VISIBLE);
                                if(num == 6) isTextLoaded = true;
                            }
                        }
                    }, delay * i);
                }

                // storyTextView에 터치 이벤트 리스너 추가
                storyTextView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            // 터치 시 핸들러의 모든 작업 취소하고 전체 텍스트 표시
                            handler.removeCallbacksAndMessages(null);
                            storyTextView.setText(storyText);
                            textFullyDisplayed[0] = true; // 전체 텍스트 표시 상태로 플래그 설정
                            if (isImageLoaded && num <= 5 && !choicesVisible) {
                                makeStory.generateChoices(num);
                            }

                            if(num == 1) nextTextView.setVisibility(View.VISIBLE);
                            if(num == 6) isTextLoaded = true;
                        }
                        // false를 반환하여 기본 스크롤 동작을 허용
                        return false;
                    }
                });

                retryBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (isImageLoaded && num <= 5) {
                            makeStory.generateChoices(num); // 선택지 생성
                        }
                    }
                });
            }
        });
    }

    //선택지를 화면에 띄움
    public void showChoices(String choicesText) {
        choicesVisible = true;
        // selectMic3에 텍스트가 있는지 확인하고, 있다면 비움
        if (!TextUtils.isEmpty(selectMic3.getText())) {
            selectMic3.setText(""); // 텍스트 지움
        }
        String[] choices = choicesText.split("/");
        if (choices.length >= 2) {
            selectText1.setText(choices[0].trim());
            selectText2.setText(choices[1].trim());
        } else {
            showToast("선택지가 부족합니다.");
        }
        selectImage1.setVisibility(View.VISIBLE);
        selectImage2.setVisibility(View.VISIBLE);
        selectText1.setVisibility(View.VISIBLE);
        selectText2.setVisibility(View.VISIBLE);
        selectMic1.setVisibility(View.VISIBLE);
        selectMic2.setVisibility(View.VISIBLE);
        selectMic3.setVisibility(View.VISIBLE);
    }

    // URL에서 Bitmap 객체를 생성하는 함수
    private Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();

            // 비트맵의 크기를 조절하기 위해 비트맵을 먼저 Decode 해보기
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, options);
            input.close();

            // 비트맵의 크기를 조절하기 위한 샘플링 비율을 계산
            options.inSampleSize = calculateInSampleSize(options, 1280, 800);
            options.inJustDecodeBounds = false;

            // 크기를 조절하여 비트맵을 Decode
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            input = connection.getInputStream();
            return BitmapFactory.decodeStream(input, null, options);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 비트맵 샘플링 비율을 계산하는 메서드
    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // 원본 이미지의 크기
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // 이미지의 크기를 절반으로 줄이면서 요청된 크기와 비교
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private void showToast(String message) {
        Toast.makeText(MakeStoryActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private byte[] convertBitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos); // JPEG 포맷으로 압축, 압축 비율 80%
        return baos.toByteArray();
    }

    private void uploadImage(Bitmap bitmap) {
        // 일반 테마 목록
        List<String> commonThemes = Arrays.asList("바다", "궁전", "숲", "마을", "우주", "사막");
        if (commonThemes.contains(selectedTheme)) {
            // 표준 테마일 경우
            themePath = "background/" + selectedTheme;
            finalSelectedTheme = selectedTheme;
        } else {
            // 커스텀 테마일 경우
            themePath = "background/커스텀/";
            finalSelectedTheme = "커스텀";
        }

        // background/theme 별로 저장된 경로
        StorageReference themeRef = storageRef.child(themePath);

        // 경로에 있는 파일 목록 가져오기
        themeRef.listAll().addOnSuccessListener(listResult -> {
            // 유저별 index 증가 처리
            int userCount = 0;
            for (StorageReference item : listResult.getItems()) {
                // 파일 이름에서 유저를 추출하여 비교
                String userId = item.getName();
                if (userId.startsWith(user.getUid())) userCount++;
            }
            int index = userCount + 1;

            fileName = user.getUid() + "_" + index + ".png";

            // 이미지가 저장될 경로 설정
            StorageReference imageRef = themeRef.child(fileName);

            // bitmap을 png로 압축 및 저장
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();

            // 업로드 시작
            UploadTask uploadTask = imageRef.putBytes(data);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                Toast.makeText(this, "이미지 업로드 성공", Toast.LENGTH_SHORT).show();
            });
        });
    }

    // 업로드된 이미지 삭제
    private void deleteImage() {
        StorageReference themeRef = storageRef.child(themePath);
        StorageReference imageRef = themeRef.child(fileName);

        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(MakeStoryActivity.this, "이미지 삭제", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveStory() {
        // 모든 페이지 내용을 하나의 StringBuilder에 저장
        StringBuilder fileContent = new StringBuilder();
        StorageReference fileRef = storageRef.child("stories/");

        for (int i = 0; i < pageContents.size(); i++) {
            String pageContent = pageContents.get(i); // 페이지 내용 가져오기
            fileContent.append("페이지 ").append(i + 1).append("\n");
            fileContent.append(pageContent).append("\n");
        }

        fileRef.listAll().addOnSuccessListener(listResult -> {
            // 테마별 index 증가 처리
            int themeCount = 0;
            for (StorageReference item : listResult.getItems()) {
                // 파일 이름에서 테마를 추출하여 비교
                String userId = item.getName();
                String itemName = item.getName();
                String[] parts = itemName.split("_");
                if (userId.startsWith(user.getUid()) && parts.length > 2 && parts[1].equals(finalSelectedTheme))
                    themeCount++;
            }

            int index = themeCount + 1;
            String fileName = user.getUid() + "_" + finalSelectedTheme + "_" + index + ".txt";

            // 파일 저장
            try {
                FileOutputStream fos = openFileOutput(fileName, MODE_PRIVATE);
                fos.write(fileContent.toString().getBytes("UTF-8"));  // 한글 인코딩을 위해 UTF-8 사용
                fos.close();
                uploadFile(fileName);
            } catch (IOException e) {
                showToast("파일 저장 실패: " + e.getMessage());
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // VoiceActivity에서 전달된 recognizedText를 받아오기
        String recognizedText = intent.getStringExtra("recognizedText");

        // selectMic3 텍스트뷰에 반영
        if (recognizedText != null && !recognizedText.isEmpty()) {
            selectMic3.setText(recognizedText);
        }
        selectMic2.setVisibility(View.INVISIBLE);
    }

    // Firebase Storage에 파일 업로드
    private void uploadFile(String fileName) {
        // Firebase Storage 참조 생성
        StorageReference fileRef = storageRef.child("stories/" + fileName);

        // 저장된 파일 읽기
        try {
            FileInputStream fis = openFileInput(fileName);
            byte[] data = new byte[fis.available()];
            fis.read(data);
            fis.close();

            // 파일 메타데이터 생성 (MIME 타입 설정)
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("text/plain")
                    .build();

            // 파일 업로드
            UploadTask uploadTask = fileRef.putBytes(data, metadata);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                showToast("파일 업로드 성공");
            });
        } catch (IOException e) {
            showToast("파일 읽기 실패: " + e.getMessage());
        }
    }

    // 효과음
    public void sound() {
        isSoundOn = pref.getBoolean("on&off2", true);
        Intent intent = new Intent(this, SoundService.class);
        if (isSoundOn) startService(intent); // 효과음 on
        else stopService(intent);            // 효과음 off
    }
}
