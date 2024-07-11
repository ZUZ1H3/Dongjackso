package com.example.holymoly;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class JoinActivity extends AppCompatActivity {

    private EditText etId, etPwd, etPwdChk;
    private ImageButton btnDouble, btnNext;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_join);

        mAuth = FirebaseAuth.getInstance();

        // XML 레이아웃의 뷰 객체 연결
        etId = findViewById(R.id.et_id);
        etPwd = findViewById(R.id.et_pwd);
        etPwdChk = findViewById(R.id.et_pwdchk);
        btnDouble = findViewById(R.id.btn_double);
        btnNext = findViewById(R.id.btn_next);

        // 회원가입 버튼 클릭 리스너
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etId.getText().toString().trim();
                String password = etPwd.getText().toString().trim();
                String passwordCheck = etPwdChk.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(JoinActivity.this, "이메일을 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password) || TextUtils.isEmpty(passwordCheck)) {
                    Toast.makeText(JoinActivity.this, "비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(passwordCheck)) {
                    Toast.makeText(JoinActivity.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Firebase로 회원가입 시도
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(JoinActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // 회원가입 성공
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Toast.makeText(JoinActivity.this, "회원가입 성공!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(JoinActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // 회원가입 실패
                                    Toast.makeText(JoinActivity.this, "회원가입 실패. 다시 시도하세요.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }
}