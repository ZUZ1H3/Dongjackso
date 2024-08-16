package com.example.holymoly;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class MaketitleActivity extends AppCompatActivity {
    private ImageView backgroundImageView;
    private ImageButton nextBtn;
    private EditText title;
    private String bookTitle;
    private TextView name;
    private String selectedTheme;
    private ArrayList<String> selectedCharacters;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maketitle);

        backgroundImageView = findViewById(R.id.background_image_view);
        nextBtn = findViewById(R.id.ib_nextStep);
        title = findViewById(R.id.tv_booktitle);
        name = findViewById(R.id.tv_writername);

        Intent intent = getIntent();
        byte[] imageBytes = intent.getByteArrayExtra("backgroundImageBytes");
        selectedTheme = intent.getStringExtra("selectedTheme");
        selectedCharacters = intent.getStringArrayListExtra("selectedCharacters");

        if (imageBytes != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            if (bitmap != null) {
                backgroundImageView.setImageBitmap(bitmap);
            } else {
                Toast.makeText(this, "이미지 로드 실패", Toast.LENGTH_SHORT).show();
            }
        }

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bookTitle = title.getText().toString(); // 클릭 시점에 가져오기
                Intent intent = new Intent(MaketitleActivity.this, MakeBookcoverActivity.class);
                intent.putExtra("bookTitle", bookTitle); // bookTitle 키를 사용해야 함
                intent.putStringArrayListExtra("selectedCharacters", selectedCharacters);
                intent.putExtra("selectedTheme", selectedTheme);
                startActivity(intent);
            }
        });

        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String userName = document.getString("name");
                        name.setText(userName);
                    }
                });
    }
}
