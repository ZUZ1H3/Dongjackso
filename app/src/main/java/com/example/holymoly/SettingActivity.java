package com.example.holymoly;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingActivity extends AppCompatActivity {

    private MediaPlayer bgmPlayer; // 배경 음악을 재생할 MediaPlayer
    private boolean isBgmOn = true; // 배경 음악 상태 변수
    private boolean isSoundOn = true; // 효과음 상태 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // 성별 스피너 설정
        Spinner genderSpinner = findViewById(R.id.spinner_gender);
        String[] genderArray = getResources().getStringArray(R.array.gender_array);
        String[] genderWithPrompt = new String[genderArray.length + 1];
        genderWithPrompt[0] = "성별";
        System.arraycopy(genderArray, 0, genderWithPrompt, 1, genderArray.length);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.db_spinner_item, genderWithPrompt) {
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
        RadioButton rbBgmOn = findViewById(R.id.rb_bgm_on);
        RadioButton rbBgmOff = findViewById(R.id.rb_bgm_off);

        rgBgm.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_bgm_on) {
                    isBgmOn = true;
                    startBackgroundMusic();
                } else if (checkedId == R.id.rb_bgm_off) {
                    isBgmOn = false;
                    stopBackgroundMusic();
                }
            }
        });

        // 효과음 라디오 그룹 설정
        RadioGroup rgSound = findViewById(R.id.rg_sound);
        RadioButton rbSoundOn = findViewById(R.id.rb_sound_on);
        RadioButton rbSoundOff = findViewById(R.id.rb_sound_off);

        rgSound.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_sound_on) {
                    isSoundOn = true;
                    Toast.makeText(SettingActivity.this, "효과음 켜짐", Toast.LENGTH_SHORT).show();
                } else if (checkedId == R.id.rb_sound_off) {
                    isSoundOn = false;
                    Toast.makeText(SettingActivity.this, "효과음 꺼짐", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 배경 음악 재생을 위한 MediaPlayer 초기화
        bgmPlayer = MediaPlayer.create(this, R.raw.bgm_sea);
        bgmPlayer.setLooping(true);

        // 기본적으로 배경 음악이 켜져 있으면 재생 시작
        if (isBgmOn) {
            startBackgroundMusic();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bgmPlayer != null) {
            bgmPlayer.release();
            bgmPlayer = null;
        }
    }

    private void startBackgroundMusic() {
        if (bgmPlayer != null && !bgmPlayer.isPlaying()) {
            bgmPlayer.start();
        }
    }

    private void stopBackgroundMusic() {
        if (bgmPlayer != null && bgmPlayer.isPlaying()) {
            bgmPlayer.pause();
        }
    }
}
