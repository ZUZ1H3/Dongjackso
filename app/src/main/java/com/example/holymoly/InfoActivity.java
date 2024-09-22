package com.example.holymoly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InfoActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText name, age;  // 이름, 나이
    private ImageButton go; // 남자, 여자, ok 버튼
    private ImageView profile;
    private RadioButton btnBoy, btnGirl;
    private RadioGroup rgGender;

    private String selectedGender = ""; // 성별 선택

    /* DB */
    private FirebaseAuth mAuth;     // firebase 사용자 인증
    private FirebaseFirestore db;   // firestore DB
    private FirebaseUser user;      // firebase 사용자
    private FirebaseStorage storage; // firebase Storage
    private StorageReference storageRef;
    private StorageReference characterRef; // Storage 캐릭터 이미지 참조

    /* 효과음 */
    private SharedPreferences pref;
    private boolean isSoundOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        pref = getSharedPreferences("music", MODE_PRIVATE); // 효과음 초기화

        name = (EditText) findViewById(R.id.et_name);
        age = (EditText) findViewById(R.id.et_age);
        rgGender = (RadioGroup) findViewById(R.id.rg_gender);
        btnBoy = (RadioButton) findViewById(R.id.rb_boy);
        btnGirl = (RadioButton) findViewById(R.id.rb_girl);
        go = (ImageButton) findViewById(R.id.btn_ok);
        profile = (ImageView) findViewById(R.id.img_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();  // 현재 접속한 사용자
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        characterRef = storageRef.child("characters/" + user.getUid() + ".png");

        btnBoy.setOnClickListener(this);
        btnGirl.setOnClickListener(this);
        go.setOnClickListener(this);

        loadImage(); // 캐릭터 이미지 가져오기

        // 나이 숫자만 가능하게 설정
        age.setFilters(new InputFilter[]{new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!Character.isDigit(source.charAt(i))) return "";
                }
                return null;
            }
        }});
    }

    @Override
    public void onClick(View v) {
        sound();
        if (v.getId() == R.id.btn_ok) { // OK 버튼 클릭 시
            if (validateInput()) {
                saveInfo();
                Intent intent = new Intent(this, StartActivity.class);
                startActivity(intent);
            }
        }
    }

    // 빈칸 유효성 검사
    private boolean validateInput() {
        if (name.getText().toString().isEmpty()) {
            Toast.makeText(this, "이름을 입력하세요.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (age.getText().toString().isEmpty()) {
            Toast.makeText(this, "나이를 입력하세요.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (rgGender.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "성별을 선택하세요.", Toast.LENGTH_SHORT).show();
            return false;
        }

        int selectedId = rgGender.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = findViewById(selectedId);
        selectedGender = selectedRadioButton.getText().toString();

        return true;
    }

    // 정보 firestore DB에 저장
    private void saveInfo() {
        String userName = name.getText().toString();
        Integer userAge = Integer.parseInt(age.getText().toString());

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("name", userName);
        userInfo.put("age", userAge);
        userInfo.put("gender", selectedGender);

        db.collection("users").document(user.getUid())
                .set(userInfo, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "정보가 저장되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "정보 저장에 실패했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // 이미지 가져오기
    private void loadImage() {
        characterRef.listAll().addOnSuccessListener(listResult -> {
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
        int cropW = 25;
        int cropH = 5;
        int newWidth = 550;
        int newHeight = 450;

        return Bitmap.createBitmap(bm, cropW, cropH, newWidth, newHeight);
    }

    // 효과음
    public void sound() {
        isSoundOn = pref.getBoolean("on&off2", true);
        Intent intent = new Intent(this, SoundService.class);
        if (isSoundOn) startService(intent); // 효과음 on
        else stopService(intent);            // 효과음 off
    }
}
