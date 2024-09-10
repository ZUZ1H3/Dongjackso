package com.example.holymoly;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;

public class MakeStoryAloneActivity extends AppCompatActivity {
    private Gemini gemini;
    private SpeechRecognizer mRecognizer;
    private Typeface typeface;
    private StringBuilder recognizedText = new StringBuilder();

    private ImageView Mic, alertIc, scriptBg, touch;
    private ImageButton before, next, stop, again, create, micStop;
    private TextView howabout, alertTxt, scriptTxt, voiceTxt;
    private RadioButton bookmark_AI, bookmark_Mic, bookmark_OK, bookmark_write;
    private EditText story_txt;
    private FlexboxLayout keywordsLayout;

    private final int PERMISSION = 1;
    private ArrayList<String> selectedKeywords = new ArrayList<>();

    /* 효과음 */
    private SharedPreferences pref;
    private boolean isSoundOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_make_story_alone);
        pref = getSharedPreferences("music", MODE_PRIVATE); // 효과음 초기화

        initializeUI();
        initializePermissions();

        bookmark_write.setOnClickListener(view -> {
            sound();
            setButtonVisibility(View.VISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
        });

        bookmark_AI.setOnClickListener(view -> {
            sound();
            setButtonVisibility(View.INVISIBLE, View.VISIBLE, View.VISIBLE, View.VISIBLE, View.INVISIBLE);
            requestKeywordsFromGemini();
        });

        bookmark_Mic.setOnClickListener(view -> {
            sound();
            setButtonVisibility(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.VISIBLE);
        });

        bookmark_OK.setOnClickListener(view -> {
            sound();
            Intent intent = new Intent(MakeStoryAloneActivity.this, HomeActivity.class);
            startActivity(intent);
        });

        create.setOnClickListener(view -> {
            sound();
            if (!selectedKeywords.isEmpty()) {
                requestStoryFromGemini(selectedKeywords);
            } else {
                Toast.makeText(MakeStoryAloneActivity.this, "키워드를 선택해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        Mic.setOnClickListener(v -> {
            sound();
            startVoiceRecognition();
        });

        micStop.setOnClickListener(v -> {
            sound();
            stopVoiceRecognition();
        });

        before.setOnClickListener(v -> {
            sound();
            
        });

        next.setOnClickListener(v -> {
            sound();
        });
    }

    private void initializeUI() {
        bookmark_write = findViewById(R.id.rb_bookmark_write);
        bookmark_AI = findViewById(R.id.rb_bookmark_ai);
        bookmark_Mic = findViewById(R.id.rb_bookmark_mic);
        bookmark_OK = findViewById(R.id.rb_bookmark_ok);
        Mic = findViewById(R.id.iv_alone_mic);
        before = findViewById(R.id.ib_before);
        next = findViewById(R.id.ib_next);
        stop = findViewById(R.id.ib_stop);
        again = findViewById(R.id.ib_again);
        create = findViewById(R.id.ib_create);
        story_txt = findViewById(R.id.et_story_txt);
        howabout = findViewById(R.id.tv_howabout);
        alertIc = findViewById(R.id.iv_alert);
        alertTxt = findViewById(R.id.tv_alert);
        scriptBg = findViewById(R.id.iv_create_back);
        scriptTxt = findViewById(R.id.tv_create_txt);
        keywordsLayout = findViewById(R.id.fl_keywords);
        touch = findViewById(R.id.iv_touch);
        voiceTxt = findViewById(R.id.tv_voice_script);
        micStop = findViewById(R.id.ib_mic_stop);
    }

    private void initializePermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.INTERNET,
                    Manifest.permission.RECORD_AUDIO}, PERMISSION);
        }
    }

    private void setButtonVisibility(int writeVisibility, int aiVisibility, int createVisibility, int keywordsVisibility, int micVisibility) {
        // Write 모드
        story_txt.setVisibility(writeVisibility);
        touch.setVisibility(writeVisibility);
        // AI 모드
        howabout.setVisibility(aiVisibility);
        alertIc.setVisibility(aiVisibility);
        alertTxt.setVisibility(aiVisibility);
        again.setVisibility(aiVisibility);
        create.setVisibility(createVisibility);
        keywordsLayout.setVisibility(keywordsVisibility);
        // Mic 모드
        Mic.setVisibility(micVisibility);
        voiceTxt.setVisibility(micVisibility);
        micStop.setVisibility(micVisibility);
    }

    private void startVoiceRecognition() {
        mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Toast.makeText(getApplicationContext(), "음성인식을 시작합니다.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBeginningOfSpeech() { }
            @Override
            public void onRmsChanged(float rmsdB) { }
            @Override
            public void onBufferReceived(byte[] buffer) { }
            @Override
            public void onEndOfSpeech() { }
            @Override
            public void onPartialResults(Bundle partialResults) { }
            @Override
            public void onEvent(int eventType, Bundle params) { }

            @Override
            public void onError(int error) {
                String message = "에러 발생: " + getErrorText(error);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null) {
                    for (String match : matches) {
                        recognizedText.append(match).append(" ");
                    }
                    voiceTxt.setText(recognizedText.toString());
                }
            }
        });
        mRecognizer.startListening(new Intent());
    }

    private void stopVoiceRecognition() {
        if (mRecognizer != null) {
            mRecognizer.stopListening();
            mRecognizer.destroy();
            mRecognizer = null;
            voiceTxt.setText(recognizedText.toString());
        }
    }

    private String getErrorText(int errorCode) {
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO: return "오디오 에러";
            case SpeechRecognizer.ERROR_CLIENT: return "클라이언트 에러";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: return "퍼미션 없음";
            case SpeechRecognizer.ERROR_NETWORK: return "네트워크 에러";
            case SpeechRecognizer.ERROR_NO_MATCH: return "일치하는 결과 없음";
            default: return "알 수 없는 에러";
        }
    }

    private void requestKeywordsFromGemini() {
        String prompt = "Generate 6 creative and engaging keywords for a children's story theme. Provide them as a comma-separated list.";
        gemini.generateText(prompt, new Gemini.Callback() {
            @Override
            public void onSuccess(String text) {
                runOnUiThread(() -> displayKeywords(text));
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() -> Toast.makeText(MakeStoryAloneActivity.this, "키워드 생성 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void displayKeywords(String keywordText) {
        keywordsLayout.removeAllViews();
        selectedKeywords.clear();

        String[] keywords = keywordText.split(",");
        for (String keyword : keywords) {
            TextView keywordView = createKeywordTextView(keyword.trim());
            keywordsLayout.addView(keywordView);
        }
    }

    private TextView createKeywordTextView(String keyword) {
        TextView keywordView = new TextView(this);
        keywordView.setText(keyword);
        keywordView.setPadding(5, 5, 5, 5);
        keywordView.setTextColor(Color.parseColor("#9F9F9F"));
        keywordView.setTextSize(16);
        keywordView.setTypeface(ResourcesCompat.getFont(this, R.font.ssurround));

        keywordView.setOnClickListener(v -> {
            if (selectedKeywords.contains(keyword)) {
                selectedKeywords.remove(keyword);
                keywordView.setTextColor(Color.parseColor("#9F9F9F"));
            } else {
                selectedKeywords.add(keyword);
                keywordView.setTextColor(Color.parseColor("#639699"));
            }
        });
        return keywordView;
    }

    private void requestStoryFromGemini(ArrayList<String> selectedKeywords) {
        String prompt = "Using these keywords: " + String.join(", ", selectedKeywords) + ", write a creative 3-sentence story for children.";
        gemini.generateText(prompt, new Gemini.Callback() {
            @Override
            public void onSuccess(String text) {
                runOnUiThread(() -> {
                    scriptTxt.setVisibility(View.VISIBLE);
                    scriptTxt.setText(text);
                });
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() -> Toast.makeText(MakeStoryAloneActivity.this, "Failed to generate story: " + t.getMessage(), Toast.LENGTH_SHORT).show());
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
}
