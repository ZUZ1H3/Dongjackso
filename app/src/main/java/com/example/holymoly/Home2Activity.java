package com.example.holymoly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Home2Activity extends AppCompatActivity implements UserInfoLoader {
    private ImageButton btntrophy, btnsetting, btnmaking, btnalbum, btngame, btnfairy;
    private ImageView profile;
    private TextView name, nickname;

    private UserInfo userInfo = new UserInfo();
    /* 효과음 */
    private SharedPreferences pref;
    private boolean isSoundOn;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home2);
        pref = getSharedPreferences("music", MODE_PRIVATE); // 효과음 초기화

        name = findViewById(R.id.mini_name);
        nickname = findViewById(R.id.mini_nickname);
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
                sound();
                Intent intent = new Intent(Home2Activity.this, RegistrationActivity.class);
                startActivity(intent);
            }
        });

        btntrophy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound();
                Intent intent = new Intent(Home2Activity.this, TrophyActivity.class);
                intent.putExtra("from", "Home2Activity");
                startActivity(intent);
            }
        });

        btnsetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound();
                Intent intent = new Intent(Home2Activity.this, SettingActivity.class);
                intent.putExtra("from", "Home2Activity");
                startActivity(intent);
            }
        });

        btnmaking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound();
                Intent intent = new Intent(Home2Activity.this, DiaryActivity.class);
                startActivity(intent);
            }
        });

        btnalbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound();
                Intent intent = new Intent(Home2Activity.this, AlbumDiaryActivity.class);
                startActivity(intent);
            }
        });

        btngame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound();
                Intent intent = new Intent(Home2Activity.this, SelectGameActivity.class);
                startActivity(intent);
            }
        });

        btnfairy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound();
                Intent intent = new Intent(Home2Activity.this, HomeActivity.class);
                startActivity(intent);
            }
        });
    }

    public void onStart() {
        super.onStart();
        loadUserInfo(profile, name, nickname);
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