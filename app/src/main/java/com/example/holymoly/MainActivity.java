package com.example.holymoly;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TextView findpwd;
    private EditText etId, etPwd;
    private ImageButton btnLogin, btnJoin, btnToggle;
    private CheckBox auto;

    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private int checked = 0; // 자동 로그인 상태를 위한 변수
    private boolean autoLogin; // 라디오 버튼 상태
    private boolean isPasswordVisible = false;   // 비밀번호 표시 및 숨기기

    public static ArrayList<Activity> actList = new ArrayList<Activity>(); //    스택에 쌓인 액티비티들 중 제거할 액티비티 리스트

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        // "r_state"라는 이름으로 파일생성, MODE_PRIVATE는 자기 앱에서만 사용하도록 설정하는 기본값
        sharedPreferences = getSharedPreferences("r_state", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        findpwd = (TextView) findViewById(R.id.tv_findpwd);
        etId = findViewById(R.id.et_id);
        etPwd = findViewById(R.id.et_pwd);
        btnLogin = findViewById(R.id.btn_login);
        btnJoin = findViewById(R.id.btn_join);
        btnToggle = findViewById(R.id.btn_hidenshow);
        auto = findViewById(R.id.cb_auto);

        // 자동 로그인 라디오 버튼 상태 설정
        auto.setChecked(sharedPreferences.getBoolean("autoLogin", false));
        checked = autoLogin ? 1 : 0;

        // 액티비티가 시작될 때 MusicService를 시작
        Intent startIntent = new Intent(this, MusicService.class);
        startService(startIntent);

        // 비밀번호 숨기기 및 표시
        btnToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPasswordVisible) { // 비밀번호 표시
                    etPwd.setInputType(InputType.TYPE_CLASS_TEXT);
                    btnToggle.setImageResource(R.drawable.ic_eye);
                } else { // 비밀번호 숨기기
                    etPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    btnToggle.setImageResource(R.drawable.ic_eye2);
                }
                etPwd.setSelection(etPwd.length());
                isPasswordVisible = !isPasswordVisible;
            }
        });
        auto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = auto.isChecked();
                editor.putBoolean("autoLogin", isChecked);
                editor.apply();
            }
        });
        /*
        // 자동 로그인 라디오 버튼 클릭 시
        auto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) { // 체크 상태가 활성화된 경우
                    buttonView.setSelected(true);
                    if (checked == 0) {
                        editor.putBoolean("autoLogin", isChecked);
                        editor.apply();
                        checked = 1;
                    }
                } else { // 체크 상태가 비활성화된 경우
                    buttonView.setSelected(false);
                    /*if (checked == 1) {
                        editor.putBoolean("autoLogin", isChecked);
                        editor.apply();
                        checked = 0;
                    }
                }
            }
        });
        */

        // 비밀번호 재설정 클릭 시
        findpwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FindPasswordActivity.class);
                startActivity(intent);
            }
        });

        // 로그인 버튼 클릭 시
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = etId.getText().toString().trim();
                String password = etPwd.getText().toString().trim();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(MainActivity.this, "아이디 또는 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 파이어베이스로 로그인
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // 로그인 성공 시
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Toast.makeText(MainActivity.this, "로그인 성공!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(MainActivity.this, StartActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // 로그인 실패 시
                                    Toast.makeText(MainActivity.this, "로그인 실패, 아이디 또는 비밀번호를 확인하세요." + task.getException(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        // 회원가입 버튼 클릭 시
        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, JoinActivity.class);
                startActivity(intent);
            }
        });
    }

    // 자동 로그인
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        autoLogin = sharedPreferences.getBoolean("autoLogin", false);

        if(user != null && autoLogin) {
            Intent intent = new Intent(this, StartActivity.class);
            startActivity(intent);
        }
    }

    //제거할 액티비티를 리스트에 저장하기 위함
    public ArrayList<Activity> actList(){
        return actList;
    }
}