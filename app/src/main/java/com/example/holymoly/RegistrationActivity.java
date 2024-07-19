package com.example.holymoly;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegistrationActivity extends AppCompatActivity {

    private ImageButton ibHair, ibClothes, ibColor, icNext;
    private RadioGroup rgHair, rgClothes, rgColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialize buttons and radio groups
        ibHair = findViewById(R.id.ib_hair);
        ibClothes = findViewById(R.id.ib_clothes);
        ibColor = findViewById(R.id.ib_color);
        icNext = findViewById(R.id.ic_next);
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

        icNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(RegistrationActivity.this, "Next button clicked", Toast.LENGTH_SHORT).show();
            }
        });

        rgHair.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = findViewById(checkedId);
                if (rb != null) {
                    Toast.makeText(RegistrationActivity.this, "Hair style: " + rb.getContentDescription(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        rgClothes.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = findViewById(checkedId);
                if (rb != null) {
                    Toast.makeText(RegistrationActivity.this, "Clothes: " + rb.getContentDescription(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        rgColor.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = findViewById(checkedId);
                if (rb != null) {
                    Toast.makeText(RegistrationActivity.this, "Color: " + rb.getContentDescription(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

