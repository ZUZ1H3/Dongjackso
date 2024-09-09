package com.example.holymoly;

import static java.lang.Math.abs;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class StartPuzzleActivity extends AppCompatActivity implements View.OnClickListener {
    ArrayList<PuzzlePiece> pieces;
    private String image;
    private int rows, cols;

    private RelativeLayout layout;
    private ImageView imageView;
    private TextView count;
    private ImageButton stop, hint;
    private long backPressedTime = 0;
    private Handler handler = new Handler();
    private int countdownTime = 5; // 카운트다운 초

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String uid = user.getUid();  // 사용자의 uid

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_puzzle);

        layout = findViewById(R.id.layout);
        imageView = findViewById(R.id.imageView);
        count = findViewById(R.id.count);
        stop = findViewById(R.id.ib_stop);
        hint = findViewById(R.id.ib_hint);

        stop.setOnClickListener(this);
        hint.setOnClickListener(this);

        // Intent로 전달받은 데이터 추출
        Intent intent = getIntent();
        image = intent.getStringExtra("selectedImage");
        rows = intent.getIntExtra("rows", 3); // 기본값 3
        cols = intent.getIntExtra("cols", 3); // 기본값 3

        // 이미지 로드 및 퍼즐 조각 분할
        loadImage(image, imageView);
    }

    @Override
    public void onClick(View v) {
        sound();
        if (v.getId() == R.id.ib_stop) {
            if (System.currentTimeMillis() - backPressedTime >= 2000) {
                backPressedTime = System.currentTimeMillis();
                Toast.makeText(this, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        else if (v.getId() == R.id.ib_hint) {
            // hint 클릭시 이미지 보임
            countdown();
            imageView.setVisibility(View.VISIBLE);
            // 5초 후에 이미지 안 보임
            new Handler().postDelayed(() -> imageView.setVisibility(View.INVISIBLE), 5000);
        }
    }

    // Glide를 사용해 이미지를 로드한 후 Bitmap으로 변환
    private void loadImage(String imageUrl, ImageView imageView) {
        Glide.with(this)
                .asBitmap()
                .load(imageUrl)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        // 이미지를 로드한 후, 올바른 방향으로 회전시킴
                        imageView.setImageBitmap(resource);
                        imageView.setVisibility(View.INVISIBLE);

                        // 이미지 불러온 후, 퍼즐 조각으로 분리
                        imageView.post(() -> {
                            pieces = splitImage();
                            PuzzleTouchListener touchListener = new PuzzleTouchListener(StartPuzzleActivity.this);
                            Collections.shuffle(pieces); // 순서 섞기
                            int marginAdjustment = 40;   // 하단에 줄일 마진 크기

                            layout.removeAllViews(); // 이전 뷰 제거
                            for (PuzzlePiece piece : pieces) {
                                piece.setOnTouchListener(touchListener);
                                layout.addView(piece);

                                // 이미지 아래에 퍼즐 조각 랜덤 배치
                                RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) piece.getLayoutParams();
                                lParams.leftMargin = marginAdjustment + new Random().nextInt(layout.getWidth() - piece.pieceWidth - 2 * marginAdjustment);

                                lParams.topMargin = layout.getHeight() - piece.pieceHeight;

                                piece.setLayoutParams(lParams);
                            }
                        });
                    }

                    @Override
                    public void onLoadCleared(Drawable placeholder) { }

                    @Override
                    public void onLoadFailed(Drawable errorDrawable) {
                        Toast.makeText(StartPuzzleActivity.this, "이미지 로드 실패", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 이미지를 직소 퍼즐 조각으로 나누는 메서드
    private ArrayList<PuzzlePiece> splitImage() {
        ImageView imageView = findViewById(R.id.imageView);

        ArrayList<PuzzlePiece> pieces = new ArrayList<>(rows * cols);
        // 이미지의 비트맵 가져오기
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();

        int[] dimensions = getBitmapPositionInsideImageView(imageView);
        int scaledBitmapLeft = dimensions[0];
        int scaledBitmapTop = dimensions[1];
        int scaledBitmapWidth = dimensions[2];
        int scaledBitmapHeight = dimensions[3];

        int croppedImageWidth = scaledBitmapWidth - 2 * abs(scaledBitmapLeft);
        int croppedImageHeight = scaledBitmapHeight - 2 * abs(scaledBitmapTop);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledBitmapWidth, scaledBitmapHeight, true);
        Bitmap croppedBitmap = Bitmap.createBitmap(scaledBitmap, abs(scaledBitmapLeft), abs(scaledBitmapTop), croppedImageWidth, croppedImageHeight);

        // 퍼즐 조각의 가로와 세로 크기 계산
        int pieceWidth = croppedImageWidth / cols;
        int pieceHeight = croppedImageHeight / rows;

        // 각 비트맵 조각을 생성하고 결과 배열에 추가
        int yCoord = 0;
        for (int row = 0; row < rows; row++) {
            int xCoord = 0;
            for (int col = 0; col < cols; col++) {
                // 각 조각의 오프셋 계산
                int offsetX = 0;
                int offsetY = 0;
                if (col > 0) {
                    offsetX = pieceWidth / 3;
                }
                if (row > 0) {
                    offsetY = pieceHeight / 3;
                }

                // 오프셋을 각 조각에 적용
                Bitmap pieceBitmap = Bitmap.createBitmap(croppedBitmap, xCoord - offsetX, yCoord - offsetY, pieceWidth + offsetX, pieceHeight + offsetY);
                PuzzlePiece piece = new PuzzlePiece(getApplicationContext());
                piece.setImageBitmap(pieceBitmap);
                piece.xCoord = xCoord - offsetX + imageView.getLeft();
                piece.yCoord = yCoord - offsetY + imageView.getTop();
                piece.pieceWidth = pieceWidth + offsetX;
                piece.pieceHeight = pieceHeight + offsetY;

                // 최종 퍼즐 조각 이미지
                Bitmap puzzlePiece = Bitmap.createBitmap(pieceWidth + offsetX, pieceHeight + offsetY, Bitmap.Config.ARGB_8888);

                // 경로 그리기
                int bumpSize = pieceHeight / 4;
                Canvas canvas = new Canvas(puzzlePiece);
                Path path = new Path();
                path.moveTo(offsetX, offsetY);
                if (row == 0) {
                    // 상단 면 조각
                    path.lineTo(pieceBitmap.getWidth(), offsetY);
                } else {
                    // 상단 돌기
                    path.lineTo(offsetX + (pieceBitmap.getWidth() - offsetX) / 3, offsetY);
                    path.cubicTo(offsetX + (pieceBitmap.getWidth() - offsetX) / 6, offsetY - bumpSize, offsetX + (pieceBitmap.getWidth() - offsetX) / 6 * 5, offsetY - bumpSize, offsetX + (pieceBitmap.getWidth() - offsetX) / 3 * 2, offsetY);
                    path.lineTo(pieceBitmap.getWidth(), offsetY);
                }

                if (col == cols - 1) {
                    // 오른쪽 면 조각
                    path.lineTo(pieceBitmap.getWidth(), pieceBitmap.getHeight());
                } else {
                    // 오른쪽 돌기
                    path.lineTo(pieceBitmap.getWidth(), offsetY + (pieceBitmap.getHeight() - offsetY) / 3);
                    path.cubicTo(pieceBitmap.getWidth() - bumpSize, offsetY + (pieceBitmap.getHeight() - offsetY) / 6, pieceBitmap.getWidth() - bumpSize, offsetY + (pieceBitmap.getHeight() - offsetY) / 6 * 5, pieceBitmap.getWidth(), offsetY + (pieceBitmap.getHeight() - offsetY) / 3 * 2);
                    path.lineTo(pieceBitmap.getWidth(), pieceBitmap.getHeight());
                }

                if (row == rows - 1) {
                    // 하단 면 조각
                    path.lineTo(offsetX, pieceBitmap.getHeight());
                } else {
                    // 하단 돌기
                    path.lineTo(offsetX + (pieceBitmap.getWidth() - offsetX) / 3 * 2, pieceBitmap.getHeight());
                    path.cubicTo(offsetX + (pieceBitmap.getWidth() - offsetX) / 6 * 5, pieceBitmap.getHeight() - bumpSize, offsetX + (pieceBitmap.getWidth() - offsetX) / 6, pieceBitmap.getHeight() - bumpSize, offsetX + (pieceBitmap.getWidth() - offsetX) / 3, pieceBitmap.getHeight());
                    path.lineTo(offsetX, pieceBitmap.getHeight());
                }

                if (col == 0) {
                    // 왼쪽 면 조각
                    path.close();
                } else {
                    // 왼쪽 돌기
                    path.lineTo(offsetX, offsetY + (pieceBitmap.getHeight() - offsetY) / 3 * 2);
                    path.cubicTo(offsetX - bumpSize, offsetY + (pieceBitmap.getHeight() - offsetY) / 6 * 5, offsetX - bumpSize, offsetY + (pieceBitmap.getHeight() - offsetY) / 6, offsetX, offsetY + (pieceBitmap.getHeight() - offsetY) / 3);
                    path.close();
                }

                // 조각 마스킹
                Paint paint = new Paint();
                paint.setColor(0XFF000000);
                paint.setStyle(Paint.Style.FILL);

                canvas.drawPath(path, paint);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                canvas.drawBitmap(pieceBitmap, 0, 0, paint);

                // 흰색 테두리 그리기
                Paint border = new Paint();
                border.setColor(0X80FFFFFF);
                border.setStyle(Paint.Style.STROKE);
                border.setStrokeWidth(8.0f);
                canvas.drawPath(path, border);

                // 검은색 테두리 그리기
                border = new Paint();
                border.setColor(0X80000000);
                border.setStyle(Paint.Style.STROKE);
                border.setStrokeWidth(3.0f);
                canvas.drawPath(path, border);

                // 결과 비트맵을 조각에 설정
                piece.setImageBitmap(puzzlePiece);

                pieces.add(piece);
                xCoord += pieceWidth;
            }
            yCoord += pieceHeight;
        }

        return pieces;
    }

    // 이미지 뷰 안에서 이미지의 위치와 크기 계산
    private int[] getBitmapPositionInsideImageView(ImageView imageView) {
        int[] ret = new int[4];

        if (imageView == null || imageView.getDrawable() == null) return ret;

        // 이미지의 스케일 값 추출
        float[] f = new float[9];
        imageView.getImageMatrix().getValues(f);
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];

        // 원본 이미지의 크기 계산
        final Drawable d = imageView.getDrawable();
        final int origW = d.getIntrinsicWidth();
        final int origH = d.getIntrinsicHeight();

        // 실제 이미지 크기 계산
        final int actW = Math.round(origW * scaleX);
        final int actH = Math.round(origH * scaleY);

        // 이미지의 위치와 크기 설정
        ret[2] = actW;
        ret[3] = actH;

        // 이미지 뷰의 크기 가져오기
        int imgViewW = imageView.getWidth();
        int imgViewH = imageView.getHeight();

        // 이미지의 왼쪽과 위쪽 위치 계산
        int top = (imgViewH - actH) / 2;
        int left = (imgViewW - actW) / 2;

        ret[0] = left;
        ret[1] = top;

        return ret;
    }

    // 게임 오버 확인
    public void checkGameOver() {
        if (isGameOver()) {
            // Firestore에 저장할 데이터
            Map<String, Object> data = new HashMap<>();
            data.put("imageUrl", image);  // 퍼즐 이미지 URL

            // 사용자 문서의 'completed' 컬렉션에서 동일한 Url이 있는지 확인
            db.collection("puzzles").document(uid).collection("completed")
                    .whereEqualTo("imageUrl", image) // imageUrl이 같은 문서 검색
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();

                            if (!querySnapshot.isEmpty()) {
                                // 동일한 Url이 존재하면 해당 문서의 rows 배열에 새로운 값을 추가
                                for (QueryDocumentSnapshot document : querySnapshot) {
                                    db.collection("puzzles").document(uid)
                                            .collection("completed")
                                            .document(document.getId())
                                            // arrayUnion을 이용해 중복 없이 값 추가
                                            .update("rows", FieldValue.arrayUnion(rows)) // rows 값 배열에 추가
                                            .addOnSuccessListener(aVoid -> { });
                                }
                            } else {
                                // 동일한 Url이 없으면 새로 추가하고 rows 필드를 배열로 설정
                                data.put("rows", Collections.singletonList(rows)); // 배열로 rows 값 설정
                                db.collection("puzzles").document(uid)
                                        .collection("completed")
                                        .add(data)
                                        .addOnSuccessListener(documentReference -> {
                                            Toast.makeText(this, "퍼즐 데이터가 저장되었습니다.", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        }
                    });

            // 퍼즐 완성 알림 다이얼로그 표시
            AlertDialog completion = new AlertDialog.Builder(this)
                    .setTitle("완성했습니다")
                    .setMessage("퍼즐을 완성했습니다.\n 다른 퍼즐을 맞추시겠습니까?")
                    .setPositiveButton("네", (dialog, which) -> {
                        Intent intent = new Intent(this, PuzzleActivity.class);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("아니요", (dialog, which) -> {
                        finish();
                    })
                    .create(); // AlertDialog 객체 생성
            completion.show(); // 다이얼로그 표시
        }
    }

    // 모든 퍼즐 조각이 제자리에 놓였는지 확인
    private boolean isGameOver() {
        for (PuzzlePiece piece : pieces) {
            if (piece.canMove) {
                return false;
            }
        }
        return true;
    }

    // 카운트다운
    private void countdown() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (countdownTime > 0) {
                    count.setVisibility(View.VISIBLE);
                    count.setText(String.valueOf(countdownTime)); // 남은 시간 텍스트에 표시
                    countdownTime--;
                    handler.postDelayed(this, 1000); // 1초마다
                }
                else count.setVisibility(View.INVISIBLE);
            }
        }, 0);
    }

    // 효과음
    public void sound() {
        Intent intent = new Intent(this, SoundService.class);
        startService(intent);
    }
}
