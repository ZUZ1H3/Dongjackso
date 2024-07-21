package com.example.holymoly;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegistrationActivity extends AppCompatActivity {

    private ImageButton ibHair, ibClothes, ibColor, ibNext;
    private RadioGroup rgHair, rgClothes, rgColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        //머리/옷/염색 카테고리 이미지 버튼 - 누른 버튼에 따라 관련된 레이아웃만 나오도록 함
        ibHair = findViewById(R.id.ib_hair);
        ibClothes = findViewById(R.id.ib_clothes);
        ibColor = findViewById(R.id.ib_color);

        //뒤로가기 버튼 - 누르면 홈엑티비티로 넘어감
        ibNext = findViewById(R.id.ib_next);

        //머리/옷/염색 종류 라디오그룹 - 선택된 것을 캐릭터에 입히고 색깔도 변하게함 / 중복선택x
        rgHair = findViewById(R.id.rg_hair);
        rgClothes = findViewById(R.id.rg_clothes);
        rgColor = findViewById(R.id.rg_color);

        //디폴트 상태 - 처음에 설정을 키면 머리가 선택된채로 나오게함
        findViewById(R.id.hairlayout).setVisibility(View.VISIBLE);
        findViewById(R.id.clothlayout).setVisibility(View.GONE);
        findViewById(R.id.colorlayout).setVisibility(View.GONE);

        //머리 버튼을 눌렀을 때 - 머리레이아웃만 보이게함
        ibHair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.hairlayout).setVisibility(View.VISIBLE);
                findViewById(R.id.clothlayout).setVisibility(View.GONE);
                findViewById(R.id.colorlayout).setVisibility(View.GONE);
            }
        });

        //옷 버튼을 눌렀을 때 - 옷 레이아웃만 보이게함
        ibClothes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.hairlayout).setVisibility(View.GONE);
                findViewById(R.id.clothlayout).setVisibility(View.VISIBLE);
                findViewById(R.id.colorlayout).setVisibility(View.GONE);
            }
        });

        //염색 버튼을 눌렀을 때 - 염색 레이아웃만 보이게함
        ibColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.hairlayout).setVisibility(View.GONE);
                findViewById(R.id.clothlayout).setVisibility(View.GONE);
                findViewById(R.id.colorlayout).setVisibility(View.VISIBLE);
            }
        });

        //icNext를 누르면 HomeActivity로 넘어가도록 함(뒤로가기 버튼)
        ibNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegistrationActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        //이 다음 해야할 일
        //xml에 머리랑 옷들 피그마에서 이미지 따와서 하나하나 추가
        //이미지들 위치 조정해서 배치
        //선택되면 선택된 이미지만 남겨두고 visible gone으로 설정
        //선택된 거만 visible 설정
        //선택된 거 저장?

    }
}
