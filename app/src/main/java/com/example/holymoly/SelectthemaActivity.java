package com.example.holymoly;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SelectthemaActivity extends AppCompatActivity implements UserInfoLoader {
    private ImageButton btnhome, btntrophy, btnsetting, btnnext;
    private RadioGroup radioGroup;
    private TextView name;
    private ImageView profile;

    private UserInfo userInfo = new UserInfo();
    private Karlo karlo;
    private Gemini gemini;
    private ActivityResultLauncher<Intent> customThemeLauncher;// ActivityResultLauncher 선언 (커스텀 테마 액티비티에서 결과를 받기 위함)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectthema);

        name = findViewById(R.id.mini_name);
        profile = findViewById(R.id.mini_profile);
        btnhome = findViewById(R.id.ib_homebutton);
        btntrophy = findViewById(R.id.ib_trophy);
        btnsetting = findViewById(R.id.ib_setting);
        radioGroup = findViewById(R.id.radioGroup);
        btnnext = findViewById(R.id.ib_nextStep);

        loadUserInfo(profile, name);
        gemini = new Gemini();
        karlo = new Karlo();

        // ActivityResultLauncher 초기화
        customThemeLauncher = registerForActivityResult( // ActivityResultLauncher 초기화
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                String customTheme = data.getStringExtra("customTheme");
                                if (customTheme != null) {
                                    updateCustomRadioButtonText(customTheme);
                                    translateTheme(customTheme); // 테마를 번역하고 이미지를 생성
                                }
                            }
                        }
                    }
                });

        btnhome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound();
                Intent intent = new Intent(SelectthemaActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        btntrophy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound();
                Intent intent = new Intent(SelectthemaActivity.this, TrophyActivity.class);
                startActivity(intent);
            }
        });

        btnsetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound();
                Intent intent = new Intent(SelectthemaActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });

        // 커스텀 라디오 버튼 추가
        addCustomRadioButton("바다", R.drawable.radio_sea, R.id.thema_sea);
        addCustomRadioButton("궁전", R.drawable.radio_castle, R.id.thema_castle);
        addCustomRadioButton("숲", R.drawable.radio_forest, R.id.thema_forest);
        addCustomRadioButton("마을", R.drawable.radio_village, R.id.thema_village);
        addCustomRadioButton("우주", R.drawable.radio_space, R.id.thema_house);
        addCustomRadioButton("사막", R.drawable.radio_desert, R.id.thema_desert);
        addCustomRadioButton("?", R.drawable.radio_custom, R.id.thema_custom);

        btnnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound();
                int selectedId = radioGroup.getCheckedRadioButtonId();
                if (selectedId == -1) { // 테마가 선택되지 않은 경우
                    Toast.makeText(SelectthemaActivity.this, "테마를 선택하세요!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 선택된 라디오 버튼의 텍스트를 가져옴
                View selectedRadioButton = findViewById(selectedId);
                TextView textView = selectedRadioButton.findViewById(R.id.radio_text);
                String themaText = textView.getText().toString();

                // 선택된 테마를 인텐트에 추가하고 다음 액티비티로 이동
                if ("?".equals(themaText)) {
                    Intent intent = new Intent(SelectthemaActivity.this, CustomThemeActivity.class);
                    customThemeLauncher.launch(intent);
                } else {
                    Intent intent = new Intent(SelectthemaActivity.this, SelectcharacterActivity.class);
                    intent.putExtra("selectedThema", themaText);
                    startActivity(intent);
                }
            }
        });
    }

    // "?" ID를 가진 라디오 버튼의 텍스트 업데이트
    private void updateCustomRadioButtonText(String newText) {
        View customRadioButton = findViewById(R.id.thema_custom);
        if (customRadioButton != null) {
            TextView textView = customRadioButton.findViewById(R.id.radio_text);
            textView.setText(newText);
        }
    }

    private void addCustomRadioButton(String text, int imageResId, int radioButtonId) {
        // 레이아웃 인플레이터를 사용하여 커스텀 라디오 버튼 레이아웃 인플레이트
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.custom_radio_button, radioGroup, false);

        // 레이아웃에서 뷰 찾기
        ImageView imageView = view.findViewById(R.id.radio_image);
        TextView textView = view.findViewById(R.id.radio_text);

        // 이미지와 텍스트 설정
        imageView.setImageResource(imageResId);
        textView.setText(text);

        view.setId(radioButtonId);// View에 ID 설정

        // View에 클릭 리스너 설정
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound();
                // 선택된 View의 ID를 라디오 그룹의 체크된 항목으로 설정
                radioGroup.check(v.getId());

                // 모든 라디오 버튼의 이미지 불투명도 초기화 (원래대로)
                for (int i = 0; i < radioGroup.getChildCount(); i++) {
                    View childView = radioGroup.getChildAt(i);
                    ImageView childImageView = childView.findViewById(R.id.radio_image);
                    childImageView.setAlpha(1.0f); // 원래대로 투명도 설정
                }

                // 선택된 라디오 버튼의 이미지 불투명도 설정
                imageView.setAlpha(0.4f);

                // 텍스트가 "?"인 경우 즉시 CustomThemeActivity로 이동
                if ("?".equals(textView.getText().toString())) {
                    Intent intent = new Intent(SelectthemaActivity.this, CustomThemeActivity.class);
                    customThemeLauncher.launch(intent);
                }
            }
        });

        // 라디오 그룹에 추가
        radioGroup.addView(view);
    }

    @Override
    public void loadUserInfo(ImageView profile, TextView name) {
        userInfo.loadUserInfo(profile, name);
    }

    //테마를 번역하는 함수
    private void translateTheme(final String theme) {
        String prompt = "Translate the following theme to English: " + theme + ". Please provide a concise, single-word or short-phrase answer.";
        gemini.generateText(prompt, new Gemini.Callback() {
            @Override
            public void onSuccess(String theme) {
                String prompt = "Dreamy, fairytale, cute, smooth, fancy, twinkle, bright, cartoon style " + theme;
                generateImage(prompt);
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(SelectthemaActivity.this, "테마 번역 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generateImage(String prompt) {
        karlo.requestImage(prompt, "", new Karlo.Callback() {
            @Override
            public void onSuccess(String imageUrl) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadBitmapFromUrl(imageUrl);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SelectthemaActivity.this, "이미지 생성 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void loadBitmapFromUrl(String imageUrl) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(imageUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(input);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateCustomRadioButtonImage(bitmap);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SelectthemaActivity.this, "이미지 로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    private void updateCustomRadioButtonImage(Bitmap bitmap) {
        View customRadioButton = findViewById(R.id.thema_custom);
        if (customRadioButton != null) {
            ImageView imageView = customRadioButton.findViewById(R.id.radio_image);
            Bitmap circularBitmap = getCircularBitmap(bitmap);
            imageView.setImageBitmap(circularBitmap);
        }
    }

    private Bitmap getCircularBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int minDimension = Math.min(width, height);

        Bitmap output = Bitmap.createBitmap(minDimension, minDimension, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, minDimension, minDimension);
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.BLACK);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, null, rect, paint);

        return output;
    }

    public void sound() {
        Intent intent = new Intent(this, SoundService.class);
        startService(intent);
    }
}
