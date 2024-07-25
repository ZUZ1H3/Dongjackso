package com.example.holymoly;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.example.holymoly.R;

import androidx.appcompat.app.AppCompatActivity;

public class RegistrationActivity extends AppCompatActivity {

    private ImageButton ibNext;
    private RadioGroup rgCategory, rgHair, rgClothes, rgHairColor, rgEyesColor;
    private ImageView ivHair, ivEyesColor, ivClothes;
    private boolean isOriginalColor = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        //카테고리 라디오 그룹
        rgCategory = findViewById(R.id.rg_category);

        //뒤로가기 버튼 - 누르면 홈엑티비티로 넘어감
        ibNext = findViewById(R.id.ib_next);

        //머리/옷/염색 종류 라디오그룹 - 선택된 것을 캐릭터에 입히고 색깔도 변하게함 / 중복선택x
        rgHair = findViewById(R.id.rg_hair);
        rgClothes = findViewById(R.id.rg_clothes);
        rgHairColor = findViewById(R.id.rg_hairColor);
        rgEyesColor = findViewById(R.id.rg_eyesColor);

        //디폴트 상태 - 처음에 설정을 키면 머리가 선택된채로 나오게함
        findViewById(R.id.hairLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.clothLayout).setVisibility(View.GONE);
        findViewById(R.id.hairColorLayout).setVisibility(View.GONE);
        findViewById(R.id.eyesColorLayout).setVisibility(View.GONE);

        //착용된 헤어, 눈동자 선언
        ivHair = findViewById(R.id.iv_hair);
        ivEyesColor = findViewById(R.id.iv_character_eyes);
        ivClothes = findViewById(R.id.iv_character_clothes);

        rgCategory.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_hairCategory) {
                    findViewById(R.id.hairLayout).setVisibility(View.VISIBLE);
                    findViewById(R.id.clothLayout).setVisibility(View.GONE);
                    findViewById(R.id.hairColorLayout).setVisibility(View.GONE);
                    findViewById(R.id.eyesColorLayout).setVisibility(View.GONE);
                }
                else if (checkedId == R.id.rb_clothesCategory) {
                    findViewById(R.id.hairLayout).setVisibility(View.GONE);
                    findViewById(R.id.clothLayout).setVisibility(View.VISIBLE);
                    findViewById(R.id.hairColorLayout).setVisibility(View.GONE);
                    findViewById(R.id.eyesColorLayout).setVisibility(View.GONE);
                }
                else if (checkedId == R.id.rb_colorCategory) {
                    findViewById(R.id.hairLayout).setVisibility(View.GONE);
                    findViewById(R.id.clothLayout).setVisibility(View.GONE);
                    findViewById(R.id.hairColorLayout).setVisibility(View.VISIBLE);
                    findViewById(R.id.eyesColorLayout).setVisibility(View.VISIBLE);
                }
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

        //헤어 버튼 누를때 맞는 헤어를 입혀주는 체크체인지리스너
        rgHair.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 예외 처리 또는 기본 행동
                if (checkedId == R.id.rb_g_long) {
                    ivHair.setImageResource(R.drawable.iv_hair_long);
                } else if (checkedId == R.id.rb_b_hook) {
                    ivHair.setImageResource(R.drawable.iv_hair_hook);
                } else if (checkedId == R.id.rb_g_pigtails) {
                    ivHair.setImageResource(R.drawable.iv_hair_pigtails);
                } else if (checkedId == R.id.rb_b_hedgehog) {
                    ivHair.setImageResource(R.drawable.iv_hair_hedgehog);
                } else if (checkedId == R.id.rb_g_twinbuns) {
                    ivHair.setImageResource(R.drawable.iv_hair_twinbuns);
                } else if (checkedId == R.id.rb_b_broccoli) {
                    ivHair.setImageResource(R.drawable.iv_hair_broccoli);
                } else if (checkedId == R.id.rb_g_short) {
                    ivHair.setImageResource(R.drawable.iv_hair_short);
                } else if (checkedId == R.id.rb_b_gourd) {
                    ivHair.setImageResource(R.drawable.iv_hair_gourd);
                } else if (checkedId == R.id.rb_g_twintail) {
                    ivHair.setImageResource(R.drawable.iv_hair_twintail);
                } else if (checkedId == R.id.rb_b_chestnut) {
                    ivHair.setImageResource(R.drawable.iv_hair_chestnut);
                } else if (checkedId == R.id.rb_hair_g_ponytail) {
                    ivHair.setImageResource(R.drawable.iv_hair_ponytail);
                } else if (checkedId == R.id.rb_hair_b_twohooks) {
                    ivHair.setImageResource(R.drawable.iv_hair_twohooks);
                }
            }
        });

        //눈동자 색깔 바꿔주는 체크체인지리스너
        rgEyesColor.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 예외 처리 또는 기본 행동
                if (checkedId == R.id.rb_ec_purple) {
                    ivEyesColor.setImageResource(R.drawable.iv_eyes_purple);
                } else if (checkedId == R.id.rb_ec_green) {
                    ivEyesColor.setImageResource(R.drawable.iv_eyes_green);
                } else if (checkedId == R.id.rb_ec_yellow) {
                    ivEyesColor.setImageResource(R.drawable.iv_eyes_yellow);
                } else if (checkedId == R.id.rb_ec_pink) {
                    ivEyesColor.setImageResource(R.drawable.iv_eyes_pink);
                } else if (checkedId == R.id.rb_ec_brown) {
                    ivEyesColor.setImageResource(R.drawable.iv_eyes_brown);
                } else if (checkedId == R.id.rb_ec_blue) {
                    ivEyesColor.setImageResource(R.drawable.iv_eyes_blue);
                }
            }
        });

        // 머리 색깔 바꿔주는 체크체인지리스너
        rgHairColor.setOnCheckedChangeListener((group, checkedId) -> {
            int colorFilter = Color.TRANSPARENT;

            if (checkedId == R.id.rb_hc_purple) {
                colorFilter = Color.parseColor("#AA93BC"); // 보라색
            } else if (checkedId == R.id.rb_hc_green) {
                colorFilter = Color.parseColor("#91B684"); // 녹색
            } else if (checkedId == R.id.rb_hc_yellow) {
                colorFilter = Color.parseColor("#FBC17E"); // 노란색
            } else if (checkedId == R.id.rb_hc_pink) {
                colorFilter = Color.parseColor("#EC96B0"); // 분홍색
            } else if (checkedId == R.id.rb_hc_brown) {
                colorFilter = Color.parseColor("#A86D60"); // 갈색
            } else if (checkedId == R.id.rb_hc_blue) {
                colorFilter = Color.parseColor("#7E8DB1"); // 파란색
            }

            ivHair.setColorFilter(colorFilter, PorterDuff.Mode.SRC_ATOP);
        });

        //옷 버튼을 누르면 옷을 입혀주는 체크체인지리스너
        rgClothes.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 예외 처리 또는 기본 행동
                if (checkedId == R.id.rb_clothes_princessdress) {
                    ivClothes.setImageResource(R.drawable.iv_clothes_princessdress);
                } else if (checkedId == R.id.rb_clothes_princedress) {
                    ivClothes.setImageResource(R.drawable.iv_clothes_princedress);
                } else if (checkedId == R.id.rb_clothes_denimskirts) {
                    ivClothes.setImageResource(R.drawable.iv_clothes_denimskirt);
                } else if (checkedId == R.id.rb_clothes_denimjeans) {
                    ivClothes.setImageResource(R.drawable.iv_clothes_denimjeans);
                } else if (checkedId == R.id.rb_clothes_rabbit) {
                    ivClothes.setImageResource(R.drawable.iv_clothes_rabbit);
                } else if (checkedId == R.id.rb_clothes_frog) {
                    ivClothes.setImageResource(R.drawable.iv_clothes_frog);
                } else if (checkedId == R.id.rb_clothes_flower) {
                    ivClothes.setImageResource(R.drawable.iv_clothes_flower);
                } else if (checkedId == R.id.rb_clothes_marin) {
                    ivClothes.setImageResource(R.drawable.iv_clothes_marin);
                } else if (checkedId == R.id.rb_clothes_stripe) {
                    ivClothes.setImageResource(R.drawable.iv_clothes_stripe);
                } else if (checkedId == R.id.rb_clothes_cherry) {
                    ivClothes.setImageResource(R.drawable.iv_clothes_cherry);
                }
            }
        });

        //해야할일
        //완성한 것 png로 저장
    }
}
