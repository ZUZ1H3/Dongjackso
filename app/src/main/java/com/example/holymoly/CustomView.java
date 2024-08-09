package com.example.holymoly;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CustomView extends View {
    private Paint paint;

    public CustomView(Context context) {
        super(context);
        init();
    }

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(5);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.WHITE);
        //canvas.drawLine(50, 50, 200, 200, paint);
        //canvas.drawRect(50, 250, 200, 400, paint);
        //canvas.drawCircle(150, 550, 100, paint);
        //paint.setTextSize(50);
        //canvas.drawText("Hello Canvas", 50, 700, paint);
    }
}
