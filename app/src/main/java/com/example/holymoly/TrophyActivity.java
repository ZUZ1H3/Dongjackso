package com.example.holymoly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class TrophyActivity extends AppCompatActivity implements View.OnClickListener{
    /* firebase 초기화 */
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();
    private String uid = user.getUid();
    private StorageReference imgRef = storageRef.child("characters/" + user.getUid() + "_1.png");

    // 작가 호칭 liveData
    private UserViewModel userViewModel;

    // 캐릭터 생성 위치
    private ImageView spot1, spot2, spot3, spot4;
    private ImageButton trophy, stopMaking;

    /* 효과음 */
    private SharedPreferences pref;
    private boolean isSoundOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trophy);
        pref = getSharedPreferences("music", MODE_PRIVATE); // 효과음 초기화

        // livedata에서 nickname 갖고 옴
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userViewModel.loadNickname(uid);

        spot1 = findViewById(R.id.iv_spot1);
        spot2 = findViewById(R.id.iv_spot2);
        spot3 = findViewById(R.id.iv_spot3);
        spot4 = findViewById(R.id.iv_spot4);

        trophy = findViewById(R.id.ib_bigtrophy);
        stopMaking = findViewById(R.id.ib_stopMaking);
        stopMaking.setOnClickListener(this);
        trophy.setOnClickListener(this);

        loadChangeImage();
    }
    public void onClick(View v) {
        sound();
        if(v.getId() == R.id.ib_bigtrophy) {
            Intent intent = new Intent(this, AchieveActivity.class);
            startActivity(intent);
            finish();
        }
        else if(v.getId() == R.id.ib_stopMaking) finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChangeImage();
    }

    private void loadCharacter(String nickname) {
        imgRef.getDownloadUrl().addOnSuccessListener(uri -> {
            if("꼬마 작가".equals(nickname)) Glide.with(this).load(uri).into(spot1); // 기본적으로 spot1에 캐릭터 위치
            else if ("새내기 작가".equals(nickname)) Glide.with(this).load(uri).into(spot2); // 새내기 작가
            else if ("베테랑 작가".equals(nickname)) Glide.with(this).load(uri).into(spot3); // 베테랑 작가
            else if ("마스터 작가".equals(nickname)) Glide.with(this).load(uri).into(spot4); // 마스터 작가
        });
    }

    private void loadChangeImage() {
        // 이미지 로드 전에 기존 이미지를 지우기
        Glide.with(this).clear(spot1);
        Glide.with(this).clear(spot2);
        Glide.with(this).clear(spot3);
        Glide.with(this).clear(spot4);

        userViewModel.getNicknameLiveData().observe(this, nickname -> {
            loadCharacter(nickname);
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