package com.example.holymoly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.security.SecureRandom;
import java.util.regex.Pattern;

public class ResetPasswordActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText etEmail;
    private EditText etPwd;
    private EditText etPwdcheck;

    private ImageButton emailCertification;
    private ImageButton ok;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore db;

    private boolean isCertifiedEmail = false;   // 이메일 인증 여부 확인
    private boolean isUpdatePassword = false;   // 비밀번호 재설정 인증 여부 확인

    private Handler emailCheckHandler = new Handler(); // 이메일 확인 handler
    private Runnable emailCheckRunnable;        // 이메일 확인 runnable

    /* 효과음 */
    private SharedPreferences pref;
    private boolean isSoundOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resetpassword);
        pref = getSharedPreferences("music", MODE_PRIVATE); // 효과음 초기화

        etEmail = (EditText) findViewById(R.id.et_email);
        etPwd = (EditText) findViewById(R.id.et_pwd);
        etPwdcheck = (EditText) findViewById(R.id.et_pwdchk);
        emailCertification = (ImageButton) findViewById(R.id.btn_emailCertification);
        ok = (ImageButton) findViewById(R.id.btn_ok);

        emailCertification.setOnClickListener(this);
        ok.setOnClickListener(this);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
    }
    @Override
    public void onClick(View v) {
        // 이메일 인증 버튼 클릭 리스너
        if(v.getId() == R.id.btn_emailCertification) {
            emailForm();
        }
        // OK 버튼 클릭 리스너
        else if(v.getId() == R.id.btn_ok) {
            // 인증 메일 클릭 후 비밀번호 재설정
            if(isCertifiedEmail) { updatePassword(); }
            else {
                Toast.makeText(this, "이메일 인증이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
            if(isUpdatePassword) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
        }
        sound();
    }
    // 이메일 입력 칸 형식 검사
    private void emailForm() {
        String email = etEmail.getText().toString().trim();
        db = FirebaseFirestore.getInstance();

        // 공백 검사
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "이메일을 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        // 형식 검사
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "유효한 이메일 주소를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        // 이메일이 DB에 있는지 확인
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        // 이메일이 유효하면 인증 이메일 발송
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            sendEmailLink();
                        } else {
                            Toast.makeText(ResetPasswordActivity.this, "등록되지 않은 이메일입니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }
    // 인증 이메일 발송
    private void sendEmailLink() {
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ResetPasswordActivity.this, "인증 메일을 발송했습니다.", Toast.LENGTH_SHORT).show();
                            startEmailCheck();
                        } else {
                            Toast.makeText(ResetPasswordActivity.this, "인증 메일 발송에 실패했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    // 이메일 발송 후 10초 마다 인증 확인
    private void startEmailCheck() {
        emailCheckRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isCertifiedEmail) {
                    checkEmailLink();
                    emailCheckHandler.postDelayed(this, 10000);
                }
            }
        };
        emailCheckHandler.post(emailCheckRunnable);
    }
    // 이메일 링크 인증 상태 확인
    private void checkEmailLink() {
        user.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    if (user.isEmailVerified()) {
                        isCertifiedEmail = true;
                        Toast.makeText(ResetPasswordActivity.this, "이메일 인증이 완료되었습니다. 비밀번호를 새로 설정하세요.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ResetPasswordActivity.this, "이메일 인증이 필요합니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ResetPasswordActivity.this, "사용자 정보를 다시 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    // 비밀번호 업데이트
    private void updatePassword() {
        String newPassword = etPwd.getText().toString().trim();
        String newPasswordCheck = etPwdcheck.getText().toString().trim();

        if (TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(newPasswordCheck)) {
            Toast.makeText(this, "모든 필드를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isPasswordValid(newPassword)) {
            Toast.makeText(this, "비밀번호는 영문, 숫자, 특수문자를 포함하여 8자 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(newPasswordCheck)) {
            Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        user.updatePassword(newPassword)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            db.collection("users").document(user.getUid())
                                    .update("password", newPassword)
                                    .addOnSuccessListener(documentReference -> {
                                        Toast.makeText(ResetPasswordActivity.this, "비밀번호가 업데이트 되었습니다.", Toast.LENGTH_SHORT).show();
                                        isUpdatePassword = true;
                                        isCertifiedEmail = false;
                                    });
                        }
                        else Toast.makeText(ResetPasswordActivity.this, "비밀번호 업데이트를 실패했습니다: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    // 비밀번호 유효성 검사
    private boolean isPasswordValid(String password) {
        Pattern pattern = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+=<>?{}\\[\\]~-]).{8,}$");
        return pattern.matcher(password).matches();
    }

    // 효과음
    public void sound() {
        isSoundOn = pref.getBoolean("on&off2", true);
        Intent intent = new Intent(this, SoundService.class);
        if (isSoundOn) startService(intent); // 효과음 on
        else stopService(intent);            // 효과음 off
    }
}