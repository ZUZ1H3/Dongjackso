package com.example.holymoly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.*;
import java.util.*;
import android.app.AlertDialog;

public class PuzzleActivity extends AppCompatActivity implements View.OnClickListener, UserInfoLoader {
    /* 상단 프로필 */
    private TextView name, nickname;
    private ImageView profile;
    private UserInfo userInfo = new UserInfo();

    /* DB */
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();

    /* 우측 상단 미니 버튼 */
    private ImageButton btnhome, btntrophy, btnsetting;

    // 이미지의 url
    private List<String> allImageUrls = new ArrayList<>();
    private List<String> imageUrls = new ArrayList<>();

    private String[] items = { "전체", "바다", "궁전", "숲", "마을", "우주", "사막", "커스텀" };
    private RecyclerView recyclerView;
    private Spinner spinnerNav;
    private PuzzleAdapter puzzleAdapter;

    /* 효과음 */
    private SharedPreferences pref;
    private boolean isSoundOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle);
        pref = getSharedPreferences("music", MODE_PRIVATE); // 효과음 초기화

        btnhome = findViewById(R.id.ib_homebutton);
        btntrophy = findViewById(R.id.ib_trophy);
        btnsetting = findViewById(R.id.ib_setting);

        btnhome.setOnClickListener(this);
        btntrophy.setOnClickListener(this);
        btnsetting.setOnClickListener(this);

        // 상단 프로필 로딩
        name = findViewById(R.id.mini_name);
        nickname = findViewById(R.id.mini_nickname);
        profile = findViewById(R.id.mini_profile);
        loadUserInfo(profile, name, nickname);
        recyclerView = findViewById(R.id.recyclerView);

        // 가로 방향 스크롤 설정
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false));

        puzzleAdapter = new PuzzleAdapter(this, imageUrls);
        recyclerView.setAdapter(puzzleAdapter);

        // 스피너 설정
        spinnerNav = findViewById(R.id.thema_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.thema_text, items);
        adapter.setDropDownViewResource(R.layout.thema_text);
        spinnerNav.setAdapter(adapter);

        loadImages();  // 전체 이미지 로드

        // 스피너 선택 이벤트 처리
        spinnerNav.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sound();
                selectSection(position);  // 선택된 섹션으로 설정
                String selectedTheme = items[position];
                filterByTheme(selectedTheme);  // 테마에 맞게 필터링
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 아무것도 선택되지 않았을 때의 행동
            }
        });
        puzzleAdapter.setOnItemClickListener(imageUrl -> {
            sound();
            Intent intent = new Intent(this, SelectPuzzleActivity.class);
            intent.putExtra("selectedImage", imageUrl);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onClick(View v) {
        sound();
        /* 상단 미니 아이콘 클릭 */
        if(v.getId() == R.id.ib_homebutton) {
            Intent intent = new Intent(this, Home2Activity.class);
            startActivity(intent);
        }
        else if(v.getId() == R.id.ib_trophy) {
            Intent intent = new Intent(this, TrophyActivity.class);
            intent.putExtra("from", "PuzzleActivity");
            startActivity(intent);
        }
        else if(v.getId() == R.id.ib_setting) {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
        }
    }

    private void selectSection(int id) {
        TextView secTitle = findViewById(R.id.sec_title);
        switch (id) {
            case 0: secTitle.setText("- 전체 -"); break;
            case 1: secTitle.setText("- 바다 -"); break;
            case 2: secTitle.setText("- 궁전 -"); break;
            case 3: secTitle.setText("- 숲 -"); break;
            case 4: secTitle.setText("- 마을 -"); break;
            case 5: secTitle.setText("- 우주 -"); break;
            case 6: secTitle.setText("- 사막 -"); break;
            case 7: secTitle.setText("- 커스텀 -"); break;
        }
    }

    // Firebase Storage에서 이미지 가져옴
    private void loadImages() {
        String uid = user.getUid();

        for (String theme : items) {
            // "전체"와 "내 사진"은 Storage에 없는 폴더이므로 제외
            if (!theme.equals("전체")) {
                StorageReference themeRef = storageRef.child("background").child(theme);

                themeRef.listAll().addOnSuccessListener(listResult -> {
                    for (StorageReference item : listResult.getItems()) {
                        String img = item.getName();
                        if (img.startsWith(uid)) {
                            item.getDownloadUrl().addOnSuccessListener(uri -> {
                                String url = uri.toString();
                                // URL에 테마를 추가하여 저장
                                if (!allImageUrls.contains(url)) {
                                    allImageUrls.add(url + "|" + theme); // 테마 정보를 함께 저장
                                }
                                filterByTheme(spinnerNav.getSelectedItem().toString()); // 현재 선택된 테마로 필터링
                            });
                        }
                    }
                });
            }
        }
    }

    // 테마에 따라 이미지를 필터링
    private void filterByTheme(String theme) {
        imageUrls.clear();

        if (theme.equals("전체")) {
            // 전체 이미지를 보여줌
            for (String url : allImageUrls) {
                imageUrls.add(url.split("\\|")[0]); // 테마 정보 제거 후 URL만 사용
            }
        } else {
            // 선택된 테마에 해당하는 이미지만 필터링
            for (String url : allImageUrls) {
                String[] parts = url.split("\\|");
                if (parts[1].equals(theme)) {
                    imageUrls.add(parts[0]); // URL만 추가
                }
            }
        }
        // 어댑터에 변경사항 반영
        puzzleAdapter.notifyDataSetChanged();
    }

    @Override
    public void loadUserInfo(ImageView profile, TextView name, TextView nickname) {
        userInfo.loadUserInfo(profile, name, nickname);
    }

    // 효과음
    public void sound() {
        isSoundOn = pref.getBoolean("on&off2", true);
        Intent intent = new Intent(this, SoundService.class);
        if (isSoundOn) startService(intent); // 효과음 on
        else stopService(intent);            // 효과음 off
    }
}
