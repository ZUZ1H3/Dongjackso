package com.example.holymoly;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class Home2Activity extends AppCompatActivity implements UserInfoLoader {
    private ImageButton btntrophy, btnsetting, btnmaking, btnalbum, btngame, btnfairy;
    private ImageView profile;
    private TextView name;

    private UserInfo userInfo = new UserInfo();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home2);

        name = findViewById(R.id.mini_name);
        profile = findViewById(R.id.mini_profile);
        btntrophy = findViewById(R.id.ib_trophy);
        btnsetting = findViewById(R.id.ib_setting);
        btnmaking = findViewById(R.id.ib_making);
        btnalbum = findViewById(R.id.ib_album);
        btngame = findViewById(R.id.ib_game);
        btnfairy = findViewById(R.id.ib_diaryHome);


        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home2Activity.this, RegistrationActivity.class);
                startActivity(intent);
            }
        });

        btntrophy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home2Activity.this, TrophyActivity.class);
                startActivity(intent);
            }
        });

        btnsetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home2Activity.this, SettingActivity.class);
                startActivity(intent);
            }
        });

        btnmaking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home2Activity.this, DiaryActivity.class);
                startActivity(intent);
            }
        });

        btnalbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(Home2Activity.this, AlbumActivity.class);
                //startActivity(intent);
            }
        });

        btngame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home2Activity.this, PuzzleActivity.class);
                startActivity(intent);
            }
        });

        btnfairy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home2Activity.this, HomeActivity.class);
                startActivity(intent);
            }
        });
    }
    public void onStart() {
        super.onStart();
        loadUserInfo(profile, name);
    }

    @Override
    public void loadUserInfo(ImageView profile, TextView name) {
        userInfo.loadUserInfo(profile, name);
    }
}