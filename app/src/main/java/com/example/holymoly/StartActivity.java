package com.example.holymoly;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class StartActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageButton start, nope; // 좋아, 안 할래 버튼
    private TextView name;
    private UserInfo userInfo = new UserInfo();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        start = (ImageButton) findViewById(R.id.btn_good);
        nope = (ImageButton) findViewById(R.id.nope);
        name = (TextView) findViewById(R.id.tv_name);

        start.setOnClickListener(this);
        nope.setOnClickListener(this);

        // 작가 이름 설정
        name.setText(userInfo.getName());
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
