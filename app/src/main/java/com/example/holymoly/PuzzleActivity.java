package com.example.holymoly;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
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
    private TextView name;
    private ImageView profile;
    private UserInfo userInfo = new UserInfo();

    /* DB */
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    private List<Bitmap> bitmapList = new ArrayList<>();
    private List<String> imageUrls = new ArrayList<>();
    private Spinner spinnerNav;

    private GridLayout imagesContainer;
    private ImageView iv, plusImageView; // 현재 plusImageView를 참조하는 변수
    private int imageCnt = 0; // 현재 이미지 개수
    private Bitmap selectedBitmap; // 선택된 비트맵 저장
    private String selectedImageUrl; // 선택된 이미지의 URL 저장

    private Map<Integer, List<Bitmap>> themesMap = new HashMap<>(); //
    private int currentThemeId = -1; // 현재 테마 id

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle);

        iv = findViewById(R.id.iv);
        iv.setOnClickListener(this);

        // 상단 프로필 로딩
        name = findViewById(R.id.mini_name);
        profile = findViewById(R.id.mini_profile);
        loadUserInfo(profile, name);

        // Firebase 초기화
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        imagesContainer = findViewById(R.id.images_container);

        String[] items = { "전체", "바다", "궁전", "숲", "마을", "우주", "사막", "커스텀", "내 사진" };
        // 스피너 설정
        spinnerNav = findViewById(R.id.thema_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.thema_text, items);
        adapter.setDropDownViewResource(R.layout.thema_text);
        spinnerNav.setAdapter(adapter);

        spinnerNav.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectSection(position);
                if (currentThemeId != position) {
                    currentThemeId = position;
                    loadImagesByTheme(); // 현재 테마에 맞는 저장된 이미지 로드
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 아무것도 선택되지 않았을 때의 행동
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (selectedBitmap != null && selectedImageUrl != null) { // 선택된 이미지가 있는 경우
            Intent intent = new Intent(PuzzleActivity.this, SelectPuzzleActivity.class);
            intent.putExtra("selectedImage", selectedImageUrl); // Intent에 이미지 바이트 배열을 추가
            startActivity(intent);
        } else loadImages(); // 이미지가 설정되지 않은 경우
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
            case 8: secTitle.setText("- 내 사진 -"); break;
        }
        if(id != 0) resetImages();
    }


    private void loadImages() {
        String selectedTheme = spinnerNav.getSelectedItem().toString();
        StorageReference themeRef = storageRef.child("background/" + selectedTheme + "/");
        if ("전체".equals(selectedTheme)) {
            loadAllThemesImages();
            return;
        }

        themeRef.listAll().addOnSuccessListener(listResult -> {
            bitmapList.clear(); // 리스트 초기화
            List<StorageReference> items = listResult.getItems();

            // 현재 사용자의 이미지 리스트
            List<StorageReference> userItems = new ArrayList<>();
            for (StorageReference item : items) {
                if (item.getName().startsWith(user.getUid())) userItems.add(item);
            }

            if (userItems.isEmpty()) {
                Toast.makeText(this, "불러올 이미지가 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            final long MEGABYTE = 2 * 1024 * 1024; // 2MB
            for (StorageReference item : userItems) {
                // URL을 얻기 위한 호출
                item.getDownloadUrl().addOnSuccessListener(uri -> {
                    imageUrls.add(uri.toString()); // URL을 리스트에 추가

                    item.getBytes(MEGABYTE).addOnSuccessListener(bytes -> {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        bitmapList.add(bitmap);

                        if (bitmapList.size() == userItems.size())
                            showImagesDialog(bitmapList);
                    });
                });
            }
        });
    }

    private void showImagesDialog(List<Bitmap> bitmapList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_image_list, null);
        builder.setView(dialogView);

        RecyclerView recyclerView = dialogView.findViewById(R.id.dialog_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        PuzzleImageAdapter adapter = new PuzzleImageAdapter(this, bitmapList, new PuzzleImageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Bitmap bitmap) {
                selectedBitmap = bitmap; // 선택된 이미지 설정

                AlertDialog applyDialog = new AlertDialog.Builder(PuzzleActivity.this)
                        .setTitle("이미지를 추가하세요")
                        .setMessage("정말 이 이미지를 추가하시겠습니까?")
                        .setPositiveButton("추가하기", (dialog, which) -> {
                            if (imageCnt == 0) {  // 첫 번째 이미지
                                iv.setImageBitmap(bitmap);
                                iv.setPadding(5,5,5,5);
                                iv.setBackgroundResource(R.drawable.puzzle_stroke_box);
                                imageCnt++;
                            } else {
                                plusImageView.setImageBitmap(bitmap);
                                plusImageView.setPadding(5,5,5,5);
                                plusImageView.setBackgroundResource(R.drawable.puzzle_stroke_box);
                            }

                            saveImagesByTheme(bitmap); // 현재 테마에 추가된 이미지 저장
                            addImageView(); // 새로운 ImageView 추가
                            dialog.dismiss(); // 다이얼로그 닫기
                        })
                        .setNegativeButton("돌아가기", (dialog, which) -> dialog.dismiss())
                        .create();

                applyDialog.show();
            }
        });
        recyclerView.setAdapter(adapter);

        builder.setPositiveButton("완료", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void addImageView() {
        plusImageView = new ImageView(PuzzleActivity.this);

        // 320 dp x 200 dp를 픽셀로 변환
        int widthPx = dpToPx(320);
        int heightPx = dpToPx(200);

        // 현재 추가된 이미지 수에 따라 위치 계산
        int column; // 열
        int row;    // 행

        if (imageCnt < 3) {
            // 처음 3개의 이미지는 row=0에 위치
            column = imageCnt;
            row = 0;
        } else if (imageCnt < 6) {
            // 다음 3개의 이미지는 row=1에 위치
            column = imageCnt - 3;
            row = 1;
        } else {
            // 7번째부터는 row=0, 1 사이를 번갈아가며 배치, column는 증가
            row = imageCnt % 2;  // 0과 1을 번갈아가면서
            column = imageCnt / 2;  // 이미지 개수가 7개 이상일 때 column 증가
        }

        // LayoutParams 설정
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = widthPx;
        params.height = heightPx;

        // margin 적용
        params.setMargins(0, 0, dpToPx(50), dpToPx(50));
        params.columnSpec = GridLayout.spec(column);
        params.rowSpec = GridLayout.spec(row);
        plusImageView.setLayoutParams(params);

        plusImageView.setImageResource(R.drawable.iv_plusimg);
        plusImageView.setBackgroundResource(R.drawable.puzzle_stroke_box); // 테두리 추가

        // plusImg 클릭 시 loadImages 호출
        plusImageView.setOnClickListener(v -> loadImages());

        // GridLayout에 ic_plusImg 추가
        imagesContainer.addView(plusImageView);
        // 이미지가 추가될 때마다 카운트 증가
        imageCnt++;
    }

    // dp를 px로 변환
    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    // 각 테마에 맞는 이미지 저장 (테마 ID에 따라)
    private void saveImagesByTheme(Bitmap bitmap) {
        if (!themesMap.containsKey(currentThemeId)) {
            themesMap.put(currentThemeId, new ArrayList<>());
        }
        themesMap.get(currentThemeId).add(bitmap);
    }

    private void loadAllThemesImages() {
        // Storage에서 background 폴더의 모든 폴더 및 파일을 가져옴
        StorageReference backgroundRef = storageRef.child("background/");
        backgroundRef.listAll().addOnSuccessListener(listResult -> {
            bitmapList.clear(); // 리스트 초기화
            List<StorageReference> folders = listResult.getPrefixes(); // 폴더들
            List<StorageReference> allImages = new ArrayList<>();

            // 모든 폴더에서 이미지 참조 가져오기
            for (StorageReference folder : folders) {
                folder.listAll().addOnSuccessListener(folderResult -> {
                    allImages.addAll(folderResult.getItems()); // 모든 이미지를 리스트에 추가

                    // 모든 폴더에서 이미지 수집이 완료되었을 때
                    if (folders.indexOf(folder) == folders.size() - 1) {
                        final long MEGABYTE = 2 * 1024 * 1024; // 2MB로 이미지 크기 제한
                        for (StorageReference item : allImages) {
                            // 파일 이름이 현재 사용자 UID로 시작하는지 확인
                            if (item.getName().startsWith(user.getUid())) {
                                item.getBytes(MEGABYTE).addOnSuccessListener(bytes -> {
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    bitmapList.add(bitmap);

                                    // 각 테마에 이미지 저장
                                    saveImagesByTheme(bitmap);
                                    // 모든 이미지 로딩이 완료되면 다이얼로그 표시
                                    if (bitmapList.size() == allImages.size()) {
                                        showImagesDialog(bitmapList);

                                    }
                                });
                            }
                        }
                    }
                });
            }
        });
    }

    private void loadImagesByTheme() {
        List<Bitmap> savedBitmaps = themesMap.get(currentThemeId);
        if (savedBitmaps != null) {
            for (Bitmap bitmap : savedBitmaps) {
                if (imageCnt == 0) {
                    iv.setImageBitmap(bitmap);
                    imageCnt++;
                } else {
                    plusImageView.setImageBitmap(bitmap);
                    plusImageView.setBackgroundResource(R.drawable.puzzle_stroke_box);
                }
                addImageView(); // 새로운 ImageView 추가
            }
        }
    }
    private void resetImages() {
        // GridLayout에서 iv를 제외한 모든 View 제거
        for (int i = imagesContainer.getChildCount() - 1; i >= 0; i--) {
            View child = imagesContainer.getChildAt(i);
            if (child != iv) {
                imagesContainer.removeViewAt(i);
            }
        }
        iv.setImageResource(R.drawable.iv_plusimg);

        // 이미지 추가 관련 변수 초기화
        imageCnt = 0;
    }
    @Override
    public void loadUserInfo(ImageView profile, TextView name) {
        userInfo.loadUserInfo(profile, name);
    }
}
