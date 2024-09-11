package com.example.holymoly;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SelectGameActivity extends AppCompatActivity {
    private ImageButton btnhome, btntrophy, btnsetting;
    private ImageButton ibSelectBingo, ibSelectPuzzle;
    private ImageView profile;
    private TextView name, nickname;

    private UserInfo userInfo = new UserInfo();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectgame);

        name = findViewById(R.id.mini_name);
        nickname = findViewById(R.id.mini_nickname);
        profile = findViewById(R.id.mini_profile);

        btnhome = findViewById(R.id.ib_homebutton);
        btntrophy = findViewById(R.id.ib_trophy);
        btnsetting = findViewById(R.id.ib_setting);
        ibSelectBingo = findViewById(R.id.ib_selectBingo);
        ibSelectPuzzle = findViewById(R.id.ib_selectPuzzle);

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

    public void sound() {
        Intent intent = new Intent(this, SoundService.class);
        startService(intent);
    }
}
