package com.example.holymoly;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class CustomImageView extends AppCompatImageView {

    private Paint paint;
    private Bitmap bitmap;
    private Path path;

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();
    private StorageReference characterRef = storageRef.child("characters/" + user.getUid() + ".png");

    public CustomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initPaint(); // Paint 객체 초기화
    }
    // stroke 지정
    private void initPaint() {
        paint = new Paint();
        paint.setColor(Color.WHITE); // 색상 설정
        paint.setStyle(Paint.Style.STROKE); // 색상 채우기 및 테두리
        paint.setStrokeWidth(6); // 두께 설정
        paint.setStrokeJoin(Paint.Join.ROUND); // 경로 연결 부분을 둥글게 설정
        paint.setAntiAlias(true); // 높은 해상도 설정
    }
    // 이미지 가져오기
    public void loadImage() {
        final long MEGABYTE = 1024 * 1024; // 1MB
        characterRef.getBytes(MEGABYTE).addOnSuccessListener(bytes -> {
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            path = createStroke(bitmap); // 비트맵 경로 생성
            invalidate(); // ImageView 화면에 다시 그림
        });
    }
    // 캔버스에 stroke와 캐릭터 그리기
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (bitmap != null) {
            // 비트맵을 캔버스에 그리기
            int left = (getWidth() - 450) / 2;
            int top = (getHeight() - 450) / 2 - 45;
            int right = left + 450;
            int bottom = top + 450;

            // 경로를 스케일링
            Matrix matrix = new Matrix();
            matrix.setScale(0.75f, 0.75f);
            path.transform(matrix, path); // 현재 path를 matrix에 맞게 변형

            // 테두리 그리기
            canvas.drawPath(path, paint);
            // 캐릭터 불러오기
            canvas.drawBitmap(bitmap, null, new RectF(left, top, right, bottom), null);
        }
    }
    private Path createStroke(Bitmap bitmap) {
        Path path = new Path();
        int width = 500;
        int height = getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (bitmap.getPixel(x, y) != Color.TRANSPARENT)
                    // 테두리 위치 수정 및 두께 추가 설정
                    path.addCircle(x + 7, y + 40, 6, Path.Direction.CW);
            }
        }
        return path;
    }
}
