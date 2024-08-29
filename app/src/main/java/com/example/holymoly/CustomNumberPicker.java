package com.example.holymoly;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.NumberPicker;
import android.widget.TextView;

public class CustomNumberPicker extends NumberPicker {

    public CustomNumberPicker(Context context) {
        super(context);
        init();
    }

    public CustomNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomNumberPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 설정할 색상과 크기
        final int textColor = Color.parseColor("#FF69B4"); // 핑크색
        final float textSize = 30; // 24sp

        // NumberPicker의 텍스트 색상과 크기를 설정
        setFormatter(value -> String.format("%d", value));

        // NumberPicker의 내부 TextView들에 접근하여 색상과 크기 설정
        setOnValueChangedListener((picker, oldVal, newVal) -> updateTextViews(textColor, textSize));
        updateTextViews(textColor, textSize);
    }

    private void updateTextViews(int color, float size) {
        for (int i = 0; i < getChildCount(); i++) {
            try {
                Object child = getChildAt(i);
                if (child instanceof TextView) {
                    TextView textView = (TextView) child;
                    textView.setTextColor(color);
                    textView.setTextSize(size);
                }
            } catch (ClassCastException e) {
            }
        }
    }
}

