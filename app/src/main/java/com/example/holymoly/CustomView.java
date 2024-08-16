package com.example.holymoly;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import java.util.Stack;

public class CustomView extends View {
    private Paint paint = new Paint();
    private Path path = new Path();
    private Paint canvasPaint;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;
    private float touchX, touchY;
    private float penWidth = 20f; // 기본 펜 굵기
    private Stack<Path> paths = new Stack<>(); // 모든 경로를 저장하는 스택
    private Stack<Path> undonePaths = new Stack<>(); // 지운 경로를 저장하는 스택

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint.setAntiAlias(true);
        paint.setStrokeWidth(penWidth);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(path, paint);
    }

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
                drawCanvas.drawPath(path, paint);
                paths.push(new Path(path)); // 현재 경로를 저장
                path.reset();
                break;
            default:
                return false;
        }
        invalidate();
        return true;
    }

    public void setColor(String newColor) {
        paint.setColor(Color.parseColor(newColor));
        invalidate();
    }

    public void setPenWidth(float width) {
        penWidth = width;
        paint.setStrokeWidth(penWidth);
        invalidate();
    }

    // Undo 기능 추가
    public void undo() {
        if (!paths.isEmpty()) {
            undonePaths.push(paths.pop()); // 최근 경로를 undonePaths에 저장
            redraw();
        }
    }

    // Redraw all paths
    private void redraw() {
        drawCanvas.drawColor(Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR); // Clear the canvas
        for (Path p : paths) {
            drawCanvas.drawPath(p, paint); // 재드로잉
        }
        invalidate();
    }
}
