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
    private Paint paint;
    private Path path;
    private Paint canvasPaint;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;
    private float touchX, touchY;
    private float penWidth = 15f; // 기본 펜 굵기
    private int currentColor = Color.BLACK; // 기본 색상
    private Stack<DrawCommand> paths = new Stack<>(); // 모든 경로를 저장하는 스택
    private Stack<DrawCommand> undonePaths = new Stack<>(); // 지운 경로를 저장하는 스택
    private String bookTitle = "";

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(penWidth);
        paint.setColor(currentColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        canvasPaint = new Paint(Paint.DITHER_FLAG);
        path = new Path();
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
                paths.push(new DrawCommand(new Path(path), currentColor, penWidth)); // 현재 경로를 저장
                path.reset();
                break;
            default:
                return false;
        }
        invalidate();
        return true;
    }

    public void setColor(String newColor) {
        currentColor = Color.parseColor(newColor);
        paint.setColor(currentColor);
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
    // 그림 전체 지우기
    public void clearCanvas() {
        paths.clear();
        undonePaths.clear();
        drawCanvas.drawColor(Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR); // 캔버스를 지움
        invalidate();
    }
    
    // Redraw all paths
    private void redraw() {
        drawCanvas.drawColor(Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR); // Clear the canvas
        for (DrawCommand command : paths) {
            paint.setColor(command.color);
            paint.setStrokeWidth(command.width);
            drawCanvas.drawPath(command.path, paint); // 재드로잉
        }
        invalidate();
    }

    private class DrawCommand {
        Path path;
        int color;
        float width;

        DrawCommand(Path path, int color, float width) {
            this.path = path;
            this.color = color;
            this.width = width;
        }
    }
}
