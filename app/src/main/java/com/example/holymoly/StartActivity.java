package com.example.holymoly;

import android.content.Intent;
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

        // Firestore에서 사용자 이름 가져오기
        db.collection("users").document(user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String userName = document.getString("name");
                            name.setText(userName);
                        }
                    }
                });
    }
    @Override
    public void onClick(View v) {
        // 좋아! 클릭 시
        if(v.getId() == R.id.btn_good) {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        }
        // 안 할래 클릭 시
        if(v.getId() == R.id.nope) finishAffinity();
    }
}
