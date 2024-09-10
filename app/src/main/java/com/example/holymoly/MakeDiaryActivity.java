package com.example.holymoly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class MakeDiaryActivity extends AppCompatActivity {
    private String story, name, generateStory;
    private ImageButton stopMakingBtn;
    private Gemini gemini = new Gemini();
    private TextView storyTextView, day;
    private long backPressedTime = 0;
    private ImageView backgroundimageview;

    private boolean isGenerated = false;

    private RadioGroup radioGroup;

    /* firebase 초기화 */
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();
    private String uid = user.getUid();

    /* 효과음 */
    private SharedPreferences pref;
    private boolean isSoundOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_diary);
        pref = getSharedPreferences("music", MODE_PRIVATE); // 효과음 초기화

        Intent intent = getIntent();
        story = intent.getStringExtra("story");
        name = intent.getStringExtra("name");

        storyTextView = findViewById(R.id.storyTextView);
        storyTextView.setMovementMethod(new ScrollingMovementMethod());
        stopMakingBtn = findViewById(R.id.ib_stopMaking);
        day = findViewById(R.id.dayTextView);
        backgroundimageview= findViewById(R.id.background_image_view);
        radioGroup = findViewById(R.id.radioGroup);

        String currentDate = getCurrentDate();
        String date = getIntent().getStringExtra("date");

        day.setText(currentDate);

        if (date != null) {
            loadImage(date);   // 이미지 불러오기
            loadTxt(date);     // 텍스트 불러오기
            loadWeather(date); // 날씨 불러오기
        }
        if (story != null) {
            if(!isGenerated) {
                generateFairyTale(story);
            }
            else{
                storyTextView.setText(generateStory);
            }
        } else {
            storyTextView.setText("스토리가 없습니다.");
        }

        stopMakingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound();
                if (System.currentTimeMillis() - backPressedTime >= 2000) {
                    backPressedTime = System.currentTimeMillis();
                    Toast.makeText(MakeDiaryActivity.this, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
                    int selected = radioGroup.getCheckedRadioButtonId();
                    intent.putExtra("selected", selected);
                    Intent intent = new Intent(MakeDiaryActivity.this, AlbumDiaryActivity.class);
                    startActivity(intent);
                } else {
                    finish();
                }
            }
        });
        backgroundimageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound();
                Intent intent = new Intent(MakeDiaryActivity.this, MakeDrowDiaryActivity.class);
                startActivity(intent);
            }
        });

        // 라디오 버튼 선택 리스너
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                sound();
                saveWeather(date, checkedId);
            }
        });
    }

    private void generateFairyTale(String story) {
        String prompt = "어린이가 오늘 있었던 일을 주제로 이야기 한 내역을 제공해드리겠습니다." +
                "이 이야기를 바탕으로, 동화 형태로 다듬어 어린이가 주인공인 일기 동화를 만들어주세요. " +
                "이야기는 현실적인 내용이지만, 약간의 과장을 해도 좋습니다. " +
                "판타지 등 추가적인 요소를 과하지 않게 섞는 것도 좋습니다. " +
                "제목은 만들지 마세요. 줄바꿈을 하지 마세요. 따옴표를 사용하여 대사를 만들지 마세요. "+
                "어린이의 이름은 '" + name + "' 입니다.\n" +
                "다음은 어린아이가 얘기한 내용 입니다. :" + story;

        gemini.generateText(prompt, new Gemini.Callback() {
            @Override
            public void onSuccess(String text) {
                runOnUiThread(() -> {
                    storyTextView.setText(text);
                    isGenerated = true;
                    generateStory = text;
                    saveTxt(); // 글 저장
                });
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() -> {
                    storyTextView.setText("동화 생성에 실패했습니다.");
                });
                Log.e("MakeDiaryActivity", "동화 생성 실패", t);
            }
        });
    }

    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("M월 d일 E", Locale.KOREAN);
        Date date = new Date();
        return dateFormat.format(date);
    }

    // 저장된 날짜를 M월 d일 E로 반환
    private String getDate(String date) {
        try {
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
            SimpleDateFormat targetFormat = new SimpleDateFormat("M월 d일 E", Locale.KOREAN);
            Date parsedDate = originalFormat.parse(date);
            return targetFormat.format(parsedDate);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    // 텍스트 업로드
    private void saveTxt() {
        String current = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        String fileName = uid + "_" + current + ".txt";
        StorageReference fileRef = storageRef.child("diaries/" + fileName);
        String content = storyTextView.getText().toString();

        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("text/plain")
                .build();

        UploadTask uploadTask = fileRef.putBytes(content.getBytes(), metadata);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(this, "텍스트 파일 업로드 성공", Toast.LENGTH_SHORT).show();
        });
    }

    // 텍스트 불러오기
    private void loadTxt(String date) {
        String fileName = uid + "_" + date + ".txt";
        StorageReference fileRef = storageRef.child("diaries/" + fileName);

        fileRef.getBytes(Long.MAX_VALUE) // 파일 전체를 메모리로 가져옴
                .addOnSuccessListener(bytes -> {
                    // 파일을 읽어 텍스트로 변환
                    String text = new String(bytes);
                    storyTextView.setText(text);

                    // 불러온 날짜로 설정
                    String loadDate = getDate(date);
                    day.setText(loadDate);
                });
    }
    // 이미지 불러오기
    private void loadImage(String date) {
        String fileName = uid + "_" + date + ".png";
        StorageReference imgRef = storageRef.child("diaries/" + fileName);

        imgRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    // 다운로드 URL을 가져와서 Glide로 이미지 로드
                    if (backgroundimageview != null) {
                        Glide.with(this).load(uri).into(backgroundimageview);
                    }
                });
    }
    // 날씨 저장
    private void saveWeather(String date, int selectedId) {
        DocumentReference weatherRef = db.collection("weather").document(uid).collection("dates").document(date);

        // 데이터 저장
        weatherRef.set(Collections.singletonMap("selectedId", selectedId))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MakeDiaryActivity.this, "날씨 정보 저장", Toast.LENGTH_SHORT).show();
                });
    }

    // 날씨 불러오기
    private void loadWeather(String date) {
        DocumentReference weatherRef = db.collection("weather").document(uid).collection("dates").document(date);

        weatherRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            int selectedId = document.getLong("selectedId").intValue();
                            RadioButton savedButton = findViewById(selectedId);
                            if (savedButton != null) {
                                savedButton.setChecked(true);
                            }
                        }
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