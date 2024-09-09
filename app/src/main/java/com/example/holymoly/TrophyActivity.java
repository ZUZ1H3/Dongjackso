package com.example.holymoly;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class TrophyActivity extends AppCompatActivity implements View.OnClickListener, UserInfoLoader{
    /* 좌측 상단 프로필 초기화 */
    private TextView name, nickname;
    private ImageButton trophy, home, edit;
    private ImageView profile;
    private UserInfo userInfo = new UserInfo();

    /* firebase 초기화 */
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();

    // 캐릭터 생성 위치
    private ImageView spot1, spot2, spot3, spot4;
    // 이전 액티비티 정보
    private String from;
    private String[] activities = {"Home2Activity", "SelectGameActivity", "PuzzleActivity", "SelectPuzzleActivity"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trophy);

        name = findViewById(R.id.mini_name);
        nickname = findViewById(R.id.mini_nickname);
        trophy = findViewById(R.id.ib_bictrophy);
        home = findViewById(R.id.ib_homebutton);
        edit = findViewById(R.id.ib_edit);
        profile = findViewById(R.id.mini_profile);

        spot1 = findViewById(R.id.iv_spot1);
        spot2 = findViewById(R.id.iv_spot2);
        spot3 = findViewById(R.id.iv_spot3);
        spot4 = findViewById(R.id.iv_spot4);

        loadUserInfo(profile, name, nickname); // 미니 프로필 불러오기

        // 이전 액티비티 정보 받기
        from = getIntent().getStringExtra("from");

        StorageReference imgRef = storageRef.child("characters/" + user.getUid() + "_1.png");
        // 이미지 로드
        imgRef.getDownloadUrl().addOnSuccessListener(uri -> {
            if (nickname.getText().toString().equals("새내기 작가")) Glide.with(this).load(uri).into(spot2);     // 새내기 작가일 경우
            else if(nickname.getText().toString().equals("베테랑 작가")) Glide.with(this).load(uri).into(spot3); // 베테랑 작가일 경우
            else if(nickname.getText().toString().equals("마스터 작가")) Glide.with(this).load(uri).into(spot3); // 마스터 작가일 경우
            else Glide.with(this).load(uri).into(spot1); // 기본적으로 spot1에 캐릭터 위치
        });

        trophy.setOnClickListener(this);
        home.setOnClickListener(this);
        edit.setOnClickListener(this);
    }
    public void onClick(View v) {
        sound();
        if(v.getId() == R.id.ib_bictrophy) {
            Intent intent = new Intent(this, AchieveActivity.class);
            startActivity(intent);
        }
        else if(v.getId() == R.id.ib_homebutton) {
            Intent intent;
            if (!isInArray(from, activities)) {
                intent = new Intent(this, HomeActivity.class);
            } else {
                intent = new Intent(this, Home2Activity.class);
            }
            startActivity(intent);
        }
        else if(v.getId() == R.id.ib_edit) {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
        }
    }

    // 배열에서 값을 확인
    private boolean isInArray(String value, String[] array) {
        for (String item : array) {
            if (item.equals(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void loadUserInfo(ImageView profile, TextView name, TextView nickname) {
        userInfo.loadUserInfo(profile, name, nickname);
    }

    public void sound() {
        Intent intent = new Intent(this, SoundService.class);
        startService(intent);
    }
}