package com.example.holymoly;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class StartActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageButton start, nope; // 좋아, 안 할래 버튼
    private TextView name;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore db;

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_USER_NAME = "userPref";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        start = (ImageButton) findViewById(R.id.btn_good);
        nope = (ImageButton) findViewById(R.id.nope);
        name = (TextView) findViewById(R.id.tv_name);

        start.setOnClickListener(this);
        nope.setOnClickListener(this);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        checkUserName();
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
    // 저장된 사용자 이름이 현재 로그인한 사용자와 같은지 확인하고 필요시 업데이트
    private void checkUserName() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedUserName = prefs.getString(KEY_USER_NAME, null);

        // 현재 로그인한 사용자의 정보를 가져옴
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String currentUserName = document.getString("name");
                        if (currentUserName != null) {
                            // 저장된 이름과 현재 사용자 이름이 다르면 업데이트
                            if (savedUserName == null || !savedUserName.equals(currentUserName)) {
                                // SharedPreferences에 현재 사용자 이름 저장
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString(KEY_USER_NAME, currentUserName);
                                editor.apply(); // 변경사항 적용

                                // 텍스트뷰에 새로운 사용자 이름 설정
                                name.setText(currentUserName);
                            } else {
                                // 저장된 사용자 이름이 현재 사용자 이름과 같으면 그대로 사용
                                name.setText(savedUserName);
                            }
                        }
                    }
                });
    }
    // 효과음
    public void sound() {
        Intent intent = new Intent(this, SoundService.class);
        startService(intent);
    }
}
