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
    private TextView name, nickname;
    private ImageButton trophy, home, edit;
    private ImageView profile;

    // 캐릭터 생성 위치
    private ImageView spot1, spot2, spot3, spot4;

    private UserInfo userInfo = new UserInfo();

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();

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

        StorageReference imgRef = storageRef.child("characters/" + user.getUid() + "_1.png");
        // 이미지 로드
        imgRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(this).load(uri).into(spot1); // 첫 번째 위치에 캐릭터 생성
            // Glide.with(this).load(uri).into(spot2); // 두 번째 위치에 캐릭터 생성
            // Glide.with(this).load(uri).into(spot3); // 세 번째 위치에 캐릭터 생성
            // Glide.with(this).load(uri).into(spot4); // 네 번째 위치에 캐릭터 생성
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
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        }
        else if(v.getId() == R.id.ib_edit) {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
        }
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