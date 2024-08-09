package com.example.holymoly;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class MakeBookcoverActivity extends AppCompatActivity {

    private CustomView drawView;
    private ImageButton pen, erase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_bookcover);

        pen = findViewById(R.id.ib_pen);
        erase = findViewById(R.id.ib_erase);
        drawView = (CustomView) findViewById(R.id.drawing);

        erase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 지우개 버튼 클릭 시 흰색으로 색상 변경
                drawView.setColor("#FFFFFFFF");
            }
        });

        pen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 펜 버튼 클릭 시 검정색으로 색상 변경
                drawView.setColor("#FF000000");
            }
        });
    }
}
