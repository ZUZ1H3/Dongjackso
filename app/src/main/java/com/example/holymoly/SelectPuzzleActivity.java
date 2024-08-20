package com.example.holymoly;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class SelectPuzzleActivity extends AppCompatActivity implements View.OnClickListener, UserInfoLoader {
    /* 상단 프로필 */
    private TextView name;
    private ImageView profile;
    private UserInfo userInfo = new UserInfo();

    /* DB */
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    private ImageView iv;
    private ImageButton btn3x3, btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_puzzle);

        /*
        // 상단 프로필 로딩
        name = findViewById(R.id.mini_name);
        profile = findViewById(R.id.mini_profile);
        loadUserInfo(profile, name);

        // Firebase 초기화
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        iv = findViewById(R.id.iv);
        btn3x3 = findViewById(R.id.btn3x3);
        btnStart = findViewById(R.id.btnStart);

        btn3x3.setOnClickListener(this);
        btnStart.setOnClickListener(this);

        String selectedImageUrl = getIntent().getStringExtra("selectedImageUrl");

        //Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        //iv.setImageBitmap(bitmap);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnStart) {
            Intent intent = new Intent(this, PuzzleActivity.class);
            startActivity(intent);
        }
        else if (v.getId() == R.id.btn3x3) {
            Toast.makeText(this, "3x3 버튼 클릭됨", Toast.LENGTH_SHORT).show();
        }
    }
         */
    }
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, PuzzleActivity.class);
        startActivity(intent);
    }
    @Override
    public void loadUserInfo(ImageView profile, TextView name) {
        userInfo.loadUserInfo(profile, name);
    }
}
