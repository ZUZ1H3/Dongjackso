package com.example.holymoly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class WorldActivity extends AppCompatActivity implements View.OnClickListener{
    /* 우측 상단 미니 버튼 */
    private ImageButton search, home, mypage;
    /* 배너 */
    private EditText find;
    private ImageView book, cover;
    private TextView mainTitle, writer, title, explain;
    private ImageButton back, next, mini_back, mini_next;

    private TextView best, latest, page_1, page_2, page_3, page_4, page_5;
    private StrokeText all, sea, castle, forest, village, space, desert, custom, alone;
    private ImageView[] covers = new ImageView[8];  // 표지 이미지
    private TextView[] titles = new TextView[8];    // 책 제목 text
    private ImageView[] hearts = new ImageView[8];  // 하트 이미지
    private TextView[] likes = new TextView[8]; // 좋아요 수 text
    private boolean[] isLiked = new boolean[8]; // 이미지에 대한 좋아요 상태
    private int[] cntLikes = new int[8];        // 좋아요 개수

    /* DB */
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();
    private String uid = user.getUid();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference().child("covers");

    /* 효과음 */
    private SharedPreferences pref;
    private boolean isSoundOn;

    private static final int IMAGES_PER_PAGE = 8;
    private List<Pair<String, Date>> timestampList = new ArrayList<>();
    private List<Pair<String, Integer>> likesList = new ArrayList<>();
    private boolean[] isCoverSet = new boolean[8]; // 기본값은 false
    private boolean isBestMode = true;

    /* 페이지 */
    private List<TextView> pageText;
    private int currentPage = 1;
    private int bannerPage = 1;
    private int totalPages;

    /* 중복 초기화 */
    private Set<String> loadedByLikes = new HashSet<>();
    private Set<String> loadedByLatest = new HashSet<>();

    private Gemini gemini = new Gemini();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_world);

        init();

        // 각 페이지 번호를 클릭했을 때
        for (int i = 0; i < pageText.size(); i++) {
            final int index = i;
            pageText.get(i).setOnClickListener(v -> {
                int clicked = index + 1;
                updatePage(clicked);
            });
        }

        // 실행될 때 베스트 페이지 우선
        bestImages("전체", () -> {
            updatePage(currentPage);  // 페이지 업데이트
        });
        Top3Images(); // 명예의 전당 배너 이미지
    }

    // 초기화
    private void init() {
        pref = getSharedPreferences("music", MODE_PRIVATE); // 효과음 초기화
        search = findViewById(R.id.ib_search);
        home = findViewById(R.id.ib_homebutton);
        find = findViewById(R.id.et_find);
        mypage = findViewById(R.id.ib_mypage);
        book = findViewById(R.id.book);
        cover = findViewById(R.id.bookCover);
        mainTitle = findViewById(R.id.title);
        writer = findViewById(R.id.bookWriter);
        title = findViewById(R.id.bookTitle);
        explain = findViewById(R.id.bookExplain);
        back = findViewById(R.id.backPage);
        next = findViewById(R.id.nextPage);
        mini_back = findViewById(R.id.mini_backPage);
        mini_next = findViewById(R.id.mini_nextPage);
        best = findViewById(R.id.btn_best);
        latest = findViewById(R.id.btn_latest);
        all = findViewById(R.id.thema_all);
        sea = findViewById(R.id.thema_sea);
        castle = findViewById(R.id.thema_castle);
        forest = findViewById(R.id.thema_forest);
        village = findViewById(R.id.thema_village);
        space = findViewById(R.id.thema_space);
        desert = findViewById(R.id.thema_desert);
        custom = findViewById(R.id.thema_custom);
        alone = findViewById(R.id.thema_alone);
        page_1 = findViewById(R.id.page_1);
        page_2 = findViewById(R.id.page_2);
        page_3 = findViewById(R.id.page_3);
        page_4 = findViewById(R.id.page_4);
        page_5 = findViewById(R.id.page_5);

        /* 8개의 ImageView와 TextView */
        covers[0] = findViewById(R.id.iv_covers1);
        covers[1] = findViewById(R.id.iv_covers2);
        covers[2] = findViewById(R.id.iv_covers3);
        covers[3] = findViewById(R.id.iv_covers4);
        covers[4] = findViewById(R.id.iv_covers5);
        covers[5] = findViewById(R.id.iv_covers6);
        covers[6] = findViewById(R.id.iv_covers7);
        covers[7] = findViewById(R.id.iv_covers8);

        titles[0] = findViewById(R.id.tv_title1);
        titles[1] = findViewById(R.id.tv_title2);
        titles[2] = findViewById(R.id.tv_title3);
        titles[3] = findViewById(R.id.tv_title4);
        titles[4] = findViewById(R.id.tv_title5);
        titles[5] = findViewById(R.id.tv_title6);
        titles[6] = findViewById(R.id.tv_title7);
        titles[7] = findViewById(R.id.tv_title8);

        hearts[0] = findViewById(R.id.heart1);
        hearts[1] = findViewById(R.id.heart2);
        hearts[2] = findViewById(R.id.heart3);
        hearts[3] = findViewById(R.id.heart4);
        hearts[4] = findViewById(R.id.heart5);
        hearts[5] = findViewById(R.id.heart6);
        hearts[6] = findViewById(R.id.heart7);
        hearts[7] = findViewById(R.id.heart8);

        likes[0] = findViewById(R.id.like1);
        likes[1] = findViewById(R.id.like2);
        likes[2] = findViewById(R.id.like3);
        likes[3] = findViewById(R.id.like4);
        likes[4] = findViewById(R.id.like5);
        likes[5] = findViewById(R.id.like6);
        likes[6] = findViewById(R.id.like7);
        likes[7] = findViewById(R.id.like8);

        // 페이지를 리스트에 추가
        pageText = new ArrayList<>();
        pageText.add(page_1);
        pageText.add(page_2);
        pageText.add(page_3);
        pageText.add(page_4);
        pageText.add(page_5);

        mainTitle.setOnClickListener(this);
        search.setOnClickListener(this);
        home.setOnClickListener(this);
        mypage.setOnClickListener(this);
        back.setOnClickListener(this);
        next.setOnClickListener(this);
        mini_back.setOnClickListener(this);
        mini_next.setOnClickListener(this);
        best.setOnClickListener(this);
        latest.setOnClickListener(this);
        all.setOnClickListener(this);
        sea.setOnClickListener(this);
        castle.setOnClickListener(this);
        forest.setOnClickListener(this);
        village.setOnClickListener(this);
        space.setOnClickListener(this);
        desert.setOnClickListener(this);
        custom.setOnClickListener(this);
        alone.setOnClickListener(this);
        page_1.setOnClickListener(this);
        page_2.setOnClickListener(this);
        page_3.setOnClickListener(this);
        page_4.setOnClickListener(this);
        page_5.setOnClickListener(this);

        for(int i = 0; i< covers.length; i++) {
            covers[i].setOnClickListener(this);
        }
        for(int i = 0; i < hearts.length; i++) {
            hearts[i].setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        sound();
        if(v.getId() == R.id.ib_search) {
            String searchTitle = find.getText().toString().trim(); // 검색어 갖고 옴

            // 값이 비어있지 않은지 확인해서 제목으로 이미지 검색
            if(!searchTitle.isEmpty() && isValidSearch(searchTitle)) {
                findImagesByTitle(searchTitle);
            }
            else if(searchTitle.isEmpty()){ // 검색어가 없을 때
                Toast.makeText(this, "검색할 제목을 입력하세요.", Toast.LENGTH_SHORT).show();
            } // 검색어가 한 글자일 때
            else Toast.makeText(this, "두글자 이상 검색해주세요", Toast.LENGTH_SHORT).show();
        }
        else if(v.getId() == R.id.title) {
            bestImages("전체", () -> {
                updatePage(currentPage);  // 페이지 업데이트
            });
            find.setText("");
        }
        else if(v.getId() == R.id.ib_homebutton) {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        }
        else if(v.getId() == R.id.ib_mypage) {
            Intent intent = new Intent(this, MyPageActivity.class);
            startActivity(intent);
        }
        else if(v.getId() == R.id.backPage) {
            if(bannerPage > 1) {
                bannerPage--;
            } else bannerPage = 3; // 1보다 작으면 3으로
            loadTop3Images(likesList, bannerPage);
        }
        else if(v.getId() == R.id.nextPage) {
            if(bannerPage < 3) {
                bannerPage++;
            } else bannerPage = 1; // 3을 초과하면 다시 1로
            loadTop3Images(likesList, bannerPage);
        }
        else if(v.getId() == R.id.mini_backPage) {
            if(currentPage > 1) {
                currentPage--;
                updatePage(currentPage);
            }
        }
        else if(v.getId() == R.id.mini_nextPage) {
            if(currentPage < totalPages) {
                currentPage++;
                updatePage(currentPage);
            }
        }
        else if(v.getId() == R.id.btn_best) {
            best.setTextColor(Color.WHITE);
            latest.setTextColor(Color.parseColor("#80FFFFFF"));
            isBestMode = true;

            all.setStroke(true);
            sea.setStroke(false);
            castle.setStroke(false);
            forest.setStroke(false);
            village.setStroke(false);
            space.setStroke(false);
            desert.setStroke(false);
            custom.setStroke(false);
            alone.setStroke(false);

            currentPage = 1;
            bestImages("전체", () -> {
                updatePage(currentPage);
            });
        }
        else if(v.getId() == R.id.btn_latest) {
            latest.setTextColor(Color.WHITE);
            best.setTextColor(Color.parseColor("#80FFFFFF"));
            isBestMode = false;

            all.setStroke(true);
            sea.setStroke(false);
            castle.setStroke(false);
            forest.setStroke(false);
            village.setStroke(false);
            space.setStroke(false);
            desert.setStroke(false);
            custom.setStroke(false);
            alone.setStroke(false);

            currentPage = 1;
            latestImages("전체", () -> {
                updatePage(currentPage);
            });
        }
        else if(v.getId() == R.id.thema_all) {
            all.setStroke(true);
            sea.setStroke(false);
            castle.setStroke(false);
            forest.setStroke(false);
            village.setStroke(false);
            space.setStroke(false);
            desert.setStroke(false);
            custom.setStroke(false);
            alone.setStroke(false);

            currentPage = 1;
            if(isBestMode) {
                bestImages("전체", () -> {
                    updatePage(currentPage);
                });
            }
            else {
                latestImages("전체", () -> {
                    updatePage(currentPage);
                });
            }
        }
        else if(v.getId() == R.id.thema_sea) {
            all.setStroke(false);
            sea.setStroke(true);
            castle.setStroke(false);
            forest.setStroke(false);
            village.setStroke(false);
            space.setStroke(false);
            desert.setStroke(false);
            custom.setStroke(false);
            alone.setStroke(false);

            currentPage = 1;
            if(isBestMode) {
                bestImages("바다", () -> {
                    updatePage(currentPage);
                });
            }
            else {
                latestImages("바다", () -> {
                    updatePage(currentPage);
                });
            }
        }
        else if(v.getId() == R.id.thema_castle) {
            all.setStroke(false);
            sea.setStroke(false);
            castle.setStroke(true);
            forest.setStroke(false);
            village.setStroke(false);
            space.setStroke(false);
            desert.setStroke(false);
            custom.setStroke(false);
            alone.setStroke(false);

            currentPage = 1;
            if(isBestMode) {
                bestImages("궁전", () -> {
                    updatePage(currentPage);
                });
            }
            else {
                latestImages("궁전", () -> {
                    updatePage(currentPage);
                });
            }
        }
        else if(v.getId() == R.id.thema_forest) {
            all.setStroke(false);
            sea.setStroke(false);
            castle.setStroke(false);
            forest.setStroke(true);
            village.setStroke(false);
            space.setStroke(false);
            desert.setStroke(false);
            custom.setStroke(false);
            alone.setStroke(false);

            currentPage = 1;
            if(isBestMode) {
                bestImages("숲", () -> {
                    updatePage(currentPage);
                });
            }
            else {
                latestImages("숲", () -> {
                    updatePage(currentPage);
                });
            }
        }
        else if(v.getId() == R.id.thema_village) {
            all.setStroke(false);
            sea.setStroke(false);
            castle.setStroke(false);
            forest.setStroke(false);
            village.setStroke(true);
            space.setStroke(false);
            desert.setStroke(false);
            custom.setStroke(false);
            alone.setStroke(false);

            currentPage = 1;
            if(isBestMode) {
                bestImages("마을", () -> {
                    updatePage(currentPage);
                });
            }
            else {
                latestImages("마을", () -> {
                    updatePage(currentPage);
                });
            }
        }
        else if(v.getId() == R.id.thema_space) {
            all.setStroke(false);
            sea.setStroke(false);
            castle.setStroke(false);
            forest.setStroke(false);
            village.setStroke(false);
            space.setStroke(true);
            desert.setStroke(false);
            custom.setStroke(false);
            alone.setStroke(false);

            currentPage = 1;
            if(isBestMode) {
                bestImages("우주", () -> {
                    updatePage(currentPage);
                });
            }
            else {
                latestImages("우주", () -> {
                    updatePage(currentPage);
                });
            }
        }
        else if(v.getId() == R.id.thema_desert) {
            all.setStroke(false);
            sea.setStroke(false);
            castle.setStroke(false);
            forest.setStroke(false);
            village.setStroke(false);
            space.setStroke(false);
            desert.setStroke(true);
            custom.setStroke(false);
            alone.setStroke(false);

            currentPage = 1;
            if(isBestMode) {
                bestImages("사막", () -> {
                    updatePage(currentPage);
                });
            }
            else {
                latestImages("사막", () -> {
                    updatePage(currentPage);
                });
            }
        }
        else if(v.getId() == R.id.thema_custom) {
            all.setStroke(false);
            sea.setStroke(false);
            castle.setStroke(false);
            forest.setStroke(false);
            village.setStroke(false);
            space.setStroke(false);
            desert.setStroke(false);
            custom.setStroke(true);
            alone.setStroke(false);

            currentPage = 1;
            if(isBestMode) {
                bestImages("커스텀", () -> {
                    updatePage(currentPage);
                });
            }
            else {
                latestImages("커스텀", () -> {
                    updatePage(currentPage);
                });
            }
        }
        else if(v.getId() == R.id.thema_alone) {
            all.setStroke(false);
            sea.setStroke(false);
            castle.setStroke(false);
            forest.setStroke(false);
            village.setStroke(false);
            space.setStroke(false);
            desert.setStroke(false);
            custom.setStroke(false);
            alone.setStroke(true);

            currentPage = 1;
            if(isBestMode) {
                bestImages("개인", () -> {
                    updatePage(currentPage);
                });
            }
            else {
                latestImages("개인", () -> {
                    updatePage(currentPage);
                });
            }
        }

        int index = -1;
        if(v.getId() == R.id.heart1) index = 0;
        else if(v.getId() == R.id.heart2) index = 1;
        else if(v.getId() == R.id.heart3) index = 2;
        else if(v.getId() == R.id.heart4) index = 3;
        else if(v.getId() == R.id.heart5) index = 4;
        else if(v.getId() == R.id.heart6) index = 5;
        else if(v.getId() == R.id.heart7) index = 6;
        else if(v.getId() == R.id.heart8) index = 7;
        if(index != -1) toggleLike(index);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(isBestMode) {
            bestImages("전체", () -> {
                updatePage(currentPage);
            });
        }
        else {
            latestImages("전체", () -> {
                updatePage(currentPage);
            });
        }
        Top3Images();
    }

    // 검색어와 제목이 일치하는 이미지 찾음
    private void findImagesByTitle(String searchTitle) {
        // 검색어와 제목이 일치하는지 확인
        final boolean[] hasResults = {false};
        db.collection("covers")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // 검색된 이미지와 제목을 저장할 리스트
                        List<Pair<String, String>> searchResults = new ArrayList<>();
                        final int totalDocuments = task.getResult().size();
                        final AtomicInteger documentsProcessed = new AtomicInteger(0);

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String docId = document.getId();

                            // 하위 컬렉션 가져오기
                            db.collection("covers").document(docId).collection("filename")
                                    .get()
                                    .addOnSuccessListener(subTask -> {
                                        for (QueryDocumentSnapshot subDocument : subTask) {
                                            String subDocId = subDocument.getId();
                                            String title = extractTitle(subDocId); // 제목 알아냄

                                            // 검색어가 제목에 포함되면 일치하는 문서와 제목을 저장
                                            if (title.contains(searchTitle)) {
                                                searchResults.add(new Pair<>(subDocId, title));
                                                hasResults[0] = true;
                                            }
                                        }
                                        // 모든 문서 확인
                                        if (documentsProcessed.incrementAndGet() == totalDocuments) {
                                            if (hasResults[0]) { // 검색 결과가 있다면 이미지와 제목 로드
                                                clearCurrentImages(); // 이미지 초기화
                                                loadSearchedImages(searchResults);
                                            } else {
                                                // 결과가 없다면 검색 실패
                                                Toast.makeText(this, "검색 실패", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    // 알맞게 검색된 모든 이미지를 로드
    private void loadSearchedImages(List<Pair<String, String>> searchResults) {
        int resultCount = Math.min(searchResults.size(), covers.length); // 불러올 이미지와 커버 수를 맞춤

        for (int i = 0; i < resultCount; i++) {
            String subDocId = searchResults.get(i).first;
            String title = searchResults.get(i).second;
            String mainDocId = extractDocId(subDocId);
            StorageReference imageRef = storageRef.child(subDocId);  // Storage 경로 설정

            final int coverIndex = i; // 로컬 변수로
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(this).load(uri).into(covers[coverIndex]);
                titles[coverIndex].setText(title);
                isCoverSet[coverIndex] = true; // 이미지 불러옴
                hearts[coverIndex].setVisibility(View.VISIBLE); // 하트 보이게 설정

                // 하트 상태 확인
                DocumentReference heartRef = db.collection("covers").document(mainDocId).collection("filename").document(subDocId);
                heartRef.get().addOnSuccessListener(documentSnapshot -> {
                    List<String> likesList = (List<String>) documentSnapshot.get("likes");
                    if (likesList == null) likesList = new ArrayList<>();

                    boolean liked = likesList.contains(uid);
                    updateHeartIcon(coverIndex, liked);
                    cntLikes[coverIndex] = likesList.size();
                    likes[coverIndex].setText(String.valueOf(cntLikes[coverIndex]));
                });
            });
        }
    }

    // 한글이 두 글자 이상인지 비교
    private boolean isValidSearch(@NonNull String searchTitle) {
        return searchTitle.matches(".*[가-힣].*") && searchTitle.length() >= 2;
    }

    // 좋아요 순으로 정렬된 이미지 보여줌
    private void bestImages(String theme, Runnable onComplete) {
        loadedByLikes.clear(); // 중복 방지 초기화

        // 상위 컬렉션의 모든 문서 가져오기
        db.collection("covers").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Task<QuerySnapshot>> subTasks = new ArrayList<>();

                        // 모든 문서의 좋아요 개수를 가져오기 위한 작업 리스트
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // 상위 문서의 ID
                            String docId = document.getId();

                            // 하위 컬렉션에서 하위 문서 가져오기
                            Task<QuerySnapshot> subTask = db.collection("covers")
                                    .document(docId).collection("filename")// 하위 컬렉션 이름
                                    .whereEqualTo("upload", true).get();

                            subTasks.add(subTask);
                        }

                        // 모든 문서를 가져온 후 수행
                        Tasks.whenAllComplete(subTasks).addOnCompleteListener(subTaskResults -> {
                            likesList.clear();
                            // 각 하위 문서의 like 리스트와 like 수를 가져옴
                            for (Task<QuerySnapshot> subTask : subTasks) {
                                if (subTask.isSuccessful()) {
                                    for (QueryDocumentSnapshot subDocument : subTask.getResult()) {
                                        String subDocId = subDocument.getId();
                                        String subDocTheme = extractThema(subDocId);

                                        if (theme.equals("전체") || theme.equals(subDocTheme)) {
                                            List<String> likes = (List<String>) subDocument.get("likes");
                                            if (likes == null) likes = new ArrayList<>();

                                            int likeCount = likes.size();
                                            likesList.add(new Pair<>(subDocId, likeCount));
                                        }
                                    }
                                }
                            }
                            // 모든 데이터가 추가된 후에 정렬 수행
                            Collections.sort(likesList, (p1, p2) -> p2.second.compareTo(p1.second));

                            // 데이터 로드 후에 이미지 로드
                            if (onComplete != null) {
                                onComplete.run();
                                loadBestImages();
                            }
                        });
                    }
                });
    }

    // 좋아요 순으로 정렬된 이미지 가져옴
    private void loadBestImages() {
        // 처음과 마지막 인덱스 계산
        int startIndex = (currentPage - 1) * IMAGES_PER_PAGE;
        int endIndex = Math.min(startIndex + IMAGES_PER_PAGE, likesList.size());

        for (int i = startIndex; i < endIndex; i++) {
            String subDocId = likesList.get(i).first;

            if(loadedByLikes.contains(subDocId)) continue;

            String title = extractTitle(subDocId);
            String mainDocId = extractDocId(subDocId);
            StorageReference imageRef = storageRef.child(subDocId);  // Storage 경로 설정

            // covers 배열의 ImageView에 설정
            int coverIndex = i - startIndex; // 0부터
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(this).load(uri).into(covers[coverIndex]);
                titles[coverIndex].setText(title);
                isCoverSet[coverIndex] = true; // 불러옴
                hearts[coverIndex].setVisibility(View.VISIBLE); // 이미지 불러오면 하트 이미지 보이게 함

                // 하트 상태
                DocumentReference heartRef = db.collection("covers").document(mainDocId).collection("filename").document(subDocId);
                heartRef.get().addOnSuccessListener(documentSnapshot -> {
                    List<String> likesList = (List<String>) documentSnapshot.get("likes");
                    if (likesList == null) likesList = new ArrayList<>();

                    boolean liked = likesList.contains(uid);
                    updateHeartIcon(coverIndex, liked);
                    cntLikes[coverIndex] = likesList.size();
                    likes[coverIndex].setText(String.valueOf(cntLikes[coverIndex]));
                });
                loadedByLikes.add(subDocId); // 중복 리스트에 추가
            });

            // 이미지 클릭 리스너
            covers[coverIndex].setOnClickListener(v -> {
                Intent intent = new Intent(this, ReadBookActivity.class);
                intent.putExtra("imgName", subDocId);
                startActivity(intent);
            });
        }
    }

    // 최신순으로 정렬된 이미지 보여줌
    private void latestImages(String theme, Runnable onComplete) {
        loadedByLatest.clear(); // 중복 방지 초기화

        // 상위 컬렉션의 모든 문서 가져오기
        db.collection("covers")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Task<QuerySnapshot>> subTasks = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // 상위 문서의 ID
                            String docId = document.getId();

                            // 하위 컬렉션에서 하위 문서 가져오기
                            Task<QuerySnapshot> subTask = db.collection("covers")
                                    .document(docId).collection("filename")  // 하위 컬렉션 이름
                                    .whereEqualTo("upload", true).get();

                            subTasks.add(subTask);
                        }

                        // 모든 문서를 가져온 후 수행
                        Tasks.whenAllComplete(subTasks).addOnCompleteListener(subTaskResults -> {
                            timestampList.clear();
                            for (Task<QuerySnapshot> subTask : subTasks) {
                                if (subTask.isSuccessful()) {
                                    for (QueryDocumentSnapshot subDocument : subTask.getResult()) {
                                        // filename 문서에서 timestamp 필드 가져오기
                                        String subDocId = subDocument.getId();
                                        String subDocTheme = extractThema(subDocId);

                                        if (theme.equals("전체") || theme.equals(subDocTheme)) {
                                            Timestamp timestamp = subDocument.getTimestamp("timestamp");

                                            if (timestamp != null) {
                                                // timestamp를 Date로 변환
                                                Date date = timestamp.toDate();
                                                // 하위 문서 ID와 날짜를 리스트에 추가
                                                timestampList.add(new Pair<>(subDocId, date));
                                            }
                                        }
                                    }
                                }
                            }
                            // 모든 데이터가 추가된 후에 정렬 수행
                            Collections.sort(timestampList, (p1, p2) -> p2.second.compareTo(p1.second));

                            // 데이터 로드 후에 이미지 로드
                            if (onComplete != null) {
                                onComplete.run();
                                loadLatestImages();
                            }
                        });
                    }
                });
    }

    // 최신순으로 정렬된 이미지 가져옴
    private void loadLatestImages() {
        // 처음과 마지막 인덱스 계산
        int startIndex = (currentPage - 1) * IMAGES_PER_PAGE;
        int endIndex = Math.min(startIndex + IMAGES_PER_PAGE, timestampList.size());

        for (int i = startIndex; i < endIndex; i++) {
            String subDocId = timestampList.get(i).first;

            if(loadedByLatest.contains(subDocId)) continue;

            String title = extractTitle(subDocId);
            String mainDocId = extractDocId(subDocId);
            StorageReference imageRef = storageRef.child(subDocId);  // Storage 경로 설정

            // covers 배열의 ImageView에 설정
            int coverIndex = i - startIndex; // 0부터
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(this).load(uri).into(covers[coverIndex]);
                titles[coverIndex].setText(title);
                isCoverSet[coverIndex] = true; // 불러옴
                hearts[coverIndex].setVisibility(View.VISIBLE); // 이미지 불러오면 하트 이미지 보이게 함

                // 하트 상태
                DocumentReference heartRef = db.collection("covers").document(mainDocId).collection("filename").document(subDocId);
                heartRef.get().addOnSuccessListener(documentSnapshot -> {
                    List<String> likesList = (List<String>) documentSnapshot.get("likes");
                    if (likesList == null) likesList = new ArrayList<>();

                    boolean liked = likesList.contains(uid);
                    updateHeartIcon(coverIndex, liked);
                    cntLikes[coverIndex] = likesList.size();
                    likes[coverIndex].setText(String.valueOf(cntLikes[coverIndex]));
                });
                loadedByLatest.add(subDocId); // 중복 리스트에 추가

                // 이미지 클릭 리스너
                covers[coverIndex].setOnClickListener(v -> {
                    Intent intent = new Intent(this, ReadBookActivity.class);
                    intent.putExtra("imgName", subDocId);
                    startActivity(intent);
                });
            });
        }
    }


    @NonNull
    // 파일 이름에 따라 '_'로 분할해 제목 알아냄
    private String extractTitle(@NonNull String fileName) {
        String[] parts = fileName.split("_");
        // 제목 부분에서 .png 제거 후 반환
        return parts[3].replace(".png", "");
    }

    // 파일 이름에 따라 '_'로 분할해 테마 알아냄
    private String extractThema(@NonNull String fileName) {
        String[] parts = fileName.split("_");
        // 제목 부분에서 .png 제거 후 반환
        return parts[1];
    }

    // 파일 이름에 따라 '_'로 분할해 main 문서 알아냄
    private String extractDocId(@NonNull String fileName) {
        String[] parts = fileName.split("_");
        // 제목 부분에서 .png 제거 후 반환
        return parts[0];
    }

    // 하트 상태를 알아냄
    private void toggleLike(int index) {
        // 현재 상태에 따라 인덱스 계산
        if (isBestMode) { // 좋아요 순
            if (index < 0 || index >= likesList.size()) return;

            // 좋아요 순에서 subDocId 가져옴
            String subDocId = likesList.get((currentPage - 1) * IMAGES_PER_PAGE + index).first;
            handleLikeToggle(subDocId, index);
        } else { // 최신순
            if (index < 0 || index >= timestampList.size()) return;

            // 최신순에서 subDocId 가져옴
            String subDocId = timestampList.get((currentPage - 1) * IMAGES_PER_PAGE + index).first;
            handleLikeToggle(subDocId, index);
        }
    }

    // 좋아요 증감
    private void handleLikeToggle(String subDocId, int index) {
        String mainDocId = extractDocId(subDocId);
        DocumentReference imageRef = db.collection("covers").document(mainDocId).collection("filename").document(subDocId);

        // 하위 문서에서 likes 필드 갖고 옴
        imageRef.get().addOnSuccessListener(documentSnapshot -> {
            Map<String, Object> data = documentSnapshot.getData();
            List<String> likesListInDoc = (List<String>) data.get("likes");
            if (likesListInDoc == null) likesListInDoc = new ArrayList<>();

            boolean isLiked = likesListInDoc.contains(uid);

            // 좋아요 상태에 따라
            if (isLiked) {
                // 이미 좋아요를 눌렀으면, 좋아요 감소
                likesListInDoc.remove(uid);
            } else {
                // 좋아요를 누르지 않았으면, 좋아요 증가
                likesListInDoc.add(uid);
            }

            imageRef.update("likes", likesListInDoc).addOnSuccessListener(aVoid -> {
                updateHeartIcon(index, !isLiked);
                updateLikeCount(index, isLiked ? -1 : 1);
            });
        });
    }

    // 좋아요 개수 업데이트
    private void updateLikeCount(int index, int change) {
        String subDocId;
        if (isBestMode) {
            // likesList 크기보다 크면 중단
            if (index < 0 || index >= likesList.size()) return;

            subDocId = likesList.get(index).first;
        } else {
            // timestampList 크기보다 크면 중단
            if (index < 0 || index >= timestampList.size()) return;

            subDocId = timestampList.get(index).first;
        }

        String mainDocId = extractDocId(subDocId);
        DocumentReference imageRef = db.collection("covers").document(mainDocId).collection("filename").document(subDocId);

        // 하위 문서에서 likes 필드 갖고 옴
        imageRef.get().addOnSuccessListener(documentSnapshot -> {
            Map<String, Object> data = documentSnapshot.getData();
            List<String> likesList = (List<String>) data.get("likes");
            if (likesList == null) likesList = new ArrayList<>();

            int likeCount = likesList.size();
            likes[index].setText(String.valueOf(likeCount));
            cntLikes[index] = likeCount; // 현재 상태에 맞게 좋아요 개수 변경
        });
    }

    // 하트 아이콘 업데이트
    private void updateHeartIcon(int index, boolean liked) {
        if (liked) { // 눌렀을 경우
            hearts[index].setImageResource(R.drawable.iv_mini_fullheart);
            isLiked[index] = true;
        } else {
            hearts[index].setImageResource(R.drawable.iv_mini_emptyheart);
            isLiked[index] = false;
        }
    }

    // 페이지 번호 업데이트
    private void updatePage(int selected) {
        if(isBestMode) totalPages = (likesList.size() + IMAGES_PER_PAGE - 1) / IMAGES_PER_PAGE;
        else totalPages = (timestampList.size() + IMAGES_PER_PAGE - 1) / IMAGES_PER_PAGE;

        if (selected > totalPages) {
            Toast.makeText(this, "마지막 페이지입니다", Toast.LENGTH_SHORT).show();
            return;
        }

        int count = pageText.size();
        // 현재 페이지 번호가 중앙에 오도록
        int startPage = selected - (count / 2);

        if (startPage + count - 1 > totalPages) {
            startPage = totalPages - count + 1;
        }
        startPage = Math.max(startPage, 1);

        for (int i = 0; i < count; i++) {
            TextView textView = pageText.get(i);
            int pageNumber = startPage + i;
            textView.setText(String.valueOf(pageNumber));

            if (pageNumber == selected) { // 선택된 페이지
                textView.setBackgroundResource(R.drawable.page_selected);
                textView.setTextColor(Color.WHITE);
            } else {
                textView.setBackgroundResource(0);
                textView.setTextColor(Color.BLACK);
            }
        }
        goToPage(selected);
    }

    // 현재 커버에 설정된 이미지 모두 제거하는 함수
    private void clearCurrentImages() {
        for (int i = 0; i < IMAGES_PER_PAGE; i++) {
            covers[i].setImageDrawable(null);
            titles[i].setText("");  // 제목 비우기
            hearts[i].setVisibility(View.INVISIBLE);  // 하트 아이콘 비우기
            likes[i].setText("");  // 좋아요 수 비우기
            isCoverSet[i] = false;  // 커버 이미지 설정 상태 초기화
        }
    }

    // 미니 페이지 변경
    public void goToPage(int page) {
        currentPage = page;
        // 설정된 이미지 및 제목들 모두 제거
        clearCurrentImages();

        // 현재 모드에 맞게 이미지를 로드
        if(isBestMode) loadBestImages();
        else loadLatestImages();
    }

    // 좋아요 Top 3 이미지 알아냄
    private void Top3Images() {
        // 상위 컬렉션의 모든 문서 가져오기
        db.collection("covers")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Task<QuerySnapshot>> subTasks = new ArrayList<>();

                        // 모든 문서의 좋아요 개수를 가져오기 위한 작업 리스트
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // 상위 문서의 ID
                            String docId = document.getId();
                            Task<QuerySnapshot> subTask = db.collection("covers")
                                    .document(docId)
                                    .collection("filename")
                                    .get();

                            subTasks.add(subTask);
                        }

                        // 모든 문서를 가져온 후 수행
                        Tasks.whenAllComplete(subTasks).addOnCompleteListener(subTaskResults -> {
                            List<Pair<String, Integer>> likesList = new ArrayList<>();

                            // 각 하위 문서의 like 리스트와 like 수를 가져옴
                            for (Task<QuerySnapshot> subTask : subTasks) {
                                if (subTask.isSuccessful()) {
                                    for (QueryDocumentSnapshot subDocument : subTask.getResult()) {
                                        String subDocId = subDocument.getId();
                                        List<String> likes = (List<String>) subDocument.get("likes");
                                        if (likes == null) likes = new ArrayList<>();

                                        int likeCount = likes.size();
                                        if (likeCount > 0) {
                                            likesList.add(new Pair<>(subDocId, likeCount));
                                        }
                                    }
                                }
                            }
                            // 모든 데이터가 추가된 후 정렬 수행
                            Collections.sort(likesList, (p1, p2) -> p2.second - p1.second);

                            // 좋아요 Top 3 로드
                            loadTop3Images(likesList ,1);
                        });
                    }
                });
    }

    // 베스트 Top 3 이미지 로드
    private void loadTop3Images(List<Pair<String, Integer>> likesList, int num) {
        int topCount = Math.min(3, likesList.size());  // Top 3
        List<String> topImageDocIds = new ArrayList<>();

        // 상위 3개의 subDocId 저장
        for (int i = 0; i < topCount; i++) {
            topImageDocIds.add(likesList.get(i).first);
        }

        // 가장 좋아요를 많이 받은 순으로 정렬
        if(num == 1 && topCount > 0) {
            String subDocId = likesList.get(0).first;
            loadImageAndDetails(subDocId, cover, title, writer);
        }
        else if(num == 2 && topCount > 1) {
            String subDocId = likesList.get(1).first;
            loadImageAndDetails(subDocId, cover, title, writer);
        }
        else if(num == 3 && topCount > 2) {
            String subDocId = likesList.get(2).first;
            loadImageAndDetails(subDocId, cover, title, writer);
        }
    }

    // 베스트 Top 3 사용자 정보 로드
    private void loadImageAndDetails(String subDocId, ImageView cover, TextView title, TextView writer) {
        String userId = extractDocId(subDocId); // 좋아요를 받은 uid
        String imageTitle = extractTitle(subDocId);
        StorageReference imageRef = storageRef.child(subDocId);

        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(this).asBitmap().load(uri).into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                    cover.setImageBitmap(resource); // 이미지 로드
                    title.setText("< " + imageTitle + " >"); // 제목 설정
                    explain.setText("");
                    fetchStoryAndGenerateText(subDocId); // 부가 설명 설정

                    // Palette로 이미지의 색상 추출
                    Palette.from(resource).generate(palette -> {
                        int dominantColor = palette.getDominantColor(Color.BLACK);
                        explain.setTextColor(dominantColor); // 추출된 색상을 TextView의 색상으로 설정
                        book.setBackgroundTintList(ColorStateList.valueOf(dominantColor)); // 배경 색상 설정
                    });

                    // 사용자의 이름과 작가 호칭 알아내기
                    db.collection("users").document(userId)
                            .get().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    String userName = task.getResult().getString("name");
                                    String userNickname = task.getResult().getString("nickname");
                                    writer.setText(userName + " " + userNickname + "의");
                                }
                            });
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {
                    // 필요 시 처리
                }
            });
        });
    }

    // 스토리 불러와서 텍스트를 string으로 변환
    private void fetchStoryAndGenerateText(@NonNull String subDocId) {
        // 파일명 : uid_thema_index.txt 로 바꿈
        String fileName = subDocId.split("_")[0] + "_" + extractThema(subDocId) + "_" + subDocId.split("_")[2] + ".txt";
        StorageReference storiesRef = storage.getReference().child("stories/" + fileName);

        final long MEGABYTE = 1024 * 1024;
        storiesRef.getBytes(MEGABYTE).addOnSuccessListener(bytes -> {
            String story = "";
            try {
                story = new String(bytes, "UTF-8");
                checkAndGenerateText(subDocId, story);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Firestore에서 홍보 문구의 생성 여부를 확인하고 필요 시 생성
    private void checkAndGenerateText(String subDocId, String story) {
        String mainDoc = extractDocId(subDocId);
        DocumentReference docRef = db.collection("covers").document(mainDoc).collection("filename").document(subDocId);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // 이미 홍보 문구가 생성되어 Firestore에 저장된 경우
                    String text = document.getString("explain");
                    Boolean isGemini = document.getBoolean("gemini");
                    if (isGemini != null && isGemini) {
                        // Firestore에서 저장된 홍보 문구를 가져와서 설정
                        explain.setText(text);
                    } else {
                        // Firestore에 저장된 텍스트가 없거나 gemini가 false인 경우
                        generatePromotionalText(docRef, story);
                    }
                } else {
                    // Firestore에 문서가 없는 경우, 텍스트 생성
                    generatePromotionalText(docRef, story);
                }
            }
        });
    }

    // 부가 설명 설정
    private void generatePromotionalText(DocumentReference docRef, String story) {
        String prompt = "아래의 동화의 스토리를 읽어보시고, 서점에서 이 동화를 판매한다고 생각하면서" +
                "이 동화에 대한 홍보 문구를 호들갑을 떨면서 20자 이하로 작성하세요. " +
                "이모지, '#', '*'을 사용하지 마세요. " +
                "ex) 207명이 열광했다! 동화공작소 개발자가 강력 추천하는 대한민국 No.1 화제의 동화\n: " + story;

        gemini.generateText(prompt, new Gemini.Callback() {
            @Override
            public void onSuccess(String text) {
                runOnUiThread(() -> {
                    // 성공적으로 텍스트를 받아온 경우, TextView에 설정
                    explain.setText(text);

                    Map<String, Object> data = new HashMap<>();
                    data.put("explain", text);
                    data.put("gemini", true);

                    docRef.update(data).addOnSuccessListener(aVoid -> {
                        Toast.makeText(WorldActivity.this, "저장했습니다.", Toast.LENGTH_SHORT).show();
                    });
                });
            }

            @Override
            public void onFailure(Throwable t) {
                // 오류 발생 시 처리
                explain.setText("홍보 문구를 생성하지 못했습니다.");
            }
        });
    }

    // 효과음
    public void sound() {
        isSoundOn = pref.getBoolean("on&off2", true);
        Intent intent = new Intent(this, SoundService.class);
        if (isSoundOn) startService(intent); // 효과음 on
        else stopService(intent);            // 효과음 off
    }
}