package com.example.holymoly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {
    EditText name, age;
    RadioButton rb_bgm_on, rb_bgm_off, rb_sound_on, rb_sound_off;
    CircleImageView profile;
    ImageButton logout, pwdEdit, ok;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private StorageReference charcterRef;

    private Spinner genderSpinner;
    private ArrayAdapter<String> adapter;

    private SharedPreferences pref;
    private boolean isBgmOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

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
        charcterRef = storageRef.child("characters/");

        pref = getSharedPreferences("music", MODE_PRIVATE);
        isBgmOn = pref.getBoolean("on&off", true); // 기본값 켜짐

        // 사용자 기존 정보 로딩
        loadUserInfo();

        String[] genderArray = getResources().getStringArray(R.array.gender_array);
        String[] genderWithPrompt = new String[genderArray.length + 1];
        genderWithPrompt[0] = "성별";
        System.arraycopy(genderArray, 0, genderWithPrompt, 1, genderArray.length);

        adapter = new ArrayAdapter<String>(this, R.layout.db_spinner_item, genderWithPrompt) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0; // '성별' 항목을 선택 불가능하도록 설정
            }
        };
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

        // 라디오 버튼 기본 선택값 설정
        rb_bgm_on.setChecked(isBgmOn);
        rb_bgm_off.setChecked(!isBgmOn);
        rb_sound_on.setChecked(true);

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
    }

    @Override
    public void onClick(View v) {
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
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        }
    }

    // 캐릭터 수정 db 업데이트
    private void updateUser() {
        String userName = name.getText().toString();
        Integer userAge = Integer.parseInt(age.getText().toString());
        String userGender = genderSpinner.getSelectedItem().toString();
        int selectedGen = genderSpinner.getSelectedItemPosition() == 1 ? 1 : 2;

        if(selectedGen == 1) userGender = "남자";
        else if(selectedGen == 2) userGender = "여자";

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
        boolean isBgmOn = rb_bgm_on.isChecked();
        editor.putBoolean("on&off", isBgmOn);
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
                            spinnerPosition = 1;
                            genderSpinner.setSelection(spinnerPosition);
                        }
                        else {
                            spinnerPosition = 2;
                                    genderSpinner.setSelection(spinnerPosition);
                        }
                    }
                });
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
                            Bitmap cBitmap = cropImage(bitmap);
                            profile.setImageBitmap(cBitmap);
                        }
                    });
                }
            }
        });
    }
    // 이미지 확대
    private Bitmap cropImage(Bitmap bm) {
        int cropW = 30;
        int cropH = 5;
        int newWidth = 530;
        int newHeight = 450;

        return Bitmap.createBitmap(bm, cropW, cropH, newWidth, newHeight);
    }
}