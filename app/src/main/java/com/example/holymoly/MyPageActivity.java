package com.example.holymoly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MyPageActivity extends AppCompatActivity {
    private boolean isSoundOn;
    private SharedPreferences pref;

    private ImageButton homeBtn, myBtn, addBtn;
    private RadioButton bookBtn, loveBtn;
    private TextView name, nickname, countLove, countBook;
    private ImageView userImage;
    private ScrollView loves, books;

    /* DB */
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();
    private FirebaseFirestore db = FirebaseFirestore.getInstance(); // Firestore 인스턴스 추가

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_page);

        // SharedPreferences 초기화
        pref = getSharedPreferences("MyPreferences", MODE_PRIVATE);

        homeBtn = findViewById(R.id.ib_home);
        myBtn = findViewById(R.id.ib_my);
        addBtn = findViewById(R.id.iv_add_book);
        bookBtn = findViewById(R.id.rb_book);
        loveBtn = findViewById(R.id.rb_love);
        name = findViewById(R.id.st_name);
        nickname = findViewById(R.id.st_writer);
        countLove = findViewById(R.id.st_count_love);
        countBook = findViewById(R.id.st_count_book);
        userImage = findViewById(R.id.iv_userImage);
        books = findViewById(R.id.sv_books);
        loves = findViewById(R.id.sv_loves);

        // Firestore에서 사용자 이름과 호칭 불러오기
        loadUserData();

        // 사용자가 작성한 책의 개수와 총 좋아요 수 불러오기
        loadUserBookAndLoveCount();

        // 이미지 불러오기
        StorageReference imgRef = storageRef.child("characters/" + user.getUid() + ".png");
        imgRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(this)
                    .asBitmap()
                    .load(uri)
                    .override(310, 413) //이미지 크기 설정
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            userImage.setImageBitmap(resource); // ImageView에 설정
                        }
                        @Override
                        public void onLoadCleared(Drawable placeholder) {}
                    });
        });

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
            Intent intent = new Intent(MyPageActivity.this, WorldActivity.class);
            startActivity(intent);
        });

        addBtn.setOnClickListener(view -> {
            sound();
            Intent intent = new Intent(MyPageActivity.this, ChooseMyStory.class);
            startActivity(intent);
        });
    }

    // Firestore에서 사용자 정보 불러오기
    private void loadUserData() {
        String userId = user.getUid(); // 현재 사용자 ID 가져오기
        DocumentReference userDoc = db.collection("users").document(userId);

        userDoc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                // Firestore에서 사용자 데이터 가져오기
                String userName = task.getResult().getString("name"); // Firestore의 "name" 필드
                String userNickname = task.getResult().getString("nickname"); // Firestore의 "nickname" 필드

                // TextView에 설정
                name.setText(userName); // 이름 TextView에 설정
                nickname.setText(userNickname); // 호칭 TextView에 설정
            }
        });
    }

    // 사용자가 작성한 책의 개수와 총 좋아요 개수를 불러오는 메소드 (인데 안 됨.. 붙잡고 노력해봤는데도... 난 여기까진가벼;; 희야 부탁해...)
    private void loadUserBookAndLoveCount() {
        String uid = user.getUid(); // 현재 로그인한 사용자의 UID

        // 사용자가 작성한 책의 개수와 총 좋아요 수 초기화
        final AtomicInteger totalBookCount = new AtomicInteger(0);  // 초기값 0

        // Firestore에서 사용자가 작성한 모든 책의 문서 가져오기
        db.collection("covers")
                .whereEqualTo("uid", uid)  // 사용자가 작성한 문서만 가져오기
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Task<QuerySnapshot>> subTasks = new ArrayList<>();

                        if (task.getResult() == null || task.getResult().isEmpty()) {
                            // 문서가 없을 경우 처리
                            countBook.setText("0");
                            countLove.setText("0");
                            return;
                        }

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            totalBookCount.incrementAndGet();  // 책 개수 증가
                            String docId = document.getId();

                            // 하위 컬렉션 filename에서 각 책의 좋아요 수를 가져오기
                            Task<QuerySnapshot> subTask = db.collection("covers")
                                    .document(docId).collection("filename") // 하위 컬렉션 접근
                                    .get();
                            subTasks.add(subTask);
                        }

                        // 모든 하위 문서의 좋아요 수를 가져온 후 총합 계산
                        Tasks.whenAllComplete(subTasks).addOnCompleteListener(subTaskResults -> {
                            int totalLikes = 0;  // 총 좋아요 수를 저장할 변수

                            for (Task<QuerySnapshot> subTask : subTasks) {
                                if (subTask.isSuccessful()) {
                                    for (QueryDocumentSnapshot subDocument : subTask.getResult()) {
                                        List<String> likes = (List<String>) subDocument.get("likes");
                                        if (likes != null) {
                                            totalLikes += likes.size();  // 좋아요 수 합산
                                        }
                                    }
                                }
                            }
                            // Debug용 로그 추가
                            Log.d("UserInfo", "Total books: " + totalBookCount.get() + ", Total likes: " + totalLikes);

                            // countLove와 countBook TextView에 결과 설정
                            countLove.setText(String.valueOf(totalLikes));  // 총 좋아요 수 표시
                            countBook.setText(String.valueOf(totalBookCount.get()));  // 총 책 개수 표시
                        });
                    } else {
                        // 오류 처리
                        Log.e("FirestoreError", "Error getting documents: ", task.getException());
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
