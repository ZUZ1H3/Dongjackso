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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
public class SelectcharacterActivity extends AppCompatActivity implements UserInfoLoader{
    private ImageButton btnhome, btntrophy, btnsetting, btnnext;
    private View[] customCheckBoxes = new View[10]; // 캐릭터를 저장할 체크박스 배열
    private String[] character = new String[10]; // 캐릭터 이름을 저장할 배열
    private boolean[] isChecked = new boolean[10]; // 체크 상태를 저장할 배열
    private TextView name;
    private ImageView profile;

    private UserInfo userInfo = new UserInfo();

    private Karlo karlo;
    private Gemini gemini;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectcharacter);

        // KarloImageGenerator 인스턴스 생성
        karlo = new Karlo();
        gemini = new Gemini();

        // Intent에서 테마를 가져옴
        Intent intent = getIntent();
        String thema = intent.getStringExtra("selectedThema");
        if (thema != null) {
            // 테마에 따른 캐릭터 이름 요청
            requestCharacterNames(thema);
        } else {
            Toast.makeText(this, "테마가 없습니다", Toast.LENGTH_SHORT).show();
        }

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

        btnnext.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                ArrayList<String> selectedCharacters = new ArrayList<>();
                for (int i = 0; i < isChecked.length; i++) {
                    if (isChecked[i]) {
                        selectedCharacters.add(character[i]);
                    }
                }

                if (selectedCharacters.isEmpty()) {
                    Toast.makeText(SelectcharacterActivity.this, "캐릭터를 선택해주세요", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(SelectcharacterActivity.this, Makepage1Activity.class);
                    intent.putStringArrayListExtra("selectedCharacters", selectedCharacters);
                    intent.putExtra("selectedTheme", thema);
                    startActivity(intent);
                }
            }
        });

        LinearLayout hllFirst = findViewById(R.id.hll_first);
        LinearLayout hllSecond = findViewById(R.id.hll_second);
        LayoutInflater inflater = getLayoutInflater();

        // 커스텀 체크박스 초기화
        for (int i = 0; i < 10; i++) {
            customCheckBoxes[i] = inflater.inflate(R.layout.custom_checkbox, null);
            final int index = i; // 인덱스를 final로 설정
            customCheckBoxes[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
    }

    // 선택한 테마에 따라 AI 모델에서 등장인물 후보 요청
    private void requestCharacterNames(String thema) {
        String requestText = thema + "테마를 주제로 동화를 만들려고 합니다. 동화에 어울릴만한 등장인물의 후보가 10개 필요합니다." +
                "1~5글자로 단답형으로 답해주세요. 장소, 날씨가 등장인물이 될 수는 없으니 제외해주세요." +
                "중복은 없어야 하며 비슷한 개념도 제외해주세요. 예를 들어, '용'과 '드래곤'은 같은 개념입니다." +
                "후보와 후보 사이에는 ', '로 띄어주세요. 그러면 저희는 당신이 정해준 후보들 중 몇 개를 선택하여 동화를 만들 것입니다." +
                "당신이 답변 할 형식의 예시는 이렇습니다. '공주, 왕자, 물고기, 나무, 토끼, 거북이, 호랑이, 나비, 엄마, 동생'.";

        gemini.generateText(requestText, new Gemini.Callback() {
            @Override
            public void onSuccess(String resultText) {
                runOnUiThread(() -> {
                    parseCharacterNames(resultText);
                    setCheckBoxNames();
                    for (int i = 0; i < customCheckBoxes.length; i++) {
                        if (character[i] != null) {
                            generateAndSetImage(character[i], customCheckBoxes[i]);
                        }
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() -> Toast.makeText(SelectcharacterActivity.this, "AI 요청 실패: " + t.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    // AI의 응답을 파싱하여 캐릭터 이름 배열에 저장
    private void parseCharacterNames(String resultText) {
        if (resultText != null && !resultText.isEmpty()) {
            String[] names = resultText.split(", ");
            if (names.length == 10) {
                System.arraycopy(names, 0, character, 0, names.length);
            } else {
                Toast.makeText(this, "AI가 10개의 이름을 반환하지 않았습니다. 반환된 이름 수: " + names.length, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "AI 응답이 비어있습니다", Toast.LENGTH_LONG).show();
        }
    }

    // 체크박스에 캐릭터 이름을 설정
    private void setCheckBoxNames() {
        for (int i = 0; i < customCheckBoxes.length; i++) {
            TextView textView = customCheckBoxes[i].findViewById(R.id.checkbox_text);
            textView.setText(character[i]);
        }
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

    // 체크박스 배경 업데이트 메서드
    private void updateCheckBoxBackground(View customCheckBox, boolean isChecked) {
        ImageView imageView = customCheckBox.findViewById(R.id.checkbox_image);
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        if (drawable != null) {
            int alpha = isChecked ? 128 : 255; // 체크되었을 때는 투명도 50%, 체크 해제 시 투명도 100%
            drawable.setAlpha(alpha);
        }
    }
    // Character Name 번역 및 이미지 요청 메서드
    private void generateAndSetImage(String characterName, View customCheckBox) {
        translateCharacterName(characterName, new Gemini.Callback() {
            @Override
            public void onSuccess(String translatedName) {
                if (translatedName != null) {
                    // 프롬프트 작성 및 이미지 요청
                    String prompt = "Dreamy, cute, fairytale, simple, twinkle " + translatedName + " a sky blue background";
                    karlo.requestImage(prompt, new Karlo.Callback() {
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
    private void translateCharacterName(String characterName, Gemini.Callback callback) {
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

    @Override
    public void loadUserInfo(ImageView profile, TextView name) {
        userInfo.loadUserInfo(profile, name);
    }
}
