package com.example.holymoly;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class SelectcharacterActivity extends AppCompatActivity implements UserInfoLoader {
    private ImageButton btnhome, btntrophy, btnsetting, btnnext;
    private View[] customCheckBoxes; // 캐릭터를 저장할 체크박스 배열
    private CharacterData.CharacterInfo[] characters; // 캐릭터 정보를 저장할 배열
    private boolean[] isChecked; // 체크 상태를 저장할 배열
    private TextView name;
    private ImageView profile;
    private String thema;

    private UserInfo userInfo = new UserInfo();

    private Gemini gemini;
    private Karlo karlo;
    private ActivityResultLauncher<Intent> customCharacterLauncher;// ActivityResultLauncher 선언 (커스텀 캐릭터 액티비티에서 결과를 받기 위함)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectcharacter);

        MainActivity mainActivity = new MainActivity();
        mainActivity.actList().add(this);

        gemini = new Gemini();
        karlo = new Karlo();
        profile = findViewById(R.id.mini_profile);
        name = findViewById(R.id.mini_name);
        btnhome = findViewById(R.id.ib_homebutton);
        btntrophy = findViewById(R.id.ib_trophy);
        btnsetting = findViewById(R.id.ib_setting);
        btnnext = findViewById(R.id.ib_nextStep);

        loadUserInfo(profile, name);

        btnhome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectcharacterActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        btntrophy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectcharacterActivity.this, TrophyActivity.class);
                startActivity(intent);
            }
        });

        btnsetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectcharacterActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });

        // ActivityResultLauncher 초기화
        customCharacterLauncher = registerForActivityResult( // ActivityResultLauncher 초기화
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                String customCharacter = data.getStringExtra("customCharacter");
                                if (customCharacter != null) {
                                    updateCustomCheckBoxText(customCharacter);
                                    translateCharacter(customCharacter); // 테마를 번역하고 이미지를 생성
                                }
                            }
                        }
                    }
                });

        // Intent에서 테마를 가져옴
        Intent intent = getIntent();
        thema = intent.getStringExtra("selectedThema");
        if (thema != null) {
            // 테마에 따른 캐릭터 이름 요청
            characters = CharacterData.themeCharacterMap.get(thema);
        } else {
            Toast.makeText(this, "테마가 없습니다", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (characters == null) {
            randomCharacters(thema);

        } else {
            initializeUI();
        }
    }

    private void initializeUI() {

        btnnext.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ArrayList<String> selectedCharacters = new ArrayList<>();
                for (int i = 0; i < isChecked.length; i++) {
                    TextView textView = customCheckBoxes[i].findViewById(R.id.checkbox_text);
                    if (isChecked[i]) {
                        String characterName = textView.getText().toString();
                        if (characterName != null && !characterName.isEmpty() && !"?".equals(characterName)) {
                            selectedCharacters.add(characterName);
                        }
                    }
                }

                if (selectedCharacters.isEmpty()) {
                    Toast.makeText(SelectcharacterActivity.this, "캐릭터를 선택해주세요", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(SelectcharacterActivity.this, MakeStoryActivity.class);
                    intent.putStringArrayListExtra("selectedCharacters", selectedCharacters);
                    intent.putExtra("selectedTheme", thema);
                    startActivity(intent);
                }
            }
        });

        LinearLayout hllFirst = findViewById(R.id.hll_first);
        LinearLayout hllSecond = findViewById(R.id.hll_second);
        LayoutInflater inflater = getLayoutInflater();

        // customCheckBoxes 배열 초기화
        customCheckBoxes = new View[characters.length];
        isChecked = new boolean[characters.length];

        // 커스텀 체크박스 초기화
        for (int i = 0; i < characters.length; i++) {
            customCheckBoxes[i] = inflater.inflate(R.layout.custom_checkbox, hllFirst, false);
            final int index = i; // 인덱스를 final로 설정
            customCheckBoxes[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView textView = customCheckBoxes[index].findViewById(R.id.checkbox_text);
                    if ("?".equals(textView.getText().toString())) {
                        Intent intent = new Intent(SelectcharacterActivity.this, CustomCharacterActivity.class);
                        customCharacterLauncher.launch(intent);
                    }

                    isChecked[index] = !isChecked[index]; // 상태 변경
                    updateCheckBoxBackground(customCheckBoxes[index], isChecked[index]); // 배경 업데이트
                }
            });
            if (i < 5) {
                hllFirst.addView(customCheckBoxes[i]);
            } else {
                hllSecond.addView(customCheckBoxes[i]);
            }
        }

        // 체크박스에 캐릭터 이름 및 이미지 설정
        setCheckBoxNamesAndImages();
    }

    // 체크박스에 캐릭터 이름 및 이미지 설정
    private void setCheckBoxNamesAndImages() {
        for (int i = 0; i < customCheckBoxes.length; i++) {
            TextView textView = customCheckBoxes[i].findViewById(R.id.checkbox_text);
            textView.setText(characters[i].name);

            ImageView imageView = customCheckBoxes[i].findViewById(R.id.checkbox_image);
            imageView.setImageResource(characters[i].imageResId);
        }
    }

    // 체크박스 배경 업데이트 메서드
    private void updateCheckBoxBackground(View customCheckBox, boolean isChecked) {
        ImageView imageView = customCheckBox.findViewById(R.id.checkbox_image);
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();

        if (drawable != null) {
            int alpha = isChecked ? 128 : 255; // 체크되었을 때는 투명도 50%, 체크 해제 시 투명도 100%
            drawable.setAlpha(alpha);
        }
    }

    private void updateCustomCheckBoxText(String newText) {
        TextView textView = customCheckBoxes[9].findViewById(R.id.checkbox_text);
        if ("?".equals(textView.getText().toString())) {
            textView.setText(newText);
        }
    }

    @Override
    public void loadUserInfo(ImageView profile, TextView name) {
        userInfo.loadUserInfo(profile, name);
    }

    private void randomCharacters(String thema) {
        String prompt = thema + "테마를 주제로 동화를 만들려고 합니다." +
                "동화에 어울릴만한 등장인물의 후보가 10개 필요합니다. 1~5글자로 단답형으로 답해주세요." +
                "장소, 날씨가 등장인물이 될 수는 없으니 제외해주세요. 중복은 없어야 하며 비슷한 개념도 제외해주세요." +
                "예를 들어, '용'과 '드래곤'은 같은 개념입니다. '사탕'과 '캔디'도 뜻이 일치하기 때문에 같은 개념입니다." +
                "후보와 후보 사이에는 ', '로 띄어주세요." +
                "그러면 저희는 당신이 정해준 후보들 중 몇 개를 선택하여 동화를 만들 것입니다." +
                "당신이 답변 할 형식의 예시는 이렇습니다. '공주, 왕자, 물고기, 나무, 토끼, 거북이, 호랑이, 나비, 엄마, 동생";
        gemini.generateText(prompt, new Gemini.Callback() {
            @Override
            public void onSuccess(String text) {
                String[] characterNamesArray = text.split(", ");
                characters = new CharacterData.CharacterInfo[characterNamesArray.length];

                // 캐릭터 생성 완료 후에 한 번만 Toast 메시지 표시
                runOnUiThread(() -> Toast.makeText(SelectcharacterActivity.this, "캐릭터 생성 완료.", Toast.LENGTH_SHORT).show());

                for (int i = 0; i < characterNamesArray.length; i++) {
                    characters[i] = new CharacterData.CharacterInfo(characterNamesArray[i], R.drawable.ic_customcharacter);
                }

                runOnUiThread(() -> {
                    initializeUI();
                    for (int i = 0; i < characterNamesArray.length; i++) {
                        if (characters[i] != null) {
                            generateImage(characterNamesArray[i], customCheckBoxes[i]);
                        }
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() -> {
                    Toast.makeText(SelectcharacterActivity.this, "캐릭터를 생성하는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    // 커스텀 테마의 등장인물 이미지를 생성
    private void generateImage(String characterName, View customCheckBox) {
        translateCharacter(characterName, new Gemini.Callback() {
            @Override
            public void onSuccess(String translatedName) {
                if (translatedName != null) {
                    String prompt = "Dreamy, cute, fairytale, simple, twinkle " + translatedName + " a sky blue background";
                    String negative_prompt = "";
                    karlo.requestImage(prompt, negative_prompt, new Karlo.Callback() {
                        @Override
                        public void onSuccess(String imageUrl) {
                            Bitmap bitmap = getBitmapFromURL(imageUrl);
                            Bitmap circularBitmap = getCircularBitmap(bitmap);
                            runOnUiThread(() -> {
                                ImageView imageView = customCheckBox.findViewById(R.id.checkbox_image);
                                imageView.setImageBitmap(circularBitmap);
                            });
                        }

                        @Override
                        public void onFailure(Exception e) {
                            runOnUiThread(() -> {
                                Toast.makeText(SelectcharacterActivity.this, "이미지 요청 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(SelectcharacterActivity.this, "이름 번역에 실패했습니다.", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() -> Toast.makeText(SelectcharacterActivity.this, "번역 요청 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    // 번역 결과를 저장하고 콜백을 통해 결과를 반환하는 메서드
    private void translateCharacter(String characterName, Gemini.Callback callback) {
        String prompt = "Translate the following character name to English: " + characterName;
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
    private void translateCharacter(String characterName){
        String prompt = "Translate the following character to English: " + characterName + ". Please provide a concise, single-word or short-phrase answer.";
        gemini.generateText(prompt, new Gemini.Callback() {
            @Override
            public void onSuccess(String theme) {
                String prompt = "Dreamy, fairytale, cute, smooth, fancy, twinkle, bright, cartoon style " + theme;
                generateImage(prompt);
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(SelectcharacterActivity.this, "테마 번역 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(SelectcharacterActivity.this, "이미지 생성 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
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
                            updateCustomCheckBoxImage(bitmap);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SelectcharacterActivity.this, "이미지 로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    private void updateCustomCheckBoxImage(Bitmap bitmap) {
        ImageView imageView = customCheckBoxes[9].findViewById(R.id.checkbox_image);
            Bitmap circularBitmap = getCircularBitmap(bitmap);
            imageView.setImageBitmap(circularBitmap);
        }

    // 이미지를 원형으로 만드는 메서드
    private Bitmap getCircularBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int minEdge = Math.min(width, height);
        Bitmap output = Bitmap.createBitmap(minEdge, minEdge, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        RectF rect = new RectF(0, 0, minEdge, minEdge);
        float radius = minEdge / 2f;
        canvas.drawRoundRect(rect, radius, radius, paint);
        BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        paint.setShader(shader);
        float centerX = (width - minEdge) / 2f;
        float centerY = (height - minEdge) / 2f;
        canvas.drawCircle(minEdge / 2f, minEdge / 2f, minEdge / 2f, paint);
        return output;
    }
}