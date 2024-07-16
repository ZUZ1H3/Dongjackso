package com.example.holymoly;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SelectthemaActivity extends AppCompatActivity {

    private RadioGroup radioGroup;
    private ImageButton btnnext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectthema);

        radioGroup = findViewById(R.id.radioGroup);
        btnnext = findViewById(R.id.ib_nextStep);

        // btnnext 버튼 클릭 리스너 설정
        btnnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedId = radioGroup.getCheckedRadioButtonId();
                String selectedTheme = "";

                // 선택된 ID에 따라 테마를 설정합니다.
                if (selectedId == R.id.thema_sea) {
                    selectedTheme = "바다";
                } else if (selectedId == R.id.thema_castle) {
                    selectedTheme = "궁전";
                } else if (selectedId == R.id.thema_forest) {
                    selectedTheme = "숲";
                } else if (selectedId == R.id.thema_village) {
                    selectedTheme = "마을";
                } else if (selectedId == R.id.thema_house) {
                    selectedTheme = "집";
                } else {
                    // 테마가 선택되지 않은 경우
                    Toast.makeText(SelectthemaActivity.this, "테마를 선택하세요!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 선택된 테마를 인텐트에 추가하고 다음 액티비티로 이동합니다.
                Intent intent = new Intent(SelectthemaActivity.this, SelectcharacterActivity.class);
                intent.putExtra("selectedTheme", selectedTheme);
                startActivity(intent);
            }
        });
    }
}
