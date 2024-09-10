package com.example.holymoly;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import yuku.ambilwarna.AmbilWarnaDialog;

public class MakeDrowDiaryActivity extends AppCompatActivity {

    private CustomView drawView;
    private ImageButton selectedColorButton, undo, remove, rainbow, ok;
    private Map<ImageButton, Integer> colorButtonMap = new HashMap<>();
    private Map<ImageButton, Integer> colorCheckMap = new HashMap<>();
    private Map<Integer, String> colorCodeMap = new HashMap<>();
    private String selectedColorCode = "#CE6868"; // 기본 색상 코드 (검정색)
    private SeekBar penSeekBar; // 추가된 SeekBar


    /* firebase 초기화 */
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();

    /* 효과음 */
    private SharedPreferences pref;
    private boolean isSoundOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = getSharedPreferences("music", MODE_PRIVATE); // 효과음 초기화

        setContentView(R.layout.activity_make_drow_diary);
        rainbow = findViewById(R.id.rainbow); // rainbow 버튼 초기화
        drawView = findViewById(R.id.drawing); // XML에서 정의된 ID로 초기화
        undo = findViewById(R.id.ib_back);
        remove = findViewById(R.id.ib_remove);
        ok = findViewById(R.id.ib_ok);
        penSeekBar = findViewById(R.id.pen_seekbar); // SeekBar 초기화
        int[] colorButtonIds = {
                R.id.red, R.id.orange, R.id.yellow, R.id.green, R.id.blue,
                R.id.purple, R.id.rainbow
        };

        String [] colorCodes ={
          "#CE6868", "#EBB661", "#F7DF29", "#53C856", "#6295DB", "#847AB8","#FFFFFF"
        };

        int[] colorImages ={
                R.drawable.color_red2, R.drawable.color_orange2, R.drawable.color_yellow2,
                R.drawable.color_green2, R.drawable.color_blue2,R.drawable.color_purple2, R.drawable.color_rainbow2
        };

        int[] colorCheckedImages = {
                R.drawable.color_red3, R.drawable.color_orange3, R.drawable.color_yellow3,
                R.drawable.color_green3, R.drawable.color_blue3,R.drawable.color_purple3, R.drawable.color_rainbow3
        };

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

        // Undo 버튼 클릭 리스너 설정
        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.undo(); // Undo 기능 호출
            }
        });

        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.clearCanvas(); // 그림을 모두 지움
            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { uploadImage(); }
        });


        // SeekBar의 값을 펜 굵기에 설정하는 리스너 설정
        penSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 예: progress가 0일 때 15
                float penWidth = 15 + (progress * 9);
                drawView.setPenWidth(penWidth);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void handleColorButtonClick(ImageButton button) {
        // Rainbow 버튼 클릭 시 AmbilWarnaDialog 호출
        if (button.getId() == R.id.rainbow) {
            AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(this,
                    Color.parseColor(selectedColorCode), new AmbilWarnaDialog.OnAmbilWarnaListener() {
                @Override
                public void onOk(AmbilWarnaDialog dialog, int color) {
                    selectedColorCode = String.format("#%06X", (0xFFFFFF & color));
                    drawView.setColor(selectedColorCode);

                    // Rainbow 버튼의 이미지 업데이트
                    if (selectedColorButton != null) {
                        selectedColorButton.setImageResource(colorButtonMap.get(selectedColorButton));
                    }
                    selectedColorButton = rainbow;
                    selectedColorButton.setImageResource(colorCheckMap.get(rainbow));
                }

                @Override
                public void onCancel(AmbilWarnaDialog dialog) {
                    // 취소 버튼을 누른 경우 처리할 작업
                }
            });

            colorPicker.show();
        } else {
            if (selectedColorButton != null) {
                selectedColorButton.setImageResource(colorButtonMap.get(selectedColorButton));
            }
            selectedColorButton = button;
            selectedColorButton.setImageResource(colorCheckMap.get(button));
            selectedColorCode = colorCodeMap.get(button.getId());
            drawView.setColor(selectedColorCode);
        }
    }

    private void uploadImage() {
        // CustomView에서 Bitmap 생성
        Bitmap bitmap = Bitmap.createBitmap(drawView.getWidth(), drawView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawView.draw(canvas);

        // diaries 별로 저장된 경로
        StorageReference coverRef = storageRef.child("diaries/");

        // 현재 날짜를 yyyyMMdd 형식으로 가져오기
        String currentDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        // 파일 이름을 현재 날짜로 설정
        String fileName = user.getUid() + "_" + currentDate + ".png";

        // 이미지가 저장될 경로 설정
        StorageReference imageRef = coverRef.child(fileName);

        // bitmap을 png로 압축 및 저장
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        // 업로드 시작
        UploadTask uploadTask = imageRef.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(this, "이미지 업로드 성공", Toast.LENGTH_SHORT).show();
            Intent intent2 = new Intent(this, MakeDiaryActivity.class);
            intent2.putExtra("from", "drow");
            startActivity(intent2);
            finish();
        });
    }

    // 효과음
    public void sound() {
        isSoundOn = pref.getBoolean("on&off2", true);
        Intent intent = new Intent(this, SoundService.class);
        if (isSoundOn) startService(intent); // 효과음 on
        else stopService(intent);            // 효과음 off
    }
}
