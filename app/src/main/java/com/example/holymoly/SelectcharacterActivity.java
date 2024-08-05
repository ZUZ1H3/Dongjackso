package com.example.holymoly;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class SelectcharacterActivity extends AppCompatActivity implements UserInfoLoader {
    private ImageButton btnhome, btntrophy, btnsetting, btnnext;
    private View[] customCheckBoxes; // 캐릭터를 저장할 체크박스 배열
    private CharacterData.CharacterInfo[] characters; // 캐릭터 정보를 저장할 배열
    private boolean[] isChecked; // 체크 상태를 저장할 배열
    private TextView name;
    private ImageView profile;
    private String thema;

    private UserInfo userInfo = new UserInfo();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectcharacter);

        MainActivity mainActivity = new MainActivity();
        mainActivity.actList().add(this);

        // Intent에서 테마를 가져옴
        Intent intent = getIntent();
        thema = intent.getStringExtra("selectedThema");
        if (thema != null) {
            // 테마에 따른 캐릭터 이름 요청
            characters = CharacterData.themeCharacterMap.get(thema);
        } else {
            Toast.makeText(this, "테마가 없습니다", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (characters == null) {
            Toast.makeText(this, "해당 테마에 대한 캐릭터가 없습니다", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        profile = findViewById(R.id.mini_profile);
        name = findViewById(R.id.mini_name);
        btnhome = findViewById(R.id.ib_homebutton);
        btntrophy = findViewById(R.id.ib_trophy);
        btnsetting = findViewById(R.id.ib_setting);
        btnnext = findViewById(R.id.ib_nextStep);

        loadUserInfo(profile, name);

        btnhome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectcharacterActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        btntrophy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectcharacterActivity.this, TrophyActivity.class);
                startActivity(intent);
            }
        });

        btnsetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectcharacterActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });

        btnnext.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ArrayList<String> selectedCharacters = new ArrayList<>();
                for (int i = 0; i < isChecked.length; i++) {
                    if (isChecked[i]) {
                        selectedCharacters.add(characters[i].name);
                    }
                }

                if (selectedCharacters.isEmpty()) {
                    Toast.makeText(SelectcharacterActivity.this, "캐릭터를 선택해주세요", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(SelectcharacterActivity.this, MakeStoryActivity.class);
                    intent.putStringArrayListExtra("selectedCharacters", selectedCharacters);
                    intent.putExtra("selectedTheme", thema);
                    startActivity(intent);
                }
            }
        });

        LinearLayout hllFirst = findViewById(R.id.hll_first);
        LinearLayout hllSecond = findViewById(R.id.hll_second);
        LayoutInflater inflater = getLayoutInflater();

        // customCheckBoxes 배열 초기화
        customCheckBoxes = new View[characters.length];
        isChecked = new boolean[characters.length];

        // 커스텀 체크박스 초기화
        for (int i = 0; i < characters.length; i++) {
            customCheckBoxes[i] = inflater.inflate(R.layout.custom_checkbox, hllFirst, false);
            final int index = i; // 인덱스를 final로 설정
            customCheckBoxes[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isChecked[index] = !isChecked[index]; // 상태 변경
                    updateCheckBoxBackground(customCheckBoxes[index], isChecked[index]); // 배경 업데이트
                }
            });
            if (i < 5) {
                hllFirst.addView(customCheckBoxes[i]);
            } else {
                hllSecond.addView(customCheckBoxes[i]);
            }
        }

        // 체크박스에 캐릭터 이름 및 이미지 설정
        setCheckBoxNamesAndImages();
    }

    // 체크박스에 캐릭터 이름 및 이미지 설정
    private void setCheckBoxNamesAndImages() {
        for (int i = 0; i < customCheckBoxes.length; i++) {
            TextView textView = customCheckBoxes[i].findViewById(R.id.checkbox_text);
            textView.setText(characters[i].name);

            ImageView imageView = customCheckBoxes[i].findViewById(R.id.checkbox_image);
            imageView.setImageResource(characters[i].imageResId);
        }
    }

    // 체크박스 배경 업데이트 메서드
    private void updateCheckBoxBackground(View customCheckBox, boolean isChecked) {
        ImageView imageView = customCheckBox.findViewById(R.id.checkbox_image);
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        if (drawable != null) {
            int alpha = isChecked ? 128 : 255; // 체크되었을 때는 투명도 50%, 체크 해제 시 투명도 100%
            drawable.setAlpha(alpha);
        }
    }

    @Override
    public void loadUserInfo(ImageView profile, TextView name) {
        userInfo.loadUserInfo(profile, name);
    }
}
