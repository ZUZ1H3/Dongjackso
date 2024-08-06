package com.example.holymoly;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class TrophyActivity extends AppCompatActivity implements View.OnClickListener, UserInfoLoader{
    private TextView name;
    private ImageButton trophy, home, edit;
    private ImageView profile, spot1, high1;

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();
    StorageReference charcterRef = storageRef.child("characters/");

    private UserInfo userInfo = new UserInfo();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trophy);

        name = findViewById(R.id.mini_name);
        trophy = findViewById(R.id.ib_bictrophy);
        home = findViewById(R.id.ib_homebutton);
        edit = findViewById(R.id.ib_edit);
        profile = findViewById(R.id.mini_profile);
        spot1 = findViewById(R.id.iv_spot1);
       // high1 = findViewById(R.id.iv_high);

        loadUserInfo(profile, name);
        loadCharImage();

        trophy.setOnClickListener(this);
        home.setOnClickListener(this);
        edit.setOnClickListener(this);
    }
    public void onClick(View v) {
        if(v.getId() == R.id.ib_bictrophy) {
            Intent intent = new Intent(this, AchieveActivity.class);
            startActivity(intent);
        }
        else if(v.getId() == R.id.ib_homebutton) {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        }
        else if(v.getId() == R.id.ib_edit) {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void loadUserInfo(ImageView profile, TextView name) {
        userInfo.loadUserInfo(profile, name);
    }

    public void loadCharImage() {
        // 이미지 가져오기
        charcterRef.listAll().addOnSuccessListener(listResult -> {
            List<StorageReference> items = listResult.getItems();
            for (StorageReference item : items) {
                String img = item.getName();
                // 파일 이름이 현재 사용자의 ID로 시작하는 경우
                if (img.startsWith(user.getUid())) {
                    final long MEGABYTE = 1024 * 1024; // 1MB
                    item.getBytes(MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            spot1.setImageBitmap(bitmap);
                        }
                    });
                }
            }
        });
    }
}