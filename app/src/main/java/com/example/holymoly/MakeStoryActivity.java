package com.example.holymoly;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

public class MakeStoryActivity extends AppCompatActivity {
    private boolean isImageLoaded = false; // 이미지 로드 상태를 추적하는 변수
    private TextView storyTextView, pageTextView, selectText1, selectText2;
    private ImageButton stopMakingBtn, nextBtn;
    private ImageView backgroundImageView, loading, selectImage1, selectImage2, selectMic, nextStory;
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
        setContentView(R.layout.activity_makestory);

        // UI 요소 초기화
        storyTextView = findViewById(R.id.tv_pageText);
        pageTextView = findViewById(R.id.tv_page);
        backgroundImageView = findViewById(R.id.background_image_view);
        loading = findViewById(R.id.ib_loading);
        selectText1 = findViewById(R.id.tv_select1);
        selectText2 = findViewById(R.id.tv_select2);
        selectImage1 = findViewById(R.id.iv_select1);
        selectImage2 = findViewById(R.id.iv_select2);
        selectMic = findViewById(R.id.iv_mic);
        nextStory = findViewById(R.id.iv_nextstory);
        stopMakingBtn = findViewById(R.id.ib_stopMaking);
        nextBtn = findViewById(R.id.ib_nextStep);

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

        // 페이지 초기화
        for (int i = 0; i < 6; i++) {
            pageContents.add(""); // 페이지 내용 초기화
        }

        // 동화 생성 시작
        gemini = new Gemini();
        karlo = new Karlo(1280, 800);
        makeStory = new MakeStory(this, selectedTheme, selectedCharacters, gemini);

        makeStory.generateInitialStory();

        selectMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MakeStoryActivity.this, VoiceActivity.class);
                startActivity(intent);
            }
        });

        selectText1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextStory.setVisibility(View.INVISIBLE);
                selectImage1.setVisibility(View.INVISIBLE);
                selectImage2.setVisibility(View.INVISIBLE);
                selectText1.setVisibility(View.INVISIBLE);
                selectText2.setVisibility(View.INVISIBLE);
                selectMic.setVisibility(View.INVISIBLE);

                String selectedChoice = selectText1.getText().toString(); // 선택지1 가져오기

                if (num < 6) {
                    // 현재 페이지 내용, 선택지 저장
                    pageContents.set(num - 1, storyTextView.getText().toString() + selectedChoice);

                    if (num < 5) {
                        makeStory.generateNextStoryPart(selectedChoice);
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
                nextStory.setVisibility(View.INVISIBLE);
                selectImage1.setVisibility(View.INVISIBLE);
                selectImage2.setVisibility(View.INVISIBLE);
                selectText1.setVisibility(View.INVISIBLE);
                selectText2.setVisibility(View.INVISIBLE);
                selectMic.setVisibility(View.INVISIBLE);

                String selectedChoice = selectText2.getText().toString(); // 선택지2 가져오기

                if (num < 6) {
                    // 현재 페이지 내용, 선택지 저장
                    pageContents.set(num - 1, storyTextView.getText().toString() + selectedChoice);

                    if (num < 5) {
                        makeStory.generateNextStoryPart(selectedChoice);
                    } else if (num == 5) {
                        makeStory.generateEndStoryPart(selectedChoice);
                    }
                    ++num;
                }
                if (num > 5) nextBtn.setVisibility(View.VISIBLE);
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

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (num == 6) {
                    pageContents.set(num -1, storyTextView.getText().toString());
                }
                if (isImageLoaded) {
                    byte[] imageBytes = (byte[]) nextBtn.getTag();
                    Intent intent = new Intent(MakeStoryActivity.this, MaketitleActivity.class);
                    intent.putExtra("backgroundImageBytes", imageBytes);
                    // 표지 제작할 때 테마 별로 구분하기 위한 용도
                    intent.putExtra("selectedTheme", finalSelectedTheme);
                    intent.putStringArrayListExtra("selectedCharacters", selectedCharacters);
                    saveStory(); // 내용 저장
                    startActivity(intent);
                } else {
                    showToast("이미지가 로드되지 않았습니다.");
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
                                    loading.getAnimation().cancel();
                                    loading.clearAnimation();
                                    loading.setVisibility(View.INVISIBLE);
                                    isImageLoaded = true;

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


    public void displayStoryText(final String storyText) {
        pageTextView.setText(num + " / 6");
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
            selectText1.setText(choices[0].trim());
            selectText2.setText(choices[1].trim());
        } else {
            showToast("선택지가 부족합니다.");
        }
        nextStory.setVisibility(View.VISIBLE);
        selectImage1.setVisibility(View.VISIBLE);
        selectImage2.setVisibility(View.VISIBLE);
        selectText1.setVisibility(View.VISIBLE);
        selectText2.setVisibility(View.VISIBLE);
        selectMic.setVisibility(View.VISIBLE);
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

            String fileName = user.getUid() + "_" + index + ".png";

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
}
