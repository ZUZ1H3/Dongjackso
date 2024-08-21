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
import com.google.firebase.firestore.DocumentSnapshot;
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

        // 로컬 SharedPreferences에서 사용자 이름을 가져옴
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String namePref = prefs.getString(KEY_USER_NAME, null);

        if (namePref != null) { // 저장된 이름이 있으면 텍스트뷰에 바로 설정
            name.setText(namePref);
        }
        else if (user != null) { // 저장된 이름이 없으면 사용자 이름을 Firestore에서 가져옴
            db.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String userName = document.getString("name");
                            if (userName != null) {
                                // 사용자 이름을 SharedPreferences에 저장
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString(KEY_USER_NAME, userName);
                                editor.apply();

                                // 텍스트뷰에 설정
                                name.setText(userName);
                            }
                        }
                    });
        }
    }
    @Override
    public void onClick(View v) {
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
}
