package com.example.holymoly;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SelectversionActivity extends AppCompatActivity implements UserInfoLoader {
    private ImageButton btnhome, btntrophy, btnsetting;
    private ImageButton ibMakeWithAI, ibMakeAlone;
    private TextView name;
    private ImageView profile;
    private UserInfo userInfo = new UserInfo();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectversion);

        name = findViewById(R.id.mini_name);
        profile = findViewById(R.id.mini_profile);
        ibMakeWithAI = findViewById(R.id.ib_makeWithAI);
        ibMakeAlone = findViewById(R.id.ib_makeWithAlone);

        btnhome = findViewById(R.id.ib_homebutton);
        btntrophy = findViewById(R.id.ib_trophy);
        btnsetting = findViewById(R.id.ib_setting);

        loadUserInfo(profile, name);

        btnhome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound();
                Intent intent = new Intent(SelectversionActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        btntrophy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound();
                Intent intent = new Intent(SelectversionActivity.this, TrophyActivity.class);
                startActivity(intent);
            }
        });

        btnsetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound();
                Intent intent = new Intent(SelectversionActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });

        ibMakeWithAI.setOnClickListener(v -> {
            sound();
            // 버튼 스타일 변경 (테두리 및 색상 추가)
            ibMakeWithAI.setBackgroundResource(R.drawable.ib_version_makewithai_checked);
            ibMakeAlone.setBackgroundResource(R.drawable.ib_version_makealone);

            // 다른 액티비티로 전환
            Intent intent = new Intent(SelectversionActivity.this, SelectthemaActivity.class);
            startActivity(intent);
        });

        ibMakeAlone.setOnClickListener(v -> {
            sound();
            // 버튼 스타일 변경 (테두리 및 색상 추가)
            ibMakeAlone.setBackgroundResource(R.drawable.ib_version_makealone_checked);
            ibMakeWithAI.setBackgroundResource(R.drawable.ib_version_makewithai);

            // 다른 액티비티로 전환
            Intent intent = new Intent(SelectversionActivity.this, HomeActivity.class);
            startActivity(intent);
        });
    }


    @Override
    public void loadUserInfo(ImageView profile, TextView name) {
        userInfo.loadUserInfo(profile, name);
    }
    public void sound() {
        Intent intent = new Intent(this, SoundService.class);
        startService(intent);
    }


}
