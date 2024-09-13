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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AlbumActivity extends AppCompatActivity implements UserInfoLoader{
    private TextView name, nickname;
    private ImageView profile;
    private ImageButton btnhome, btntrophy, btnsetting;
    private Spinner spinnerNav;

    private UserInfo userInfo = new UserInfo();

    /* Firebase 초기화 */
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference().child("covers");
    private String uid = user.getUid();

    private TextView tvNone, tvPush, tvMakeFirst;
    private RecyclerView recyclerView;
    private BookAdapter bookAdapter;

    // 전체 이미지를 저장하는 리스트 (필터링에 사용)
    private List<String> allImageUrls, allTitles, allImgNames;
    // 이미지의 url, 제목, 파일명
    private List<String> imageUrls, titles, imgNames;

    /* 효과음 */
    private SharedPreferences pref;
    private boolean isSoundOn;
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        pref = getSharedPreferences("music", MODE_PRIVATE); // 효과음 초기화

        name = findViewById(R.id.mini_name);
        nickname = findViewById(R.id.mini_nickname);
        profile = findViewById(R.id.mini_profile);
        btnhome = findViewById(R.id.ib_homebutton);
        btntrophy = findViewById(R.id.ib_trophy);
        btnsetting = findViewById(R.id.ib_setting);
        spinnerNav = findViewById(R.id.theme_spinner);

        loadUserInfo(profile, name, nickname);

        // TextView 초기화
        tvNone = findViewById(R.id.tv_none);
        tvPush = findViewById(R.id.tv_push);
        tvMakeFirst = findViewById(R.id.tv_makefirst);

        recyclerView = findViewById(R.id.recyclerView);
        // 가로 방향 스크롤 설정
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        allImageUrls = new ArrayList<>();
        allTitles = new ArrayList<>();
        allImgNames = new ArrayList<>();

        imageUrls = new ArrayList<>();
        titles = new ArrayList<>();
        imgNames = new ArrayList<>();

        bookAdapter = new BookAdapter(this, imageUrls, titles);
        recyclerView.setAdapter(bookAdapter);

        loadImages();

        btnhome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound();
                Intent intent = new Intent(AlbumActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        btntrophy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound();
                Intent intent = new Intent(AlbumActivity.this, TrophyActivity.class);
                startActivity(intent);
            }
        });

        btnsetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound();
                Intent intent = new Intent(AlbumActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });

        bookAdapter.setOnItemClickListener((position, imageUrl) -> {
            // 클릭된 이미지의 파일명을 인텐트에 추가
            String imgName = imgNames.get(position);
            sound();
            Intent intent = new Intent(AlbumActivity.this, ReadBookActivity.class);
            intent.putExtra("imgName", imgName);
            startActivity(intent);
        });

        // 스피너 설정
        String[] items = { "전체", "바다", "궁전", "숲", "마을", "우주", "사막", "커스텀" , "개인"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.thema_text, items);
        adapter.setDropDownViewResource(R.layout.thema_text);
        spinnerNav.setAdapter(adapter);

        spinnerNav.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedTheme = items[position];
                filterImagesByTheme(selectedTheme);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 아무것도 선택되지 않았을 때의 행동
            }
        });
    }
    // 이미지 가져옴
    private void loadImages() {
        CollectionReference imagesRef = db.collection("covers").document(user.getUid()).collection("filename");

        // Firestore에서 timestamp을 통해 최신순으로 정렬
        imagesRef.orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> sortedImageNames = new ArrayList<>();

                    // 정렬된 파일 이름 저장
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        if (document.contains("timestamp")) {
                            String fileName = document.getId();  // 파일 이름
                            sortedImageNames.add(fileName);
                        }
                    }

                    // 해당 이름으로 이미지 URL 가져오기
                    storageRef.listAll().addOnSuccessListener(listResult -> {
                        List<StorageReference> items = listResult.getItems();
                        boolean hasUserImages = false;

                        // 사용자의 id로 시작하는 이미지가 있는지 확인
                        for (StorageReference item : items) {
                            if (item.getName().startsWith(uid)) {
                                hasUserImages = true;
                                break;
                            }
                        }

                        // 사용자 id로 시작하는 이미지가 있으면 TextView를 숨기고 RecyclerView 보이게 설정
                        if (hasUserImages) {
                            tvNone.setVisibility(View.GONE);
                            tvPush.setVisibility(View.GONE);
                            tvMakeFirst.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }

                        // Firestore에서 정렬된 순서에 맞춰 이미지 로드
                        for (String sortedImageName : sortedImageNames) {
                            for (StorageReference item : items) {
                                String img = item.getName();
                                // 파일 이름이 현재 사용자의 ID로 시작하고, timestamp로 정렬된 이름에 포함된 경우
                                if (img.startsWith(uid) && img.equals(sortedImageName)) {
                                    item.getDownloadUrl().addOnSuccessListener(uri -> {
                                        String url = uri.toString();
                                        String title = extractTitle(img); // 파일 이름에서 제목 추출

                                        // URL이 리스트에 없으면 추가 (중복 방지)
                                        if (!allImageUrls.contains(url)) {
                                            allImageUrls.add(url);
                                            allTitles.add(title);
                                            allImgNames.add(img);
                                        }

                                        // 테마별로 필터링 및 정렬
                                        filterImagesByTheme(spinnerNav.getSelectedItem().toString());
                                    });
                                }
                            }
                        }
                    });
                });
    }


    // 파일 이름에 따라 '_'로 분할해 숫자를 알아냄
    private int extractIndex(String fileName) {
        try {
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

    // 선택된 테마에 따라 이미지를 필터링
    private void filterImagesByTheme(String theme) {
        imageUrls.clear();
        titles.clear();
        imgNames.clear();

        if (theme.equals("전체")) {
            // 전체일 때 timestamp로 최신순으로 정렬된 순서를 유지
            for (int i = 0; i < allImgNames.size(); i++) {
                imageUrls.add(allImageUrls.get(i));
                titles.add(allTitles.get(i));
                imgNames.add(allImgNames.get(i));
            }
        } else {
            // 테마 선택 시 해당 테마의 이미지들을 오름차순으로 정렬
            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < allImgNames.size(); i++) {
                String imgName = allImgNames.get(i);
                String[] parts = imgName.split("_");  // 파일 이름에 따라 '_'로 분할해 테마 알아냄
                String imgTheme = parts[1];

                if (imgTheme.equals(theme)) {
                    imageUrls.add(allImageUrls.get(i));
                    titles.add(allTitles.get(i));
                    imgNames.add(allImgNames.get(i));
                    // 오름차순 정렬을 위한 번호 저장
                    indices.add(extractIndex(imgName));
                }
            }

            // 번호에 따라 오름차순으로 정렬
            List<String> sortedImageUrls = new ArrayList<>();
            List<String> sortedTitles = new ArrayList<>();
            List<String> sortedImgNames = new ArrayList<>();

            // 번호에 따라 재정렬된 리스트를 생성
            while (!indices.isEmpty()) {
                int minIndex = indices.indexOf(Collections.min(indices));
                sortedImageUrls.add(imageUrls.get(minIndex));
                sortedTitles.add(titles.get(minIndex));
                sortedImgNames.add(imgNames.get(minIndex));

                imageUrls.remove(minIndex);
                titles.remove(minIndex);
                imgNames.remove(minIndex);
                indices.remove(minIndex);
            }
            imageUrls.addAll(sortedImageUrls);
            titles.addAll(sortedTitles);
            imgNames.addAll(sortedImgNames);
        }

        // 필터링 후 imageUrls 리스트에 따라 RecyclerView 설정
        if (imageUrls.isEmpty()) {
            // 이미지가 없을 경우, RecyclerView를 숨기고 TextView들을 보이게 설정
            recyclerView.setVisibility(View.GONE);
            tvNone.setVisibility(View.VISIBLE);
            tvPush.setVisibility(View.VISIBLE);
            tvMakeFirst.setVisibility(View.VISIBLE);
        } else {
            // 이미지가 있을 경우, TextView를 숨기고 RecyclerView를 보이게 설정
            tvNone.setVisibility(View.GONE);
            tvPush.setVisibility(View.GONE);
            tvMakeFirst.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        bookAdapter.notifyDataSetChanged();
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