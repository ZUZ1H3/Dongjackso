package com.example.holymoly;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class CustomView extends View {
    private Paint paint = new Paint(); // 그리기를 위한 기본 페인트 객체
    private Path path = new Path(); // 사용자가 터치한 경로를 저장할 Path 객체
    private Paint canvasPaint; // 캔버스에 그리기 위한 페인트 객체
    private Canvas drawCanvas; // 그림을 그릴 캔버스 객체
    private Bitmap canvasBitmap; // 그려진 내용을 저장할 비트맵 객체
    private float touchX, touchY; // 터치 좌표

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint.setAntiAlias(true); // 페인트의 품질 설정 (그림이 부드러워짐)
        paint.setStrokeWidth(10f); // 펜 크기 설정
        paint.setColor(Color.BLACK); // 기본 색상 (검정색)
        paint.setStyle(Paint.Style.STROKE); // 선 스타일
        paint.setStrokeJoin(Paint.Join.ROUND); // 선 끝 처리를 둥글게
        canvasPaint = new Paint(Paint.DITHER_FLAG); // 비트맵 드로잉 품질 향상
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint); // 기존 비트맵 그리기
        canvas.drawPath(path, paint); // 현재 경로 그리기
    }

    //사용자의 터치 동작에 따라 Path를 업데이트하고, 그 경로를 Canvas에 그림
    @Override
    public boolean onTouchEvent(android.view.MotionEvent event) {
        touchX = event.getX();
        touchY = event.getY();

        switch (event.getAction()) {
            case android.view.MotionEvent.ACTION_DOWN:
                path.moveTo(touchX, touchY);
                break;
            case android.view.MotionEvent.ACTION_MOVE:
                path.lineTo(touchX, touchY);
                break;
            case android.view.MotionEvent.ACTION_UP:
                drawCanvas.drawPath(path, paint); // 경로를 비트맵에 그리기
                path.reset();
                break;
            default:
                return false;
        }
        invalidate(); // 뷰 다시 그리기
        return true;
    }

    // 색상 설정 메서드
    public void setColor(String newColor) {
        paint.setColor(Color.parseColor(newColor)); // 페인트 색상 변경
        invalidate(); // 뷰 다시 그리기
    }
}
