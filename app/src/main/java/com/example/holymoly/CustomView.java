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
                paths.push(new DrawCommand(new Path(path), currentColor, penWidth, false)); // 현재 경로를 저장
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
            undonePaths.push(paths.pop()); // Recent command to undonePaths
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

    private void redraw() {
        drawCanvas.drawColor(Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR); // Clear the canvas
        for (DrawCommand command : paths) {
            if (command.isFillCommand) {
                drawCanvas.drawColor(command.color);
            } else {
                paint.setColor(command.color);
                paint.setStrokeWidth(command.width);
                drawCanvas.drawPath(command.path, paint); // Draw path
            }
        }
        invalidate();
    }

    private class DrawCommand {
        Path path;
        int color;
        float width;
        boolean isFillCommand;

        DrawCommand(Path path, int color, float width, boolean isFillCommand) {
            this.path = new Path(path);
            this.color = color;
            this.width = width;
            this.isFillCommand = isFillCommand;
        }
    }


    public void fillCanvas(String color) {
        int fillColor = Color.parseColor(color);
        drawCanvas.drawColor(fillColor);
        paths.push(new DrawCommand(new Path(), fillColor, 0, true)); // 채운 색상만 저장
        invalidate();
    }


    public void drawBitmapOnCanvas(Bitmap bitmap) {
        if (bitmap != null) {
            // 캔버스의 너비와 높이를 가져옵니다.
            int canvasWidth = drawCanvas.getWidth();
            int canvasHeight = drawCanvas.getHeight();

            // 비트맵의 너비와 높이를 가져옵니다.
            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight();

            // 캔버스에 맞도록 비트맵의 크기 비율을 계산합니다.
            float scaleWidth = ((float) canvasWidth) / bitmapWidth;
            float scaleHeight = ((float) canvasHeight) / bitmapHeight;

            // 크기 조정을 위한 비율을 계산합니다.
            float scale = Math.min(scaleWidth, scaleHeight);

            // 비율에 따라 새로운 크기를 계산합니다.
            int newWidth = (int) (bitmapWidth * scale);
            int newHeight = (int) (bitmapHeight * scale);

            // 새 크기로 비트맵을 조정합니다.
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

            // 조정된 비트맵을 캔버스에 그립니다.
            // 캔버스의 중심에 비트맵을 그립니다.
            float left = (canvasWidth - newWidth) / 2.0f;
            float top = (canvasHeight - newHeight) / 2.0f;

            drawCanvas.drawBitmap(scaledBitmap, left, top, canvasPaint);
        }
        invalidate(); // View를 다시 그려 화면에 반영
    }
}