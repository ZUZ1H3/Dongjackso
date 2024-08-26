package com.example.holymoly;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;


public class AlbumDiaryActivity extends AppCompatActivity implements View.OnClickListener{
    private TextView calendar, leftTV, rightTV;
    private ImageView leftWT, rightWT, leftIV, rightIV;
    private ImageButton stop;
    private long backPressedTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_diary);

        calendar = findViewById(R.id.calendar);
        leftTV = findViewById(R.id.tv_left);
        rightTV = findViewById(R.id.tv_right);
        leftWT = findViewById(R.id.leftWeather);
        rightWT = findViewById(R.id.rightWeather);
        leftIV = findViewById(R.id.leftImage);
        rightIV = findViewById(R.id.rightImage);
        stop = findViewById(R.id.ib_stopReading);

        stop.setOnClickListener(this);

        // 현재 날짜를 구함
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH) + 1;

        calendar.setText(month + "월");
    }

    @Override
    public void onClick(View v) {
        sound();
        if(v.getId() == R.id.ib_backStep) { }        // 이전 페이지로
        else if(v.getId() == R.id.ib_nextStep) { }  // 다음 페이지로
        else if(v.getId() == R.id.ib_stopReading) { // 그만 읽기
            if (System.currentTimeMillis() - backPressedTime >= 2000) {
                backPressedTime = System.currentTimeMillis();
                Toast.makeText(this, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
            } else {
                finish(); // 현재 액티비티 종료
            }
        }
    }

    // 효과음
    public void sound() {
        Intent intent = new Intent(this, SoundService.class);
        startService(intent);
    }
}