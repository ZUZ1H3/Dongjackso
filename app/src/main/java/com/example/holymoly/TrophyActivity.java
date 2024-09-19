package com.example.holymoly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class TrophyActivity extends AppCompatActivity implements View.OnClickListener, UserInfoLoader{
    /* 좌측 상단 프로필 초기화 */
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

    /* 효과음 */
    private SharedPreferences pref;
    private boolean isSoundOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trophy);
        pref = getSharedPreferences("music", MODE_PRIVATE); // 효과음 초기화

        spot1 = findViewById(R.id.iv_spot1);
        spot2 = findViewById(R.id.iv_spot2);
        spot3 = findViewById(R.id.iv_spot3);
        spot4 = findViewById(R.id.iv_spot4);


        // 이전 액티비티 정보 받기
        from = getIntent().getStringExtra("from");

        StorageReference imgRef = storageRef.child("characters/" + user.getUid() + "_1.png");
        // 이미지 로드
        /* imgRef.getDownloadUrl().addOnSuccessListener(uri -> {
            if (nickname.getText().toString().equals("새내기 작가")) Glide.with(this).load(uri).into(spot2);     // 새내기 작가
            else if(nickname.getText().toString().equals("베테랑 작가")) Glide.with(this).load(uri).into(spot3); // 베테랑 작가
            else if(nickname.getText().toString().equals("마스터 작가")) Glide.with(this).load(uri).into(spot4); // 마스터 작가
            else Glide.with(this).load(uri).into(spot1); // 기본적으로 spot1에 캐릭터 위치
        });*/

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

    @Override
    protected void onResume() {
        super.onResume();

        loadChangeImage();
    }

    private void loadChangeImage() {
        // 이미지 로드 전에 기존 이미지를 지우기
        Glide.with(this).clear(spot1);
        Glide.with(this).clear(spot2);
        Glide.with(this).clear(spot3);
        Glide.with(this).clear(spot4);

        StorageReference imgRef = storageRef.child("characters/" + user.getUid() + "_1.png");
        /*imgRef.getDownloadUrl().addOnSuccessListener(uri -> {
            if (nickname.getText().toString().equals("새내기 작가")) Glide.with(this).load(uri).into(spot2);     // 새내기 작가
            else if(nickname.getText().toString().equals("베테랑 작가")) Glide.with(this).load(uri).into(spot3); // 베테랑 작가
            else if(nickname.getText().toString().equals("마스터 작가")) Glide.with(this).load(uri).into(spot4); // 마스터 작가
            else Glide.with(this).load(uri).into(spot1); // 기본적으로 spot1에 캐릭터 위치
        });*/
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

    // 효과음
    public void sound() {
        isSoundOn = pref.getBoolean("on&off2", true);
        Intent intent = new Intent(this, SoundService.class);
        if (isSoundOn) startService(intent); // 효과음 on
        else stopService(intent);            // 효과음 off
    }
}