package com.example.holymoly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class SelectGameActivity extends AppCompatActivity implements UserInfoLoader {
    private ImageButton btnhome, btntrophy, btnsetting;
    private ImageButton ibSelectBingo, ibSelectPuzzle;
    private ImageView profile, userImage, userImage2;
    private TextView name, nickname;

    private UserInfo userInfo = new UserInfo();

    /* DB */
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();

    /* 효과음 */
    private SharedPreferences pref;
    private boolean isSoundOn;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectgame);
        pref = getSharedPreferences("music", MODE_PRIVATE); // 효과음 초기화

        name = findViewById(R.id.mini_name);
        nickname = findViewById(R.id.mini_nickname);
        profile = findViewById(R.id.mini_profile);
        userImage = findViewById(R.id.userImage);
        userImage2 = findViewById(R.id.userImage2);

        btnhome = findViewById(R.id.ib_homebutton);
        btntrophy = findViewById(R.id.ib_trophy);
        btnsetting = findViewById(R.id.ib_setting);
        ibSelectBingo = findViewById(R.id.ib_selectBingo);
        ibSelectPuzzle = findViewById(R.id.ib_selectPuzzle);

        loadUserInfo(profile, name, nickname);

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound();
                Intent intent = new Intent(SelectGameActivity.this, RegistrationActivity.class);
                startActivity(intent);
            }
        });

        btnhome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound();
                Intent intent = new Intent(SelectGameActivity.this, Home2Activity.class);
                startActivity(intent);
            }
        });

        btntrophy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound();
                Intent intent = new Intent(SelectGameActivity.this, TrophyActivity.class);
                intent.putExtra("from", "SelectGameActivity");
                startActivity(intent);

            }
        });

        btnsetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound();
                Intent intent = new Intent(SelectGameActivity.this, SettingActivity.class);
                intent.putExtra("from", "SelectGameActivity");
                startActivity(intent);
            }
        });

        ibSelectBingo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound();
                // 버튼 스타일 변경
                ibSelectBingo.setBackgroundResource(R.drawable.ib_selectgame_bingo_checked);
                ibSelectPuzzle.setBackgroundResource(R.drawable.ib_selectgame_puzzle);

                Intent intent = new Intent(SelectGameActivity.this, WordGameThemeActivity.class);
                startActivity(intent);
            }
        });

        ibSelectPuzzle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound();
                // 버튼 스타일 변경
                ibSelectBingo.setBackgroundResource(R.drawable.ib_selectgame_bingo);
                ibSelectPuzzle.setBackgroundResource(R.drawable.ib_selectgame_puzzle_checked);

                Intent intent = new Intent(SelectGameActivity.this, PuzzleActivity.class);
                startActivity(intent);
            }
        });

        // 이미지 불러오기
        StorageReference imgRef = storageRef.child("characters/" + user.getUid() + ".png");
        imgRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(this)
                    .asBitmap()
                    .load(uri)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            Bitmap bitmap = createShadow(resource);
                            userImage.setImageBitmap(bitmap); // ImageView에 설정
                            userImage2.setImageBitmap(bitmap);
                        }

                        @Override
                        public void onLoadCleared(Drawable placeholder) {}
                    });
        });
    }

    // 그림자 생성
    private Bitmap createShadow(Bitmap bitmap) {
        // 기존 이미지 크기와 동일한 새 Bitmap 생성
        Bitmap shadowBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(shadowBitmap);

        // 그림자 효과를 위한 Paint 설정
        Paint shadowPaint = new Paint();
        shadowPaint.setAntiAlias(true);    // 경계선이 부드럽게
        shadowPaint.setColor(Color.BLACK); // 그림자 색상
        shadowPaint.setAlpha(150);         // 투명도 설정
        shadowPaint.setMaskFilter(new BlurMaskFilter(15f, BlurMaskFilter.Blur.NORMAL));

        // 그림자 그리기
        canvas.drawBitmap(bitmap.extractAlpha(), 10, 15, shadowPaint);
        // 원본 이미지 그리기
        canvas.drawBitmap(bitmap, 0, 0, null);

        return shadowBitmap;
    }

    @Override
    public void loadUserInfo(ImageView profile, TextView name, TextView nickname) {
        userInfo.loadUserInfo(profile, name, nickname);
    }

    // 효과음
    public void sound() {
        isSoundOn = pref.getBoolean("on&off2", true);
        Intent intent = new Intent(this, SoundService.class);
        if (isSoundOn) startService(intent); // 효과음 on
        else stopService(intent);            // 효과음 off
    }
}
