package com.example.holymoly;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class StartActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageButton start, nope; // 좋아, 안 할래 버튼
    private TextView name;
    private UserViewModel userViewModel;

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();
    private String uid = user.getUid();

    /* 효과음 */
    private SharedPreferences pref;
    private boolean isSoundOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        pref = getSharedPreferences("music", MODE_PRIVATE); // 효과음 초기화

        start = (ImageButton) findViewById(R.id.btn_good);
        nope = (ImageButton) findViewById(R.id.nope);
        name = (TextView) findViewById(R.id.tv_name);

        start.setOnClickListener(this);
        nope.setOnClickListener(this);

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userViewModel.loadName(uid);

        userViewModel.getNameLiveData().observe(this, userName -> {
            name.setText(userName);
        });
    }

    @Override
    public void onClick(View v) {
        sound();
        // 좋아! 클릭 시
        if(v.getId() == R.id.btn_good) {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        }
        // 안 할래 클릭 시
        if(v.getId() == R.id.nope) {
            stopService(new Intent(this, MusicService.class));
            finishAffinity();
        }
    }

    // 효과음
    public void sound() {
        isSoundOn = pref.getBoolean("on&off2", true);
        Intent intent = new Intent(this, SoundService.class);
        if (isSoundOn) startService(intent); // 효과음 on
        else stopService(intent);            // 효과음 off
    }
}
