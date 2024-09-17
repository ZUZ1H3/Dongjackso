package com.example.holymoly;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

public class StrokeText extends AppCompatTextView {
    private boolean stroke = false;
    private float strokeWidth = 0;
    private int strokeColor;

    public StrokeText(Context context) {
        super(context);
        init(null);
    }

    public StrokeText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public StrokeText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    /* 동적으로 */
    public void setStroke(boolean stroke) {
        this.stroke = stroke;
        invalidate(); // 뷰를 다시 그리도록 요청
    }

    private void init(AttributeSet attrs) {
        // attrs.xml에서 정의한 속성을 가져옴
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.StrokeText);
        stroke = a.getBoolean(R.styleable.StrokeText_textStroke, false);
        strokeWidth = a.getFloat(R.styleable.StrokeText_textStrokeWidth,0);
        strokeColor = a.getColor(R.styleable.StrokeText_textStrokeColor, 0);
        a.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
       if(stroke) {
           ColorStateList states = getTextColors();
           getPaint().setStyle(Paint.Style.STROKE);
           getPaint().setStrokeJoin(Paint.Join.ROUND);
           getPaint().setStrokeWidth(strokeWidth);
           getPaint().setTextSize(getTextSize());
           setTextColor(strokeColor);
           super.onDraw(canvas); // 외곽선 그리기

           getPaint().setStyle(Paint.Style.FILL);
           setTextColor(states);
       }
        super.onDraw(canvas); // 텍스트 그리기
    }
}
