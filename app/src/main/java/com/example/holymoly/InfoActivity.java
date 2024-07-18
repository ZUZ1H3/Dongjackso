package com.example.holymoly;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class InfoActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText name;  // 이름
    private EditText age;   // 나이
    private ImageButton btnBoy;     // 남자 버튼
    private ImageButton btnGirl;    // 여자 버튼
    private ImageButton go;         // OK 버튼

    private String selectedGender = ""; // 성별 선택

    private FirebaseAuth mAuth;     // firebase 인증
    private FirebaseFirestore db;   // firestore DB

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        name = (EditText) findViewById(R.id.et_name);
        age = (EditText) findViewById(R.id.et_age);
        btnBoy = (ImageButton) findViewById(R.id.btn_boy);
        btnGirl = (ImageButton) findViewById(R.id.btn_girl);
        go = (ImageButton) findViewById(R.id.btn_ok);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        btnBoy.setOnClickListener(this);
        btnGirl.setOnClickListener(this);
        go.setOnClickListener(this);

        // 나이 숫자만 가능하게 설정
        age.setFilters(new InputFilter[] { new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!Character.isDigit(source.charAt(i))) return "";
                }
                return null;
                }
            }
        });
    }
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_boy) { // 남자 버튼 클릭 시
            selectedGender = "남자";
            btnBoy.setSelected(true);
            btnGirl.setSelected(false);
        } else if (v.getId() == R.id.btn_girl) { // 여자 버튼 클릭 시
            selectedGender = "여자";
            btnGirl.setSelected(true);
            btnBoy.setSelected(false);
        } else if (v.getId() == R.id.btn_ok) { // OK 버튼 클릭 시
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
        if (selectedGender.isEmpty()) {
            Toast.makeText(this, "성별을 선택하세요.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    // 정보 firestore DB에 저장
    private void saveInfo() {
        String userName = name.getText().toString();
        Integer userAge = Integer.parseInt(age.getText().toString());
        FirebaseUser user = mAuth.getCurrentUser();

        db.collection("users").document(user.getUid())
                .update("name", userName, "age", userAge, "gender", selectedGender)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "정보가 저장되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "정보 저장에 실패했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
