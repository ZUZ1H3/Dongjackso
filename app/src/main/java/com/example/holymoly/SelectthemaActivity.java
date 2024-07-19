package com.example.holymoly;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SelectthemaActivity extends AppCompatActivity {

    private ImageButton btnhome, btntrophy, btnsetting, btnnext;
    private RadioGroup radioGroup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectthema);

        btnhome = findViewById(R.id.ib_homebutton);
        btntrophy = findViewById(R.id.ib_trophy);
        btnsetting = findViewById(R.id.ib_setting);

        radioGroup = findViewById(R.id.radioGroup);
        btnnext = findViewById(R.id.ib_nextStep);

        btnhome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectthemaActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        btntrophy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectthemaActivity.this, TrophyActivity.class);
                startActivity(intent);
            }
        });

        btnsetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectthemaActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });

        // 커스텀 라디오 버튼 추가
        addCustomRadioButton("바다", R.drawable.radio_sea, R.id.thema_sea);
        addCustomRadioButton("궁전", R.drawable.radio_cestle, R.id.thema_castle);
        addCustomRadioButton("숲", R.drawable.radio_forest, R.id.thema_forest);
        addCustomRadioButton("마을", R.drawable.radio_village, R.id.thema_village);
        addCustomRadioButton("우주", R.drawable.radio_house, R.id.thema_house);

        // btnnext 버튼 클릭 리스너 설정
        btnnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedId = radioGroup.getCheckedRadioButtonId();
                String selectedThema = "";

                // 선택된 ID에 따라 테마를 설정합니다.
                if (selectedId == R.id.thema_sea) {
                    selectedThema = "바다";
                } else if (selectedId == R.id.thema_castle) {
                    selectedThema = "궁전";
                } else if (selectedId == R.id.thema_forest) {
                    selectedThema = "숲";
                } else if (selectedId == R.id.thema_village) {
                    selectedThema = "마을";
                } else if (selectedId == R.id.thema_house) {
                    selectedThema = "우주";
                } else {
                    // 테마가 선택되지 않은 경우
                    Toast.makeText(SelectthemaActivity.this, "테마를 선택하세요!", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 선택된 테마를 인텐트에 추가하고 다음 액티비티로 이동합니다.
                Intent intent = new Intent(SelectthemaActivity.this, SelectcharacterActivity.class);
                intent.putExtra("selectedThema", selectedThema);
                startActivity(intent);
            }
        });
    }

    private void addCustomRadioButton(String text, int imageResId, int radioButtonId) {
        // 레이아웃 인플레이터를 사용하여 커스텀 라디오 버튼 레이아웃 인플레이트
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.custom_radio_button, radioGroup, false);

        // 레이아웃에서 뷰 찾기
        ImageView imageView = view.findViewById(R.id.radio_image);
        TextView textView = view.findViewById(R.id.radio_text);

        // 이미지와 텍스트 설정
        imageView.setImageResource(imageResId);
        textView.setText(text);

        // View에 ID 설정
        view.setId(radioButtonId);

        // View에 클릭 리스너 설정
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 선택된 View의 ID를 라디오 그룹의 체크된 항목으로 설정
                radioGroup.check(v.getId());

                // 모든 라디오 버튼의 이미지 불투명도 초기화 (원래대로)
                for (int i = 0; i < radioGroup.getChildCount(); i++) {
                    View childView = radioGroup.getChildAt(i);
                    ImageView childImageView = childView.findViewById(R.id.radio_image);
                    childImageView.setAlpha(1.0f); // 원래대로 투명도 설정
                }

                // 선택된 라디오 버튼의 이미지 불투명도 설정
                imageView.setAlpha(0.4f);
            }
        });

        // 라디오 그룹에 추가
        radioGroup.addView(view);
    }

}
