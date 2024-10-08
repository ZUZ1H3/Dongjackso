package com.example.holymoly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import yuku.ambilwarna.AmbilWarnaDialog;

public class MakeBookcoverActivity extends AppCompatActivity {
    private CustomView drawView;
    private ImageButton pen, erase, undo, rainbow, paint, remove, ok, AI, stop;
    private ImageButton selectedColorButton, selectedToolButton;
    private ImageView loading;
    private Map<ImageButton, Integer> colorButtonMap = new HashMap<>();
    private Map<ImageButton, Integer> colorCheckMap = new HashMap<>();
    private Map<Integer, String> colorCodeMap = new HashMap<>();
    private String selectedColorCode = "#303030"; // 기본 색상 코드 (검정색)
    private SeekBar penSeekBar; // 추가된 SeekBar
    private String bookTitle = "";
    private String aloneTitle ="";
    private String selectedTheme, from;
    private ArrayList<String> selectedCharacters;
    private Karlo karlo;
    private Gemini gemini;
    private long backPressedTime = 0;

    /* firebase 초기화 */
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();
    private StorageReference imageRef = storage.getReference().child("background/");
    private StorageReference storyRef = storage.getReference().child("stories/");

    /* 효과음 */
    private SharedPreferences pref;
    private boolean isSoundOn;

    private String combinedStory, storyFile, imageFile;

    private Animation rotateAnimation; // rotateAnimation을 필드로 선언

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_bookcover);
        pref = getSharedPreferences("music", MODE_PRIVATE);
        rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate);

        initializeFields();
        setupButtons();
        setupColorButtons();
        setupSeekBar();
        setupDefaultToolAndColor();
    }

    private void initializeFields() {
        Intent intent = getIntent();
        bookTitle = intent.getStringExtra("bookTitle");
        karlo = new Karlo(768, 960);
        gemini = new Gemini();
        selectedTheme = intent.getStringExtra("selectedTheme");
        selectedCharacters = intent.getStringArrayListExtra("selectedCharacters");
        from = intent.getStringExtra("from");
        aloneTitle = intent.getStringExtra("title");
        combinedStory = intent.getStringExtra("combinedStory");
        storyFile = intent.getStringExtra("storyFile");
        imageFile = intent.getStringExtra("imageFile");
        drawView = findViewById(R.id.drawing);
        penSeekBar = findViewById(R.id.pen_seekbar);
        pen = findViewById(R.id.ib_pen);
        erase = findViewById(R.id.ib_erase);
        rainbow = findViewById(R.id.ib_rainbow);
        remove = findViewById(R.id.ib_remove);
        undo = findViewById(R.id.ib_back);
        ok = findViewById(R.id.ib_ok);
        AI = findViewById(R.id.ib_AI);
        paint = findViewById(R.id.ib_paint);
        stop = findViewById(R.id.ib_stopMaking);
        loading = findViewById(R.id.ib_loading);

    }

    private void setupButtons() {
        pen.setOnClickListener(v -> handleToolButtonClick(pen));
        erase.setOnClickListener(v -> {
            sound();
            handleToolButtonClick(erase);
            drawView.setColor("#FFFFFFFF");
        });
        ok.setOnClickListener(v -> {
            sound();
            uploadImage();
            ok.setOnClickListener(null); // 클릭 리스너 제거
        });
        AI.setOnClickListener(v -> {
            sound();
            loading.setVisibility(View.VISIBLE); // 로딩 이미지 표시
            loading.startAnimation(rotateAnimation);
            if(from.equals("AI")) {
                generateImageFromThemeAndCharacters(selectedTheme, selectedCharacters);
            } else if (from.equals("개인")) {
                keyword(combinedStory);
            }
        });
        stop.setOnClickListener(v -> {
            sound();
            handleBackPress();
            deleteFile();
        });
        remove.setOnClickListener(v -> {
            sound();
            drawView.clearCanvas();
        });
        paint.setOnClickListener(v -> {
            sound();
            applyPaintBucket();
        });
        undo.setOnClickListener(v -> {
            sound();
            drawView.undo();
        });
    }

    private void setupColorButtons() {
        int[] colorButtonIds = {
                R.id.ib_red, R.id.ib_skyblue, R.id.ib_orange, R.id.ib_purple,
                R.id.ib_yellow, R.id.ib_pink, R.id.ib_green, R.id.ib_black,
                R.id.ib_blue, R.id.ib_rainbow
        };

        String[] colorCodes = {
                "#CE6868", "#9ED4E0", "#EBB661", "#847AB8",
                "#F7DF29", "#EC96B0", "#53C856", "#303030",
                "#6295DB", "#FFFFFF"
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

        for (int i = 0; i < colorButtonIds.length; i++) {
            ImageButton button = findViewById(colorButtonIds[i]);
            colorButtonMap.put(button, colorImages[i]);
            colorCheckMap.put(button, colorCheckedImages[i]);
            colorCodeMap.put(colorButtonIds[i], colorCodes[i]);
            button.setOnClickListener(v -> handleColorButtonClick(button));
        }
    }

    private void setupSeekBar() {
        penSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float penWidth = 15 + (progress * 9);
                drawView.setPenWidth(penWidth);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupDefaultToolAndColor() {
        handleToolButtonClick(pen);
        ImageButton defaultColorButton = findViewById(R.id.ib_black);
        handleColorButtonClick(defaultColorButton);
    }

    private void handleBackPress() {
        if (System.currentTimeMillis() - backPressedTime >= 2000) {
            backPressedTime = System.currentTimeMillis();
            Toast.makeText(this, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
        } else {
            finish();
        }
    }

    private void handleColorButtonClick(ImageButton button) {
        if (button.getId() == R.id.ib_rainbow) {
            AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(this,
                    Color.parseColor(selectedColorCode), new AmbilWarnaDialog.OnAmbilWarnaListener() {
                @Override
                public void onOk(AmbilWarnaDialog dialog, int color) {
                    selectedColorCode = String.format("#%06X", (0xFFFFFF & color));
                    drawView.setColor(selectedColorCode);
                    if (selectedColorButton != null) {
                        selectedColorButton.setImageResource(colorButtonMap.get(selectedColorButton));
                    }
                    selectedColorButton = rainbow;
                    selectedColorButton.setImageResource(colorCheckMap.get(rainbow));
                }

                @Override
                public void onCancel(AmbilWarnaDialog dialog) {}
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

    private void handleToolButtonClick(ImageButton button) {
        if (selectedToolButton != null) {
            resetButtonImage(selectedToolButton);
        }
        selectedToolButton = button;
        setCheckedButtonImage(button);
        if (selectedColorButton != null) {
            drawView.setColor(selectedColorCode);
        }
    }

    private void resetButtonImage(ImageButton button) {
        int buttonId = button.getId();
        if (buttonId == R.id.ib_pen) {
            button.setImageResource(R.drawable.ic_pen);
        } else if (buttonId == R.id.ib_erase) {
            button.setImageResource(R.drawable.ic_erase);
        } else if (buttonId == R.id.ib_paint) {
            button.setImageResource(R.drawable.ic_paint);
        } else if (buttonId == R.id.ib_rainbow) {
            button.setImageResource(R.drawable.color_rainbow);
        }
    }

    private void setCheckedButtonImage(ImageButton button) {
        int buttonId = button.getId();
        if (buttonId == R.id.ib_pen) {
            button.setImageResource(R.drawable.ic_pen_check);
        } else if (buttonId == R.id.ib_erase) {
            button.setImageResource(R.drawable.ic_erase_check);
        } else if (buttonId == R.id.ib_paint) {
            button.setImageResource(R.drawable.ic_paint_check);
        } else if (buttonId == R.id.ib_rainbow) {
            button.setImageResource(R.drawable.color_rainbow_check);
        }
    }

    private void applyPaintBucket() {
        if (selectedColorButton != null) {
            drawView.setColor(selectedColorCode);
        }
        drawView.fillCanvas(selectedColorCode);
    }

    private void uploadImage() {
        // CustomView에서 Bitmap 생성
        Bitmap bitmap = Bitmap.createBitmap(drawView.getWidth(), drawView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawView.draw(canvas);

        // 표지 만들 때 저장될 경로
        StorageReference coverRef = storageRef.child("covers/");

        // 경로에 있는 파일 목록 가져오기
        coverRef.listAll().addOnSuccessListener(listResult -> {
            // 테마별 index 증가 처리
            int themeCount = 0;
            for (StorageReference item : listResult.getItems()) {
                // 파일 이름에서 테마를 추출하여 비교
                String userId = item.getName();
                String itemName = item.getName();
                String[] parts = itemName.split("_");
                if (userId.startsWith(user.getUid()) && parts.length > 2 && parts[1].equals(selectedTheme))
                    themeCount++;
            }
            int index = themeCount + 1;

            int aloneIndex = 1;
            for (StorageReference item : listResult.getItems()) {
                String fileName = item.getName();

                if (fileName.startsWith(user.getUid()) && fileName.contains("개인"))
                    aloneIndex++;
            }

            // AI와 함께일 때의 filename 변수
            String withAIFileName = user.getUid() + "_" + selectedTheme + "_" + index + "_" + bookTitle + ".png";
            // 혼자일 때의 filename 변수
            String aloneFileName = user.getUid() + "_" + from + "_" + aloneIndex + "_" + aloneTitle + ".png";

            // 이미지가 저장될 경로 설정
            StorageReference withAIRef = coverRef.child(withAIFileName); // AI와 만들기
            StorageReference aloneRef = coverRef.child(aloneFileName);   // 혼자 만들기

            // bitmap을 png로 압축 및 저장
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 80, baos);
            byte[] data = baos.toByteArray();

            // 업로드 시작
            if(from.equals("AI")) {
                // AI와 만들기
                UploadTask uploadTask = withAIRef.putBytes(data);
                uploadTask.addOnSuccessListener(taskSnapshot -> {
                    Map<String, Object> fileData = new HashMap<>();
                    fileData.put("timestamp", new Timestamp(new Date()));

                    db.collection("covers").document(user.getUid()).collection("filename").document(withAIFileName)
                            .set(fileData)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(this, "이미지 업로드 성공", Toast.LENGTH_SHORT).show();
                                Intent musicIntent = new Intent(this, MusicService.class);
                                musicIntent.setAction("CHANGE_MUSIC");
                                musicIntent.putExtra("MUSIC_RES_ID", R.raw.ocean_theme_music);
                                startService(musicIntent); // 배경음악 기본으로 변경
                                Intent intent = new Intent(this, AlbumActivity.class);
                                intent.putExtra("booktitle", bookTitle);
                                startActivity(intent);
                                finish();
                            });
                    });
            }
            else if(from.equals("개인")) {
                // 혼자 만들기
                UploadTask aloneUploadTask = aloneRef.putBytes(data);
                aloneUploadTask.addOnSuccessListener(taskSnapshot -> {
                    Map<String, Object> fileData = new HashMap<>();
                    fileData.put("timestamp", new Timestamp(new Date()));

                    db.collection("covers").document(user.getUid()).collection("filename").document(aloneFileName)
                            .set(fileData)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(this, "이미지 업로드 성공", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(this, AlbumActivity.class);
                                startActivity(intent);
                                finish();
                            });
                });
            }
        });
    }

    private void generateImage(String prompt) {
        karlo.requestImage(prompt, "", new Karlo.Callback() {
            @Override
            public void onSuccess(String imageUrl) {
                new LoadImageTask().execute(imageUrl);
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MakeBookcoverActivity.this, "이미지 생성 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        loading.clearAnimation();  // 애니메이션 중지
                        loading.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    private class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            return getBitmapFromURL(url);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                drawView.drawBitmapOnCanvas(result);
                loading.clearAnimation();  // 애니메이션 중지
                loading.setVisibility(View.GONE);

            } else {
                Toast.makeText(MakeBookcoverActivity.this, "이미지 로드 실패", Toast.LENGTH_SHORT).show();
                loading.clearAnimation();  // 애니메이션 중지
                loading.setVisibility(View.GONE);

            }
        }
    }

    // URL에서 Bitmap 객체를 생성하는 메서드
    private Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //선택한 테마를 영어로 번역
    public void translateTheme(String theme, TranslationCallback callback) {
        String prompt = "Translate the following theme to English: " + theme + ". Please provide a concise, single-word or short-phrase answer.";
        gemini.generateText(prompt, new Gemini.Callback() {
            @Override
            public void onSuccess(String text) {
                callback.onSuccess(text.trim());
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    //선택한 캐릭터를 영어로 번역
    public void translateCharacters(ArrayList<String> characters, TranslationCallback callback) {
        StringBuilder promptBuilder = new StringBuilder("Translate the following character names to English and prepend 'a cute ' before each noun. Separate the nouns with commas: ");
        for (int i = 0; i < characters.size(); i++) {
            promptBuilder.append(characters.get(i));
            if (i < characters.size() - 1) {
                promptBuilder.append(", ");
            }
        }
        String prompt = promptBuilder.toString();

        gemini.generateText(prompt, new Gemini.Callback() {
            @Override
            public void onSuccess(String text) {
                callback.onSuccess(text.trim());
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    private void generateImageFromThemeAndCharacters(String theme, ArrayList<String> characters) {
        translateTheme(theme, new TranslationCallback() {
            @Override
            public void onSuccess(String translatedTheme) {
                translateCharacters(characters, new TranslationCallback() {
                    @Override
                    public void onSuccess(String translatedCharacters) {
                        String prompt = "Dreamy, fairytale, cute, smooth, fancy, twinkle, super bright, cartoon style. " + translatedCharacters + " are together. the background of a " + translatedTheme;
                        generateImage(prompt);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Toast.makeText(MakeBookcoverActivity.this, "캐릭터 번역 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(MakeBookcoverActivity.this, "테마 번역 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 효과음
    public void sound() {
        isSoundOn = pref.getBoolean("on&off2", true);
        Intent intent = new Intent(this, SoundService.class);
        if (isSoundOn) startService(intent); // 효과음 on
        else stopService(intent);            // 효과음 off
    }

    public void keyword(String combinedStory) {
        String prompt = "동화 스토리: "+ combinedStory + "\n이 동화에서, 등장인물과 배경 등의 중요 키워드를 뽑아 영어단어로 알려주세요. " +
                "단답형으로 대답하시고, 단어와 단어 사이에는 ', '로 이어주세요. ex) rabbit, sea, castle";
        gemini.generateText(prompt, new Gemini.Callback() {
            @Override
            public void onSuccess(String text) {
                String prompt = "Dreamy, fairytale, cute, smooth, fancy, twinkle, super bright, cartoon style. " + text;
                generateImage(prompt);
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(MakeBookcoverActivity.this, "(개인)이미지 생성 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteFile() {
        // 이미지 파일 삭제
        imageRef.child(selectedTheme + "/" + imageFile).delete().addOnSuccessListener(aVoid -> {
            // 스토리 파일 삭제
            storyRef.child(storyFile).delete().addOnSuccessListener(aVoid2 -> {
                finish();
            });
        });
    }
}
