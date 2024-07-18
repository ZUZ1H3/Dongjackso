package com.example.holymoly;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

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
    }
}
