package com.example.holymoly;

import android.content.Intent;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import yuku.ambilwarna.AmbilWarnaDialog;

public class MakeBookcoverActivity extends AppCompatActivity {

    private CustomView drawView;
    private ImageButton pen, erase, undo, rainbow, remove, ok, AI;
    private ImageButton selectedColorButton, selectedToolButton;
    private Map<ImageButton, Integer> colorButtonMap = new HashMap<>();
    private Map<ImageButton, Integer> colorCheckMap = new HashMap<>();
    private Map<Integer, String> colorCodeMap = new HashMap<>();
    private String selectedColorCode = "#303030"; // 기본 색상 코드 (검정색)
    private SeekBar penSeekBar; // 추가된 SeekBar
    private String bookTitle = "";
    private String selectedTheme;
    private ArrayList<String> selectedCharacters;
    private Karlo karlo;
    private Gemini gemini;

    /* firebase 초기화 */
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_bookcover);

        // Intent에서 테마를 가져옴
        Intent intent = getIntent();
        bookTitle = intent.getStringExtra("bookTitle");
        karlo = new Karlo(768, 960);
        gemini = new Gemini();
        selectedTheme = intent.getStringExtra("selectedTheme");
        selectedCharacters = intent.getStringArrayListExtra("selectedCharacters");

        // 버튼 초기화
        pen = findViewById(R.id.ib_pen);
        erase = findViewById(R.id.ib_erase);
        rainbow = findViewById(R.id.ib_rainbow); // rainbow 버튼 초기화
        remove = findViewById(R.id.ib_remove); // remove 버튼 초기화
        drawView = findViewById(R.id.drawing);
        penSeekBar = findViewById(R.id.pen_seekbar); // SeekBar 초기화
        undo = findViewById(R.id.ib_back); // Undo 버튼 초기화
        ok = findViewById(R.id.ib_ok);
        AI = findViewById(R.id.ib_AI);


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

        // 색상 버튼과 리소스 매핑u
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

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { uploadImage(); }
        });

        AI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateImageFromThemeAndCharacters(selectedTheme, selectedCharacters);
            }
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
                // 터치가 시작될 때 호출됩니다.
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 터치가 끝날 때 호출됩니다.
            }
        });

        // Remove 버튼 클릭 리스너 설정
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.clearCanvas(); // 그림을 모두 지움
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
        // Rainbow 버튼 클릭 시 AmbilWarnaDialog 호출
        if (button.getId() == R.id.ib_rainbow) {
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
            // 기존 색상 버튼 클릭 시 처리
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
        } else if (buttonId == R.id.ib_rainbow) {
            button.setImageResource(R.drawable.color_rainbow); // rainbow 기본 이미지
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
        } else if (buttonId == R.id.ib_rainbow) {
            button.setImageResource(R.drawable.color_rainbow_check); // rainbow 체크 이미지
        }
    }

    private void uploadImage() {
        // CustomView에서 Bitmap 생성
        Bitmap bitmap = Bitmap.createBitmap(drawView.getWidth(), drawView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawView.draw(canvas);

        // Intent로부터 데이터 가져오기
        Intent intent = getIntent();
        String theme = intent.getStringExtra("selectedTheme");
        // cover 별로 저장된 경로
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
                if (userId.startsWith(user.getUid()) && parts.length > 2 && parts[1].equals(theme))
                    themeCount++;
            }
            int index = themeCount + 1;
            // index가 아니라 책 표지 제목으로 변경할 것.
            String fileName = user.getUid() + "_" + theme + "_" + index + "_" + bookTitle + ".png";

            // 이미지가 저장될 경로 설정
            StorageReference imageRef = coverRef.child(fileName);

            // bitmap을 png로 압축 및 저장
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 80, baos);
            byte[] data = baos.toByteArray();

            // 업로드 시작
            UploadTask uploadTask = imageRef.putBytes(data);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                Toast.makeText(this, "이미지 업로드 성공", Toast.LENGTH_SHORT).show();
                Intent intent2 = new Intent(MakeBookcoverActivity.this, AlbumActivity.class);
                intent2.putExtra("booktitle", bookTitle);
                startActivity(intent2);
            });
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
            } else {
                Toast.makeText(MakeBookcoverActivity.this, "이미지 로드 실패", Toast.LENGTH_SHORT).show();
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


}
