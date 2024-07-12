package com.example.holymoly;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.regex.Pattern;

public class JoinActivity extends AppCompatActivity {

    private EditText etId, etPwd, etPwdChk;
    private ImageButton btnJoin, btnDouble;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private boolean isEmailUnique = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etId = findViewById(R.id.et_id);
        etPwd = findViewById(R.id.et_pwd);
        etPwdChk = findViewById(R.id.et_pwdchk);
        btnJoin = findViewById(R.id.btn_next);
        btnDouble = findViewById(R.id.btn_mailDuplication);

        btnDouble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkEmailDuplicate();
            }
        });

        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isEmailUnique) {
                    registerUser();
                } else {
                    Toast.makeText(JoinActivity.this, "이메일 중복 확인을 해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkEmailDuplicate() {
        String email = etId.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(JoinActivity.this, "이메일을 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(JoinActivity.this, "유효한 이메일 주소를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            boolean isDuplicate = false;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.exists()) {
                                    isDuplicate = true;
                                    break;
                                }
                            }
                            if (isDuplicate) {
                                isEmailUnique = false;
                                Toast.makeText(JoinActivity.this, "이미 존재하는 이메일입니다.", Toast.LENGTH_SHORT).show();
                            } else {
                                isEmailUnique = true;
                                Toast.makeText(JoinActivity.this, "사용 가능한 이메일입니다.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(JoinActivity.this, "이메일 확인 실패. 다시 시도하세요.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void registerUser() {
        String email = etId.getText().toString().trim();
        String password = etPwd.getText().toString().trim();
        String passwordCheck = etPwdChk.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(passwordCheck)) {
            Toast.makeText(JoinActivity.this, "모든 필드를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isPasswordValid(password)) {
            Toast.makeText(JoinActivity.this, "비밀번호는 영문, 숫자, 특수문자를 포함하여 8자 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(passwordCheck)) {
            Toast.makeText(JoinActivity.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(JoinActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // 회원가입 성공 시 Firestore에 사용자 정보 저장
                            FirebaseUser user = mAuth.getCurrentUser();
                            db.collection("users").document(user.getUid())
                                    .set(new User(email))
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(JoinActivity.this, "회원가입 성공!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(JoinActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(JoinActivity.this, "회원가입 데이터 저장 실패.", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            // 회원가입 실패 시
                            Toast.makeText(JoinActivity.this, "회원가입 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // 비밀번호 유효성 검사
    private boolean isPasswordValid(String password) {
        Pattern pattern = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+=<>?{}\\[\\]~-]).{8,}$");
        return pattern.matcher(password).matches();
    }

    // User 클래스 추가
    public static class User {
        public String email;

        public User() {
            // 기본 생성자 필요
        }

        public User(String email) {
            this.email = email;
        }
    }
}