package com.example.holymoly;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.security.SecureRandom;

public class FindPasswordActivity extends AppCompatActivity implements View.OnClickListener {
    EditText etEmail;
    TextView tempPwd;
    ImageButton emailCertification, copy, goLogin;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_findpassword);

        etEmail = (EditText) findViewById(R.id.et_email);
        tempPwd = (TextView) findViewById(R.id.tv_tempPwd);
        goLogin = (ImageButton) findViewById(R.id.goLogin);
        emailCertification = (ImageButton) findViewById(R.id.btn_emailCertification);
        copy = (ImageButton) findViewById(R.id.ib_copy);

        goLogin.setOnClickListener(this);
        emailCertification.setOnClickListener(this);
        copy.setOnClickListener(this);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
    }
    @Override
    public void onClick(View v) {
        sound();
        if(v.getId() == R.id.btn_emailCertification) {
            emailForm();
        }
        else if(v.getId() == R.id.ib_copy) {
            copyPassword();
        }
        else if(v.getId() == R.id.goLogin) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }
    // 이메일 입력 칸 형식 검사
    private void emailForm() {
        String email = etEmail.getText().toString().trim();

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
                        // 이메일이 유효하면 임시 비밀번호 발급
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            createTemporalPassword();
                        } else {
                            Toast.makeText(FindPasswordActivity.this, "등록되지 않은 이메일입니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }
    // 임시 비밀번호 발급
    private void createTemporalPassword() {
        // 안전한 난수 생성
        SecureRandom secureRandom = new SecureRandom();
        // 임시 비밀번호 길이
        int passwordLength = 8;
        // 임시 비밀번호로 사용할 수 있는 문자들
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        // 임시 비밀번호 생성
        StringBuilder temporalPwd = new StringBuilder(passwordLength);
        for (int i = 0; i < passwordLength; i++) {
            int randomIndex = secureRandom.nextInt(characters.length());
            temporalPwd.append(characters.charAt(randomIndex));
        }

        user.updatePassword(temporalPwd.toString())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            // 임시 비밀번호 db에 업데이트
                            db.collection("users").document(user.getUid())
                                    .update("password", temporalPwd.toString())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> pwTask) {
                                            if (pwTask.isSuccessful()) {
                                                tempPwd.setText(temporalPwd.toString());
                                                Toast.makeText(FindPasswordActivity.this, "임시 비밀번호를 발급했습니다.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
    // 임시 비밀번호 복사
    private void copyPassword() {
        String textToCopy = tempPwd.getText().toString();
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("", textToCopy);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this, "텍스트가 클립보드에 복사되었습니다.", Toast.LENGTH_SHORT).show();
    }
    // 효과음
    public void sound() {
        Intent intent = new Intent(this, SoundService.class);
        startService(intent);
    }
}
