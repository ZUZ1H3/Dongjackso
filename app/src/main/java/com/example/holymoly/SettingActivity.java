package com.example.holymoly;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener{
    EditText name, age;
    RadioButton rb_bgm_on, rb_bgm_off, rb_sound_on, rb_sound_off;
    ImageButton custom, logout;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore db;

    private Spinner genderSpinner;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        name = findViewById(R.id.et_name);
        age = findViewById(R.id.et_age);
        custom = findViewById(R.id.ib_custom);
        logout = findViewById(R.id.ib_logout);

        custom.setOnClickListener(this);
        logout.setOnClickListener(this);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        genderSpinner = findViewById(R.id.spinner_gender);

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

        // 나이 입력 설정
        EditText ageEditText = findViewById(R.id.et_age);
        ageEditText.setInputType(InputType.TYPE_CLASS_NUMBER);

        // 배경 음악 라디오 그룹 설정
        RadioGroup rgBgm = findViewById(R.id.rg_bgm);
        rb_bgm_on = findViewById(R.id.rb_bgm_on);
        rb_bgm_off = findViewById(R.id.rb_bgm_off);

        // 효과음 라디오 그룹 설정
        RadioGroup rgSound = findViewById(R.id.rg_sound);
        rb_sound_on = findViewById(R.id.rb_sound_on);
        rb_sound_off = findViewById(R.id.rb_sound_off);

        // 라디오 버튼 기본 선택값 설정
        rb_bgm_on.setChecked(true);
        rb_sound_on.setChecked(true);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.ib_custom) { updateUser(); }
        if(v.getId() == R.id.ib_logout) {
            auth.signOut();
            Toast.makeText(this, "로그아웃되었습니다..", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }
    // 캐릭터 수정 db 업데이트
    private void updateUser() {
        String userName = name.getText().toString();
        Integer userAge = Integer.parseInt(age.getText().toString());
        String userGender = genderSpinner.getSelectedItem().toString();

        db.collection("users").document(user.getUid())
                .update("name", userName, "age", userAge, "gender", userGender)
                .addOnCompleteListener(task -> {
                    Toast.makeText(this, "정보가 수정되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "정보 수정에 실패했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    /*
    // Firestore에서 사용자 정보 가져오기
    private void loadUserInfo() {
        DocumentReference docRef = db.collection("users").document(user.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()) {
                        UserInfo userInfo = document.toObject(UserInfo.class);
                        String userName = userInfo.getName();
                        Integer userAge = userInfo.getAge();
                        //String userGender = userInfo.getGender();

                        name.setText(userName);
                        age.setText(String.valueOf(userAge));

                        // int spinnerPosition = adapter.getPosition(userGender);
                        // genderSpinner.setSelection(spinnerPosition);
                    }
                }
            }
        });
    }
     */
}