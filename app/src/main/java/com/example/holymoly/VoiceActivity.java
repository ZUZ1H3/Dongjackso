package com.example.holymoly;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class VoiceActivity extends AppCompatActivity {
    Intent intent;
    Karlo karlo;
    Gemini gemini;
    SpeechRecognizer mRecognizer;
    ImageView micImage;
    TextView scriptView;
    ImageButton nextBtn;
    private String selectedTheme;
    private ArrayList<String> selectedCharacters;

    private String themePath, finalSelectedTheme;

    final int PERMISSION = 1;
    private StringBuilder recognizedText = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice);

        if (Build.VERSION.SDK_INT >= 23) {
            // 퍼미션 체크
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET,
                    Manifest.permission.RECORD_AUDIO}, PERMISSION);
        }

        scriptView = findViewById(R.id.tv_script);
        micImage = findViewById(R.id.iv_mic);
        nextBtn = findViewById(R.id.ib_next);

        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

        // 테마 가져옴
        Intent intent = getIntent();
        karlo = new Karlo(768, 960);
        gemini = new Gemini();
        selectedTheme = intent.getStringExtra("selectedTheme");
        selectedCharacters = intent.getStringArrayListExtra("selectedCharacters");

        // 이미지 누르면 음성 인식 시작
        micImage.setOnClickListener(v -> {
            sound();
            mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            mRecognizer.setRecognitionListener(listener);
            mRecognizer.startListening(intent);
        });

        // 버튼 누르면 음성 인식 끝내기
        nextBtn.setOnClickListener(v -> {
            if (mRecognizer != null) {
                mRecognizer.stopListening(); // 음성 인식 중지
                mRecognizer.destroy(); // 리소스 해제
                mRecognizer = null; // 참조 null로 설정

                // 누적된 음성 인식 결과를 String 형태로 저장
                String finalText = recognizedText.toString();
                scriptView.setText(finalText); // 최종 텍스트 화면에 표시

                // MakeStoryActivity로 인식된 텍스트를 전달
                Intent makeStoryIntent = new Intent(VoiceActivity.this, MakeStoryActivity.class);
                makeStoryIntent.putExtra("recognizedText", finalText);
                startActivity(makeStoryIntent); // MakeStoryActivity 시작
            }
        });
    }

    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
            Toast.makeText(getApplicationContext(), "음성인식을 시작합니다.", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBeginningOfSpeech() {}

        @Override
        public void onRmsChanged(float rmsdB) {}

        @Override
        public void onBufferReceived(byte[] buffer) {}

        @Override
        public void onEndOfSpeech() {}

        @Override
        public void onError(int error) {
            String message;

            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "오디오 에러";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "클라이언트 에러";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "퍼미션 없음";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "네트워크 에러";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "네트웍 타임아웃";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "찾을 수 없음";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "RECOGNIZER가 바쁨";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "서버가 이상함";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "말하는 시간초과";
                    break;
                default:
                    message = "알 수 없는 오류";
                    break;
            }
            Toast.makeText(getApplicationContext(), "에러가 발생하였습니다. : " + message, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onResults(Bundle results) {
            // 말을 하면 ArrayList에 단어를 넣고 textView에 단어를 이어줌
            ArrayList<String> matches =
                    results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            for (int i = 0; i < matches.size(); i++) {
                recognizedText.append(matches.get(i)).append(" ");
            }
            scriptView.setText(recognizedText.toString());
        }

        @Override
        public void onPartialResults(Bundle partialResults) {}

        @Override
        public void onEvent(int eventType, Bundle params) {}
    };
    public void sound() {
        Intent intent = new Intent(this, SoundService.class);
        startService(intent);
    }
}
