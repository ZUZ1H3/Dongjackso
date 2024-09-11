package com.example.holymoly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
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

public class SelectPuzzleActivity extends AppCompatActivity implements View.OnClickListener, UserInfoLoader {
    /* 상단 프로필 */
    private TextView name, nickname;
    private ImageView profile;
    private ImageButton btnhome, btntrophy, btnsetting;
    private UserInfo userInfo = new UserInfo();

    /* DB */
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    private String selectedImage;
    private ImageView iv;
    private ImageButton btn3x3, btn4x4, btn5x5, btn6x6, btnStart, btnBack;

    private int rows, cols = 0;
    private ImageButton selectedButton;  // 현재 선택된 버튼을 추적

    /* 효과음 */
    private SharedPreferences pref;
    private boolean isSoundOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_puzzle);
        pref = getSharedPreferences("music", MODE_PRIVATE); // 효과음 초기화

        // 상단 프로필 로딩
        name = findViewById(R.id.mini_name);
        nickname = findViewById(R.id.mini_nickname);
        profile = findViewById(R.id.mini_profile);
        loadUserInfo(profile, name, nickname);

        // 우측 상단 미니 아이콘
        btnhome = findViewById(R.id.ib_homebutton);
        btntrophy = findViewById(R.id.ib_trophy);
        btnsetting = findViewById(R.id.ib_setting);
        
        // Firebase 초기화
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        iv = findViewById(R.id.iv_selectIV);
        btn3x3 = findViewById(R.id.btn_3x3);
        btn4x4 = findViewById(R.id.btn_4x4);
        btn5x5 = findViewById(R.id.btn_5x5);
        btn6x6 = findViewById(R.id.btn_6x6);
        btnStart = findViewById(R.id.btn_start);
        btnBack = findViewById(R.id.btn_back);

        btnhome.setOnClickListener(this);
        btntrophy.setOnClickListener(this);
        btnsetting.setOnClickListener(this);

        btn3x3.setOnClickListener(this);
        btn4x4.setOnClickListener(this);
        btn5x5.setOnClickListener(this);
        btn6x6.setOnClickListener(this);
        btnStart.setOnClickListener(this);
        btnBack.setOnClickListener(this);

        selectedImage = getIntent().getStringExtra("selectedImage");

        // Glide를 사용하여 이미지 로드
        Glide.with(this)
                .load(selectedImage)
                .into(iv);
    }

    @Override
    public void onClick(View v) {
        sound();
        if (v.getId() == R.id.btn_start) {
            if (rows == 0 && cols == 0) Toast.makeText(this, "단계를 클릭해 주세요.", Toast.LENGTH_SHORT).show();
            else startPuzzleActivity(rows, cols);
        }
        else if(v.getId() == R.id.btn_back) {
            finish();
        }
        else {
            // 선택된 버튼의 알파 값을 기본값으로 되돌리기
            if (selectedButton != null) {
                selectedButton.setAlpha(1f);
            }

            // 클릭된 버튼의 알파 값을 조정하고 rows, cols 값을 설정
            selectedButton = (ImageButton) v;
            selectedButton.setAlpha(0.4f);

            if (v.getId() == R.id.btn_3x3) {
                rows = 3;   cols = 3;
            }
            else if (v.getId() == R.id.btn_4x4) {
                rows = 4;   cols = 4;
            }
            else if (v.getId() == R.id.btn_5x5) {
                rows = 5;   cols = 5;
            }
            else if (v.getId() == R.id.btn_6x6) {
                rows = 6;   cols = 6;
            }
        }
        /* 상단 미니 아이콘 클릭 */
        if(v.getId() == R.id.ib_homebutton) {
            Intent intent = new Intent(this, Home2Activity.class);
            startActivity(intent);
        }
        else if(v.getId() == R.id.ib_trophy) {
            Intent intent = new Intent(this, TrophyActivity.class);
            intent.putExtra("from", "SelectPuzzleActivity");
            startActivity(intent);
        }
        else if(v.getId() == R.id.ib_setting) {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
        }
    }

    private void startPuzzleActivity(int rows, int cols) {
        Intent intent = new Intent(this, StartPuzzleActivity.class);
        intent.putExtra("rows", rows);
        intent.putExtra("cols", cols);
        intent.putExtra("selectedImage", selectedImage);
        startActivity(intent);
        finish();
    }
    
    @Override
    public void loadUserInfo(ImageView profile, TextView name, TextView nickname) {
        userInfo.loadUserInfo(profile, name, nickname);
    }

    // 효과음
    public void sound() {
        isSoundOn = pref.getBoolean("on&off2", true);
        Intent intent = new Intent(this, SoundService.class);
        if (isSoundOn) startService(intent); // 효과음 on
        else stopService(intent);            // 효과음 off
    }
}
