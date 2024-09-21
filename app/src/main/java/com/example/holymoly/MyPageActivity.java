package com.example.holymoly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MyPageActivity extends AppCompatActivity {
    private ImageButton homeBtn;
    private RadioButton bookBtn, loveBtn;
    private TextView name, nickname, countLove, countBook;
    private ImageView userImage, add;
    private ScrollView loves, books;
    private GridLayout gl_book, gl_like;

    /* DB */
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();
    private String uid = user.getUid();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();
    private StorageReference coversRef = storageRef.child("covers");
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference userDoc = db.collection("covers").document(uid);

    /* 효과음 */
    private SharedPreferences pref;
    private boolean isSoundOn;

    private CoverAdapter adapter;
    private ArrayList<String> imagesList = new ArrayList<>();
    private ArrayList<String> urlList = new ArrayList<>();
    private ArrayList<String> likesList = new ArrayList<>();
    private Set<String> uploadedImageUrls = new HashSet<>(); // 다이얼로그 중복 확인용

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);
        pref = getSharedPreferences("music", MODE_PRIVATE); // 효과음 초기화

        homeBtn = findViewById(R.id.ib_home);
        add = findViewById(R.id.add);
        bookBtn = findViewById(R.id.rb_book);
        loveBtn = findViewById(R.id.rb_love);
        name = findViewById(R.id.st_name);
        nickname = findViewById(R.id.st_writer);
        countLove = findViewById(R.id.st_count_love);
        countBook = findViewById(R.id.st_count_book);
        userImage = findViewById(R.id.iv_userImage);
        books = findViewById(R.id.sv_books);
        loves = findViewById(R.id.sv_loves);
        gl_book = findViewById(R.id.gl_books);
        gl_like = findViewById(R.id.gl_loves);

        // 사용자 정보 로드
        loadUserData();
        // book에 업로드한 이미지 로드
        loadUploadedImages();
        // love에 사용자가 좋아요한 이미지 로드
        loadLikeImages();

        // 기본으로 bookBtn을 선택하고 books를 보이게 설정
        bookBtn.setChecked(true);
        books.setVisibility(View.VISIBLE); // 책 스크롤 뷰 보이기
        loves.setVisibility(View.INVISIBLE); // 좋아요 스크롤 뷰 숨기기

        // bookBtn과 loveBtn 선택 리스너 추가
        bookBtn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                books.setVisibility(View.VISIBLE); // 책 스크롤 뷰 보이기
                loves.setVisibility(View.INVISIBLE); // 좋아요 스크롤 뷰 숨기기
            }
        });

        loveBtn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                loves.setVisibility(View.VISIBLE); // 좋아요 스크롤 뷰 보이기
                books.setVisibility(View.INVISIBLE); // 책 스크롤 뷰 숨기기
            }
        });

        // 홈버튼 클릭 리스너
        homeBtn.setOnClickListener(view -> {
            sound();
            finish();
            Intent intent = new Intent(MyPageActivity.this, WorldActivity.class);
            startActivity(intent);
        });

        add.setOnClickListener(view -> {
            sound();
            showDialog();
        });
    }

    // 사용자 정보 불러옴
    private void loadUserData() {
        // 사용자 이미지 불러오기
        StorageReference imgRef = storageRef.child("characters/" + user.getUid() + ".png");
        imgRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(this)
                    .asBitmap().load(uri).into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            Bitmap bitmap = createShadow(resource);
                            userImage.setImageBitmap(bitmap);
                        }

                        @Override
                        public void onLoadCleared(Drawable placeholder) {}
                    });
        });

        // 사용자 이름, 작가 호칭 불러오기
        DocumentReference user = db.collection("users").document(uid);
        user.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String userName = task.getResult().getString("name");
                String userNickname = task.getResult().getString("nickname");

                name.setText(userName); // 이름 TextView에 설정
                nickname.setText(userNickname); // 호칭 TextView에 설정
            }
        });
    }

    // 그림자 생성
    private Bitmap createShadow(Bitmap bitmap) {
        // 기존 이미지 크기와 동일한 새 Bitmap 생성
        Bitmap shadowBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(shadowBitmap);

        // 그림자 효과를 위한 Paint 설정
        Paint shadowPaint = new Paint();
        shadowPaint.setAntiAlias(true);    // 경계선이 부드럽게
        shadowPaint.setColor(Color.BLACK); // 그림자 색상
        shadowPaint.setAlpha(75);         // 투명도 설정
        shadowPaint.setMaskFilter(new BlurMaskFilter(5f, BlurMaskFilter.Blur.NORMAL));

        // 그림자 그리기
        canvas.drawBitmap(bitmap.extractAlpha(), 0, 5, shadowPaint);
        // 원본 이미지 그리기
        canvas.drawBitmap(bitmap, 0, 0, null);

        return shadowBitmap;
    }

    // 이미지 선택 다이얼로그
    private void showDialog() {
        // 다이얼로그가 열릴 때마다 리스트 초기화
        imagesList.clear();
        urlList.clear();
        uploadedImageUrls.clear();

        // 다이얼로그 builder 설정
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("업로드할 이미지를 선택하세요");

        // 다이얼로그 레이아웃 보여주기
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_image, null);
        builder.setView(dialogView);

        // RecyclerView 설정
        RecyclerView recyclerView = dialogView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        // 어댑터 설정
        adapter = new CoverAdapter(this, urlList, imageUrl -> {
            if(uploadedImageUrls.contains(imageUrl)) {
                Toast.makeText(this, "업로드된 이미지입니다", Toast.LENGTH_SHORT).show();
            }
            else {
                uploadedImageUrls.add(imageUrl);
                uploadImages(imageUrl);
                updateFirestore(imageUrl);
            }
        });
        recyclerView.setAdapter(adapter);

        coversRef.listAll().addOnSuccessListener(listResult -> {
            for(StorageReference item : listResult.getItems()) {
               if(item.getName().startsWith(uid)) {
                   userDoc.collection("filename").document(item.getName()).get()
                           .addOnSuccessListener(task -> {
                               // upload 필드가 없거나 false일 때만 리스트에 추가
                               if (!task.exists() || !task.contains("upload") || !task.getBoolean("upload")) {
                                   item.getDownloadUrl().addOnSuccessListener(uri -> {
                                       imagesList.add(item.getName());
                                       urlList.add(uri.toString());

                                       adapter.notifyDataSetChanged(); // 데이터 변경
                                   });
                               }
                           });
               }
           }
            builder.setPositiveButton("완료", (dialog, which) -> dialog.dismiss());
            // 다이얼로그 생성 및 보여줌
            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    // 이미지 업로드
    private void uploadImages(String imageUrl) {
        ImageView iv = new ImageView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(192), dpToPx(240));
        params.setMargins(0, 0, 60,30);
        iv.setLayoutParams(params);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);

        Glide.with(this).load(imageUrl).into(iv);
        gl_book.addView(iv);
    }

    // dp 값을 px로 변환
    private int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    // 이미지에 해당하는 firestore 업로드 상태 변경
    private void updateFirestore(String imageUrl) {
        StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
        String fileName = imageRef.getName(); // 이미지 파일 이름 추출

        DocumentReference docRef = userDoc.collection("filename").document(fileName);
        docRef.update("upload", true).addOnSuccessListener(aVoid -> { });
    }

    // 업로드된 이미지 로드
    private void loadUploadedImages() {
        List<Pair<String, Date>> timestampList = new ArrayList<>(); // 문서 ID와 timestamp를 저장할 리스트
        int[] bookCnt = { 0 };  // 이미지 개수
        int[] loveCnt = { 0 };  // 좋아요 배열 개수

        userDoc.collection("filename")
                .whereEqualTo("upload", true)  // upload가 true인 문서만 가져옴
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String docId = document.getId(); // 문서 ID를 가져옴

                        // timestamp 필드를 가져옴
                        Timestamp timestamp = document.getTimestamp("timestamp");
                        if (timestamp != null) {
                            // timestamp를 Date로 변환하여 리스트에 추가
                            Date date = timestamp.toDate();
                            timestampList.add(new Pair<>(docId, date));
                        }
                        // likes 배열의 길이를 가져와서 카운트
                        List<String> likesList = (List<String>) document.get("likes");
                        if (likesList != null) {
                            loveCnt[0] += likesList.size(); // 좋아요 배열 개수 카운트
                        }
                        bookCnt[0]++;
                    }

                    // 최신순으로 정렬
                    Collections.sort(timestampList, (p1, p2) -> p2.second.compareTo(p1.second));

                    // 정렬된 데이터를 이용해 UI 업데이트
                    for (Pair<String, Date> entry : timestampList) {
                        String docId = entry.first;

                        // 문서와 일치하는 이미지 가져오기
                        coversRef.child(docId).getDownloadUrl().addOnSuccessListener(uri -> {
                            // ImageView 생성 및 레이아웃 설정
                            ImageView iv = new ImageView(this);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(192), dpToPx(240));
                            params.setMargins(0, 0, 60,30);
                            iv.setLayoutParams(params);
                            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);

                            // Glide를 이용해 이미지 로드
                            Glide.with(this).load(uri).into(iv);

                            iv.setOnTouchListener(new View.OnTouchListener() {
                                private Handler handler = new Handler();
                                private Runnable longPressRunnable;
                                private boolean isPressed = false;

                                @Override
                                public boolean onTouch(View v, MotionEvent event) {
                                    switch (event.getAction()) {
                                        case MotionEvent.ACTION_DOWN:
                                            isPressed = false;
                                            longPressRunnable = new Runnable() {
                                                @Override
                                                public void run() {
                                                    if(!isPressed) {
                                                        isPressed = true;
                                                        showDialog(docId); // 다이얼로그
                                                    }
                                                }
                                            };
                                            handler.postDelayed(longPressRunnable, 2000);
                                            return true;

                                        case MotionEvent.ACTION_UP: // 롱 클릭 취소
                                            handler.removeCallbacks(longPressRunnable);
                                        case MotionEvent.ACTION_CANCEL:
                                            handler.removeCallbacks(longPressRunnable); // 롱 클릭 취소
                                            return true;
                                        default:
                                            return false;
                                    }
                                }
                            });
                            gl_book.addView(iv);
                        });
                    }
                    countBook.setText(String.valueOf(bookCnt[0]));
                    countLove.setText(String.valueOf(loveCnt[0]));
                });
    }

    private void showDialog(String docId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("업로드 취소")
                .setMessage(extractTitle(docId) + "(을)를 삭제합니다.")
                .setPositiveButton("네", (dialog, which) -> {
                    // 이미지 삭제 후 재실행
                    userDoc.collection("filename").document(docId).update("upload", false)
                            .addOnSuccessListener(aVoid -> {
                               Intent intent = getIntent();
                               finish();
                               startActivity(intent);
                            });
                })
                .setNegativeButton("아니요", (dialog, which) -> {
                    dialog.dismiss();
                });
        builder.create().show();
    }

    @NonNull
    // 파일 이름에 따라 '_'로 분할해 제목 알아냄
    private String extractTitle(@NonNull String fileName) {
        String[] parts = fileName.split("_");
        // 제목 부분에서 .png 제거 후 반환
        return parts[3].replace(".png", "");
    }
    // 좋아요 누른 이미지 로드
    private void loadLikeImages() {
        likesList.clear();

        db.collection("covers").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Task<QuerySnapshot>> subTasks = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String docId = document.getId(); // 상위 문서의 ID

                    // 하위 컬렉션에서 likes 필드에 현재 사용자가 포함된 문서만 갖고 옴
                    Task<QuerySnapshot> subTask = db.collection("covers").document(docId)
                            .collection("filename").whereArrayContains("likes", uid).get();
                    subTasks.add(subTask);
                }

                // 모든 문서를 가져온 후 수행
                Tasks.whenAllComplete(subTasks).addOnCompleteListener(subTaskResults -> {
                            for (Task<QuerySnapshot> subTask : subTasks) {
                                if (subTask.isSuccessful()) {
                                    for (QueryDocumentSnapshot subDocument : subTask.getResult()) {
                                        String subDocId = subDocument.getId(); // 해당하는 하위 문서 ID
                                        likesList.add(subDocId);

                                        // 문서와 일치하는 이미지 가져오기
                                        coversRef.child(subDocId).getDownloadUrl().addOnSuccessListener(uri -> {
                                            ImageView iv = new ImageView(this);
                                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(192), dpToPx(240));
                                            params.setMargins(0, 0, 60, 30);
                                            iv.setLayoutParams(params);
                                            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);

                                            Glide.with(this).load(uri).into(iv);
                                            gl_like.addView(iv);

                                            iv.setOnClickListener(v -> {
                                                Intent intent = new Intent(this, ReadBookActivity.class);
                                                intent.putExtra("imgName", subDocId);
                                                startActivity(intent);
                                            });
                                        });
                                    }
                                }
                            }
                });
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