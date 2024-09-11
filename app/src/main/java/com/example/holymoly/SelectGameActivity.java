package com.example.holymoly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SelectGameActivity extends AppCompatActivity implements UserInfoLoader {
    private ImageButton btnhome, btntrophy, btnsetting;
    private ImageButton ibSelectBingo, ibSelectPuzzle;
    private ImageView profile;
    private TextView name, nickname;

    private UserInfo userInfo = new UserInfo();

    /* 효과음 */
    private SharedPreferences pref;
    private boolean isSoundOn;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectgame);
        pref = getSharedPreferences("music", MODE_PRIVATE); // 효과음 초기화

        name = findViewById(R.id.mini_name);
        nickname = findViewById(R.id.mini_nickname);
        profile = findViewById(R.id.mini_profile);

        btnhome = findViewById(R.id.ib_homebutton);
        btntrophy = findViewById(R.id.ib_trophy);
        btnsetting = findViewById(R.id.ib_setting);
        ibSelectBingo = findViewById(R.id.ib_selectBingo);
        ibSelectPuzzle = findViewById(R.id.ib_selectPuzzle);

        loadUserInfo(profile, name, nickname);

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound();
                Intent intent = new Intent(SelectGameActivity.this, RegistrationActivity.class);
                startActivity(intent);
            }
        });

        btnhome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound();
                Intent intent = new Intent(SelectGameActivity.this, Home2Activity.class);
                startActivity(intent);
            }
        });

        btntrophy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound();
                Intent intent = new Intent(SelectGameActivity.this, TrophyActivity.class);
                intent.putExtra("from", "SelectGameActivity");
                startActivity(intent);

            }
        });

        btnsetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound();
                Intent intent = new Intent(SelectGameActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });

        ibSelectBingo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound();
                Intent intent = new Intent(SelectGameActivity.this, WordGameReadyActivity.class);
                startActivity(intent);
            }
        });

        ibSelectPuzzle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound();
                Intent intent = new Intent(SelectGameActivity.this, PuzzleActivity.class);
                startActivity(intent);
            }
        });
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
