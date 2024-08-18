package com.example.holymoly;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MaketitleActivity extends AppCompatActivity {
    private ImageView backgroundImageView;
    private ImageButton nextBtn;
    private EditText title;
    private String bookTitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maketitle);

        backgroundImageView = findViewById(R.id.background_image_view);
        nextBtn = findViewById(R.id.ib_nextStep);
        title = findViewById(R.id.tv_booktitle);

        Intent intent = getIntent();
        byte[] imageBytes = intent.getByteArrayExtra("backgroundImageBytes");

        String selectedTheme = intent.getStringExtra("selectedTheme");

        if (imageBytes != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            if (bitmap != null) {
                backgroundImageView.setImageBitmap(bitmap);
            } else {
                Toast.makeText(this, "이미지 로드 실패", Toast.LENGTH_SHORT).show();
            }
        }

        bookTitle = title.getText().toString();
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MaketitleActivity.this, MakeBookcoverActivity.class);
                intent.putExtra("booktitle", bookTitle);
                intent.putExtra("selectedTheme", selectedTheme); // 작업이 완료되었을 때 MakeBookcoverActivity로 이동
                startActivity(intent);
            }
        });
    }
}

