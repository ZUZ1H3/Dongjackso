package com.example.holymoly;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

public class MakeBookcoverActivity extends AppCompatActivity {

    private CustomView drawView;
    private ImageButton pen, erase, paint, undo;
    private ImageButton selectedColorButton, selectedToolButton;
    private Map<ImageButton, Integer> colorButtonMap = new HashMap<>();
    private Map<ImageButton, Integer> colorCheckMap = new HashMap<>();
    private Map<Integer, String> colorCodeMap = new HashMap<>();
    private String selectedColorCode = "#000000"; // 기본 색상 코드 (검정색)
    private SeekBar penSeekBar; // 추가된 SeekBar


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_bookcover);

        // 버튼 초기화
        pen = findViewById(R.id.ib_pen);
        erase = findViewById(R.id.ib_erase);
        paint = findViewById(R.id.ib_paint);
        drawView = findViewById(R.id.drawing);
        penSeekBar = findViewById(R.id.pen_seekbar); // SeekBar 초기화
        undo = findViewById(R.id.ib_back); // Undo 버튼 초기화


        // 색상 버튼과 리소스 매핑
        int[] colorButtonIds = {
                R.id.ib_red, R.id.ib_skyblue, R.id.ib_orange, R.id.ib_purple,
                R.id.ib_yellow, R.id.ib_pink, R.id.ib_green, R.id.ib_black,
                R.id.ib_blue, R.id.ib_rainbow
        };

        String[] colorCodes = {
                "#CE6868", "#9ED4E0", "#EBB661", "#847AB8",
                "#F7DF29", "#EC96B0", "#53C856", "#303030",
                "#6295DB", "#FFFFFF" // Example color code for rainbow
        };

        int[] colorImages = {
                R.drawable.color_red, R.drawable.color_skyblue, R.drawable.color_orange,
                R.drawable.color_purple, R.drawable.color_yellow, R.drawable.color_pink,
                R.drawable.color_green, R.drawable.color_black, R.drawable.color_blue,
                R.drawable.color_rainbow
        };

        int[] colorCheckedImages = {
                R.drawable.color_red_check, R.drawable.color_skyblue_check,
                R.drawable.color_orange_check, R.drawable.color_purple_check,
                R.drawable.color_yellow_check, R.drawable.color_pink_check,
                R.drawable.color_green_check, R.drawable.color_black_check,
                R.drawable.color_blue_check, R.drawable.color_rainbow_check
        };

        // 색상 버튼과 리소스 매핑
        for (int i = 0; i < colorButtonIds.length; i++) {
            ImageButton button = findViewById(colorButtonIds[i]);
            colorButtonMap.put(button, colorImages[i]);
            colorCheckMap.put(button, colorCheckedImages[i]);
            colorCodeMap.put(colorButtonIds[i], colorCodes[i]);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleColorButtonClick(button);
                }
            });
        }

        // 도구 버튼 클릭 리스너 설정
        pen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleToolButtonClick(pen);
                // 도구 변경 시 현재 선택된 색상으로 설정
                if (selectedColorButton != null) {
                    drawView.setColor(selectedColorCode);
                }
            }
        });

        erase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleToolButtonClick(erase);
                // 도구 변경 시 흰색으로 설정
                drawView.setColor("#FFFFFFFF");
            }
        });

        paint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleToolButtonClick(paint);
                // 도구 변경 시 현재 선택된 색상으로 설정
                if (selectedColorButton != null) {
                    drawView.setColor(selectedColorCode);
                }
            }
        });

        // SeekBar의 값을 펜 굵기에 설정하는 리스너 설정
        penSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 예: progress가 0일 때 15, 1일때 23... 7씩 증가함
                float penWidth = 15 + (progress * 7);
                drawView.setPenWidth(penWidth);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 터치가 시작될 때 호출됩니다.
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 터치가 끝날 때 호출됩니다.
            }
        });

        // Undo 버튼 클릭 리스너 설정
        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.undo(); // Undo 기능 호출
            }
        });

        // 앱 실행 시 기본 선택된 도구와 색상 설정
        selectDefaultToolAndColor();
    }

    private void selectDefaultToolAndColor() {
        // 기본 도구로 '펜' 버튼 선택
        handleToolButtonClick(pen);
        ImageButton defaultColorButton = findViewById(R.id.ib_black); // 기본 색상 버튼
        handleColorButtonClick(defaultColorButton);
    }

    private void handleColorButtonClick(ImageButton button) {
        // 현재 선택된 색상 버튼의 이미지 리소스를 원래 상태로 복원
        if (selectedColorButton != null) {
            selectedColorButton.setImageResource(colorButtonMap.get(selectedColorButton));
        }
        // 선택된 색상 버튼을 체크된 이미지로 변경
        selectedColorButton = button;
        selectedColorButton.setImageResource(colorCheckMap.get(button));
        // 색상 코드 업데이트
        selectedColorCode = colorCodeMap.get(button.getId());
        // 현재 선택된 색상으로 설정
        drawView.setColor(selectedColorCode);
    }

    private void handleToolButtonClick(ImageButton button) {
        // 현재 선택된 도구 버튼의 이미지 리소스를 원래 상태로 복원
        if (selectedToolButton != null) {
            resetButtonImage(selectedToolButton);
        }
        // 선택된 도구 버튼을 체크된 이미지로 변경
        selectedToolButton = button;
        setCheckedButtonImage(button);
    }

    private void resetButtonImage(ImageButton button) {
        // 각 버튼의 기본 이미지를 설정
        int buttonId = button.getId();
        if (buttonId == R.id.ib_pen) {
            button.setImageResource(R.drawable.ic_pen);
        } else if (buttonId == R.id.ib_erase) {
            button.setImageResource(R.drawable.ic_erase);
        } else if (buttonId == R.id.ib_paint) {
            button.setImageResource(R.drawable.ic_paint);
        }
    }

    private void setCheckedButtonImage(ImageButton button) {
        // 각 버튼의 체크된 이미지를 설정
        int buttonId = button.getId();
        if (buttonId == R.id.ib_pen) {
            button.setImageResource(R.drawable.ic_pen_check);
        } else if (buttonId == R.id.ib_erase) {
            button.setImageResource(R.drawable.ic_erase_check);
        } else if (buttonId == R.id.ib_paint) {
            button.setImageResource(R.drawable.ic_paint_check);
        }
    }
}
