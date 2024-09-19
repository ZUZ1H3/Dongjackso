package com.example.holymoly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText name, age;
    private RadioButton rb_bgm_on, rb_bgm_off, rb_sound_on, rb_sound_off;
    private CircleImageView profile;
    private ImageButton logout, pwdEdit, ok;
    private ConstraintLayout background;

    /* DB */
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private StorageReference characterRef;

    private Spinner genderSpinner;
    private ArrayAdapter<String> adapter;

    /* 효과음 */
    private SharedPreferences pref;
    private boolean isBgmOn, isSoundOn;

    // 이전 액티비티 정보
    private String from;
    private String[] activities = {"Home2Activity", "SelectGameActivity", "PuzzleActivity", "SelectPuzzleActivity"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        background = findViewById(R.id.background);
        name = (EditText) findViewById(R.id.et_name);
        age = (EditText) findViewById(R.id.et_age);
        profile = (CircleImageView) findViewById(R.id.img_profile);
        logout = (ImageButton) findViewById(R.id.ib_logout);
        pwdEdit = (ImageButton) findViewById(R.id.ib_pwdedit);
        ok = (ImageButton) findViewById(R.id.ib_ok);
        genderSpinner = findViewById(R.id.spinner_gender);

        profile.setOnClickListener(this);
        logout.setOnClickListener(this);
        pwdEdit.setOnClickListener(this);
        ok.setOnClickListener(this);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        characterRef = storageRef.child("characters/" + user.getUid() + ".png");

        pref = getSharedPreferences("music", MODE_PRIVATE);
        isBgmOn = pref.getBoolean("on&off", true); // 기본값 켜짐
        isSoundOn = pref.getBoolean("on&off2", true); // 기본값 켜짐

        // 이전 액티비티 정보 받기
        from = getIntent().getStringExtra("from");
        if(isInArray(from, activities)) {
            background.setBackgroundResource(R.drawable.bg_main2);
            ok.setBackgroundResource(R.drawable.ic_ok3);
            logout.setBackgroundResource(R.drawable.ic_logout2);
            pwdEdit.setBackgroundResource(R.drawable.ic_pwdedit2);
            genderSpinner.setPopupBackgroundDrawable(new ColorDrawable(Color.parseColor("#1F2C6F")));
        }

        // 사용자 기존 정보 로딩
        loadUserInfo();

        String[] genderArray = getResources().getStringArray(R.array.gender_array);
        String[] genderWithPrompt = new String[genderArray.length];
        System.arraycopy(genderArray, 0, genderWithPrompt, 0, genderArray.length);

        adapter = new ArrayAdapter<String>(this, R.layout.db_spinner_item, genderWithPrompt);

        adapter.setDropDownViewResource(R.layout.db_spinner_item);
        genderSpinner.setAdapter(adapter);
        genderSpinner.setSelection(0); // 기본 선택 항목을 '성별'로 설정

        // 배경 음악 라디오 그룹 설정
        RadioGroup rgBgm = findViewById(R.id.rg_bgm);
        rb_bgm_on = findViewById(R.id.rb_bgm_on);
        rb_bgm_off = findViewById(R.id.rb_bgm_off);

        // 효과음 라디오 그룹 설정
        RadioGroup rgSound = findViewById(R.id.rg_sound);
        rb_sound_on = findViewById(R.id.rb_sound_on);
        rb_sound_off = findViewById(R.id.rb_sound_off);

        // 배경음 라디오 버튼 기본 선택값 설정
        rb_bgm_on.setChecked(isBgmOn);
        rb_bgm_off.setChecked(!isBgmOn);

        // 효과음 라디오 버튼 기본 선택값 설정
        rb_sound_on.setChecked(isSoundOn);
        rb_sound_off.setChecked(!isSoundOn);

        // 배경 음악 라디오 그룹 리스너 설정
        rgBgm.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checked) {
                if (checked == R.id.rb_bgm_on) {
                    startService(new Intent(SettingActivity.this, MusicService.class));
                } else if (checked == R.id.rb_bgm_off) {
                    stopService(new Intent(SettingActivity.this, MusicService.class));
                }
            }
        });
        // 상태에 따라 음악 서비스 제어
        if (isBgmOn) {
            startService(new Intent(this, MusicService.class));
        }

        // 효과음 라디오 그룹 리스너 설정
        rgSound.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checked) {
                if (checked == R.id.rb_sound_on) isSoundOn = true;
                else isSoundOn = false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        sound();
        if (v.getId() == R.id.img_profile) {
            Intent intent = new Intent(this, RegistrationActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.ib_pwdedit) {
            Intent intent = new Intent(this, ResetPasswordActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.ib_logout) {
            auth.signOut();
            Toast.makeText(this, "로그아웃되었습니다.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.ib_ok) {
            updateUser();
            finish();
        }
    }

    // 캐릭터 수정 db 업데이트
    private void updateUser() {
        String userName = name.getText().toString();
        Integer userAge = Integer.parseInt(age.getText().toString());
        String userGender = genderSpinner.getSelectedItem().toString();
        int selectedGen = genderSpinner.getSelectedItemPosition() == 0 ? 0 : 1;

        if(selectedGen == 0) userGender = "남자";
        else if(selectedGen == 1) userGender = "여자";

        db.collection("users").document(user.getUid())
                .update("name", userName, "age", userAge, "gender", userGender)
                .addOnCompleteListener(task -> {
                    Toast.makeText(this, "정보가 수정되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "정보 수정에 실패했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // 설정 저장
        SharedPreferences.Editor editor = getSharedPreferences("music", MODE_PRIVATE).edit();
        // 배경음
        boolean isBgmOn = rb_bgm_on.isChecked();
        editor.putBoolean("on&off", isBgmOn);
        // 효과음
        boolean isSoundOn = rb_sound_on.isChecked();
        editor.putBoolean("on&off2", isSoundOn);
        editor.apply();
    }

    // Firestore에서 사용자 정보 가져오기
    private void loadUserInfo() {
        // 이름, 성별, 나이 가져오기
        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String userName = document.getString("name");
                        String userAge = String.valueOf(document.getLong("age"));
                        String userGender = document.getString("gender");

                        name.setText(userName);
                        age.setText(userAge);
                        int spinnerPosition; // spinner 위치 지정 변수
                        if(userGender.equals("남자")) {
                            spinnerPosition = 0;
                            genderSpinner.setSelection(spinnerPosition);
                        }
                        else { // 여자일 때
                            spinnerPosition = 1;
                            genderSpinner.setSelection(spinnerPosition);
                        }
                    }
                });
        // 캐릭터 이미지 가져오기
        final long MEGABYTE = 1024 * 1024; // 1MB
        characterRef.getBytes(MEGABYTE).addOnSuccessListener(bytes -> {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            Bitmap cBitmap = cropImage(bitmap);
            profile.setImageBitmap(cBitmap);
        });
    }
    // 이미지 확대
    private Bitmap cropImage(Bitmap bm) {
        int cropW = 30;
        int cropH = 5;
        int newWidth = 540;
        int newHeight = 450;

        return Bitmap.createBitmap(bm, cropW, cropH, newWidth, newHeight);
    }

    // 배열에서 값을 확인
    private boolean isInArray(String value, String[] array) {
        for (String item : array) {
            if (item.equals(value)) {
                return true;
            }
        }
        return false;
    }

    // 효과음
    public void sound() {
        Intent intent = new Intent(this, SoundService.class);
        if (isSoundOn) startService(intent); // 효과음 on
        else stopService(intent);            // 효과음 off
    }
}