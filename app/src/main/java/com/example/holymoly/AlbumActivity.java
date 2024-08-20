package com.example.holymoly;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AlbumActivity extends AppCompatActivity implements UserInfoLoader{
    private TextView name;
    private ImageView profile;
    private ImageButton btnhome, btntrophy, btnsetting;
    private Spinner spinnerNav;

    private UserInfo userInfo = new UserInfo();

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();

    private TextView tvNone, tvPush, tvMakeFirst;
    private RecyclerView recyclerView;
    private BookAdapter bookAdapter;
    private List<String> imageUrls, titles;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        name = findViewById(R.id.mini_name);
        profile = findViewById(R.id.mini_profile);
        btnhome = findViewById(R.id.ib_homebutton);
        btntrophy = findViewById(R.id.ib_trophy);
        btnsetting = findViewById(R.id.ib_setting);
        spinnerNav = findViewById(R.id.theme_spinner);

        loadUserInfo(profile, name);

        // TextView 초기화
        tvNone = findViewById(R.id.tv_none);
        tvPush = findViewById(R.id.tv_push);
        tvMakeFirst = findViewById(R.id.tv_makefirst);

        recyclerView = findViewById(R.id.recyclerView);
        // 가로 방향 스크롤 설정
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        imageUrls = new ArrayList<>();
        titles = new ArrayList<>();
        bookAdapter = new BookAdapter(this, imageUrls, titles);
        recyclerView.setAdapter(bookAdapter);

        loadImages();

        btnhome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AlbumActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        btntrophy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AlbumActivity.this, TrophyActivity.class);
                startActivity(intent);
            }
        });

        btnsetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AlbumActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });

        bookAdapter.setOnItemClickListener((position, imageUrl) -> {
            // 클릭된 이미지의 URL을 이용해 작업 수행
            Intent intent = new Intent(AlbumActivity.this, ReadBookActivity.class);
            intent.putExtra("imageUrl", imageUrl);
            startActivity(intent);
        });

        // 스피너 설정
        String[] items = { "전체", "바다", "궁전", "숲", "마을", "우주", "사막", "커스텀" };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.thema_text, items);
        adapter.setDropDownViewResource(R.layout.thema_text);
        spinnerNav.setAdapter(adapter);

        spinnerNav.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 아무것도 선택되지 않았을 때의 행동
            }
        });
    }
    // 이미지 가져옴
    private void loadImages() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("covers");

        storageRef.listAll().addOnSuccessListener(listResult -> {
            if (!listResult.getItems().isEmpty()) {
                // 표지가 있으면 TextView를 숨기고 RecyclerView를 보이게 설정
                tvNone.setVisibility(View.GONE);
                tvPush.setVisibility(View.GONE);
                tvMakeFirst.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }

            // 파일들을 리스트로 가져옴
            List<StorageReference> items = listResult.getItems();
            // 파일 이름을 기준으로 정렬
            Collections.sort(items, new Comparator<StorageReference>() {
                @Override
                public int compare(StorageReference o1, StorageReference o2) {
                    // 파일 이름에서 인덱스 추출
                    int index1 = extractIndex(o1.getName());
                    int index2 = extractIndex(o2.getName());
                    return Integer.compare(index1, index2);
                }
            });

            // 정렬된 순서대로 이미지 로드
            for (StorageReference item : items) {
                String img = item.getName();
                // 파일 이름이 현재 사용자의 ID로 시작하는 경우
                if (img.startsWith(user.getUid())) {
                    item.getDownloadUrl().addOnSuccessListener(uri -> {
                        String url = uri.toString();
                        String title = extractTitle(img); // 파일 이름에서 제목 추출
                        // URL이 리스트에 없으면 추가
                        if (!imageUrls.contains(url)) {  // 중복 방지
                            imageUrls.add(url);
                            titles.add(title);
                            bookAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }
    // 파일 이름에 따라 '_'로 분할해 숫자를 알아냄
    private int extractIndex(String fileName) {
        try {
            fileName.startsWith(user.getUid());
            // 파일 이름을 "_"로 분할하여 숫자 추출
            String[] parts = fileName.split("_");
            return Integer.parseInt(parts[2]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            // 숫자 추출에 실패하면 기본값 0 반환
            return 0;
        }
    }
    // 파일 이름에 따라 '_'로 분할해 제목 알아냄
    private String extractTitle(String fileName) {
        String[] parts = fileName.split("_");
        // 제목 부분에서 .png 제거 후 반환
        return parts[3].replace(".png", "");
    }

    @Override
    public void loadUserInfo(ImageView profile, TextView name) {
        userInfo.loadUserInfo(profile, name);
    }
}