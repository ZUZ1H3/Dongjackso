package com.example.holymoly;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
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
import java.io.IOException;
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
    private ImageButton selectedColorButton, undo, remove, rainbow, paint, ok, erase, image;
    private Map<ImageButton, Integer> colorButtonMap = new HashMap<>();
    private Map<ImageButton, Integer> colorCheckMap = new HashMap<>();
    private Map<Integer, String> colorCodeMap = new HashMap<>();
    private String selectedColorCode = "#CE6868"; // 기본 색상 코드 (검정색)
    private SeekBar penSeekBar; // 추가된 SeekBar
    private static final int PICK_IMAGE_REQUEST = 1;

    /* firebase 초기화 */
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();

    /* 효과음 */
    private SharedPreferences pref;
    private boolean isSoundOn;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_drow_diary);
        initUI();  // UI 초기화 메소드
        setupColorButtons(); // 색상 버튼 설정 메소드
        setupListeners();  // 리스너 설정 메소드
    }

    private void initUI() {
        drawView = findViewById(R.id.drawing);
        undo = findViewById(R.id.ib_back);
        remove = findViewById(R.id.ib_remove);
        ok = findViewById(R.id.ib_ok);
        penSeekBar = findViewById(R.id.pen_seekbar);
        paint = findViewById(R.id.ib_paint);
        erase = findViewById(R.id.ib_erase);
        image = findViewById(R.id.ib_image);
        pref = getSharedPreferences("music", MODE_PRIVATE); // 효과음 초기화
    }

    private void setupListeners() {
        undo.setOnClickListener(v -> {
            sound(); // 효과음 재생
            drawView.undo();
        });
        remove.setOnClickListener(v -> {
            sound(); // 효과음 재생
            drawView.clearCanvas();
        });
        ok.setOnClickListener(v -> {
            sound(); // 효과음 재생
            uploadImage();
        });
        paint.setOnClickListener(v -> {
            sound(); // 효과음 재생
            applyPaintBucket();
        });

        image.setOnClickListener(v -> {
            sound(); // 효과음 재생
            openGallery(); // 갤러리 열기
        });

        penSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                drawView.setPenWidth(15 + (progress * 9));  // 펜 굵기 조정
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    // 색상 버튼 설정
    private void setupColorButtons() {
        int[] colorButtonIds = {R.id.red, R.id.orange, R.id.yellow, R.id.green, R.id.blue, R.id.purple, R.id.rainbow, R.id.ib_erase};
        String[] colorCodes = {"#E86767", "#FCBF5B", "#FFE62A", "#53C856", "#6295DB", "#8577CB", "#FFFFFF", "#FFFFFF"};
        int[] colorImages = {R.drawable.color_red2, R.drawable.color_orange2, R.drawable.color_yellow2,
                R.drawable.color_green2, R.drawable.color_blue2, R.drawable.color_purple2, R.drawable.color_rainbow2, R.drawable.ic_erase};
        int[] colorCheckedImages = {R.drawable.color_red3, R.drawable.color_orange3, R.drawable.color_yellow3,
                R.drawable.color_green3, R.drawable.color_blue3, R.drawable.color_purple3, R.drawable.color_rainbow3, R.drawable.ic_erase_check};

        for (int i = 0; i < colorButtonIds.length; i++) {
            ImageButton button = findViewById(colorButtonIds[i]);
            colorButtonMap.put(button, colorImages[i]);
            colorCheckMap.put(button, colorCheckedImages[i]);
            colorCodeMap.put(colorButtonIds[i], colorCodes[i]);

            button.setOnClickListener(v -> handleColorButtonClick(button));
        }
    }

    // 색상 버튼 클릭 처리
    private void handleColorButtonClick(ImageButton button) {
        if (button.getId() == R.id.rainbow) {
            showColorPicker();
        } else {
            updateSelectedColor(button);
        }
    }

    private void showColorPicker() {
        new AmbilWarnaDialog(this, Color.parseColor(selectedColorCode), new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                selectedColorCode = String.format("#%06X", (0xFFFFFF & color));
                drawView.setColor(selectedColorCode);
                updateColorButtonSelection(findViewById(R.id.rainbow));
            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
            }
        }).show();
    }

    // 선택된 색상 및 버튼 업데이트
    private void updateSelectedColor(ImageButton button) {
        updateColorButtonSelection(button);
        selectedColorCode = colorCodeMap.get(button.getId());
        drawView.setColor(selectedColorCode);
    }

    // 색상 버튼의 선택 상태 업데이트
    private void updateColorButtonSelection(ImageButton button) {
        if (selectedColorButton != null) {
            selectedColorButton.setImageResource(colorButtonMap.get(selectedColorButton));
        }
        selectedColorButton = button;
        selectedColorButton.setImageResource(colorCheckMap.get(button));
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

    private void applyPaintBucket() {
        // 페인트통 색상으로 전체 캔버스를 채움
        drawView.fillCanvas(selectedColorCode);
    }

    // 효과음
    public void sound() {
        isSoundOn = pref.getBoolean("on&off2", true);
        Intent intent = new Intent(this, SoundService.class);
        if (isSoundOn) startService(intent); // 효과음 on
        else stopService(intent);            // 효과음 off
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                // 선택한 이미지를 비트맵으로 변환
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                drawView.drawBitmapOnCanvas(bitmap); // CustomView에 비트맵 그리기
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "이미지를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}