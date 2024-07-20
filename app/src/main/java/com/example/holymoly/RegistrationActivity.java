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

        ibHair = findViewById(R.id.ib_hair);
        ibClothes = findViewById(R.id.ib_clothes);
        ibColor = findViewById(R.id.ib_color);
        ibNext = findViewById(R.id.ib_next);
        rgHair = findViewById(R.id.rg_hair);
        rgClothes = findViewById(R.id.rg_clothes);
        rgColor = findViewById(R.id.rg_color);

        ibHair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.hairlayout).setVisibility(View.VISIBLE);
                findViewById(R.id.clothlayout).setVisibility(View.GONE);
                findViewById(R.id.colorlayout).setVisibility(View.GONE);
            }
        });

        ibClothes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.hairlayout).setVisibility(View.GONE);
                findViewById(R.id.clothlayout).setVisibility(View.VISIBLE);
                findViewById(R.id.colorlayout).setVisibility(View.GONE);
            }
        });

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

        rgHair.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = findViewById(checkedId);
                if (rb != null) {
                    Toast.makeText(RegistrationActivity.this, "머리 버튼 선택" + rb.getContentDescription(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        rgClothes.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = findViewById(checkedId);
                if (rb != null) {
                    Toast.makeText(RegistrationActivity.this, "옷 버튼 선택" + rb.getContentDescription(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        rgColor.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = findViewById(checkedId);
                if (rb != null) {
                    Toast.makeText(RegistrationActivity.this, "염색 버튼 선택" + rb.getContentDescription(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
