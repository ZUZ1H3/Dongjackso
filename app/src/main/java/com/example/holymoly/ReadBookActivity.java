package com.example.holymoly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.polly.AmazonPolly;
import com.amazonaws.services.polly.AmazonPollyPresigningClient;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechRequest;
import com.amazonaws.services.polly.model.SynthesizeSpeechResult;
import com.amazonaws.services.polly.model.VoiceId;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.InputStream;
import java.util.ArrayList;

public class ReadBookActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView storyTextView, pageTextView;
    private ImageButton stopReadingBtn, backBtn, nextBtn, play, resume;
    private ImageView backgroundImageView, background;
    private int currentPage = 1;
    private long backPressedTime = 0;
    private String imgName;
    // 페이지 내용과 선택지를 추적하기 위한 변수
    private ArrayList<String> pageContents = new ArrayList<>();

    /* firebase 초기화 */
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();
    private String uid = user.getUid();

    /* 텍스트 표시 변수 */
    private Handler textHandler;
    private Runnable textRunnable;
    private int index = 0;
    private static final long DELAY = 60; // 텍스트 간격

    /* Amazon Polly 초기화 */
    private static final String COGNITO_POOL_ID = BuildConfig.POLLY_API_KEY;
    private Regions MY_REGION = Regions.AP_NORTHEAST_2; // 리전 설정
    private AmazonPolly pollyClient;
    private AudioTrack audioTrack;      // audioTrack 변수
    private InputStream audioStream;    // 음성 스트림 저장

    /* 효과음 */
    private SharedPreferences pref;
    private boolean isSoundOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_readbook);
        pref = getSharedPreferences("music", MODE_PRIVATE); // 효과음 초기화

        // UI 요소 초기화
        storyTextView = findViewById(R.id.tv_pageText);
        pageTextView = findViewById(R.id.tv_page);
        backgroundImageView = findViewById(R.id.background_image_view);
        background = findViewById(R.id.background);
        stopReadingBtn = findViewById(R.id.ib_stopReading);
        backBtn = findViewById(R.id.ib_backStep);
        nextBtn = findViewById(R.id.ib_nextStep);
        play = findViewById(R.id.ib_play);
        resume = findViewById(R.id.ib_resume);
        storyTextView.setMovementMethod(new ScrollingMovementMethod());

        backBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        stopReadingBtn.setOnClickListener(this);
        play.setOnClickListener(this);
        resume.setOnClickListener(this);
        resume.setVisibility(View.INVISIBLE); // 버튼 숨기기

        initPolly(); // Amazon Polly 초기화
        // 이전 액티비티에서 imgName 가져오기
        imgName = getIntent().getStringExtra("imgName");

        // 테마 추출
        String[] parts = imgName.split("_");
        String theme = parts[1];  // imgName에서 테마 추출
        Log.d("Theme", "추출된 테마: " + theme);

        // 테마에 따라 배경음악 재생
        playBackgroundMusic(theme);

        // 이미지 및 텍스트 로드
        if(imgName.contains("개인")) {
            backgroundImageView.setVisibility(View.INVISIBLE);
            background.setVisibility(View.VISIBLE);
            loadImage(imgName, currentPage);
            loadText(imgName);
        }
        else {
            backgroundImageView.setVisibility(View.VISIBLE);
            background.setVisibility(View.INVISIBLE);
            loadImage(imgName);
            loadText(imgName);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textHandler != null) {
            textHandler.removeCallbacks(textRunnable);
        }
        stopSpeech();
    }

    @Override
    public void onClick(View v) {
        sound();
        if(v.getId() == R.id.ib_backStep) {
            backPage();     // 이전 페이지로
            stopSpeech();
        }
        else if(v.getId() == R.id.ib_nextStep) {
            nextPage();     // 다음 페이지로
            stopSpeech();
        }
        else if(v.getId() == R.id.ib_stopReading) { // 그만 읽기
            if (System.currentTimeMillis() - backPressedTime >= 2000) {
                backPressedTime = System.currentTimeMillis();
                Toast.makeText(this, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
            } else {
                Intent musicIntent = new Intent(this, MusicService.class);
                musicIntent.setAction("CHANGE_MUSIC");
                musicIntent.putExtra("MUSIC_RES_ID", R.raw.ocean_theme_music);
                startService(musicIntent); // 배경음악 기본으로 변경
                finish(); // 액티비티 종료
            }
        }
        else if(v.getId() == R.id.ib_play) {
            readCurrentPage(); // 현재 페이지 읽기
            stopService(new Intent(this, MusicService.class));
            play.setVisibility(View.INVISIBLE); // play 버튼 숨기기
            resume.setVisibility(View.VISIBLE); // resume 버튼 보이기
        }
        else if(v.getId() == R.id.ib_resume) {
            stopSpeech();
            play.setVisibility(View.VISIBLE);     // play 버튼 보이기
            resume.setVisibility(View.INVISIBLE); // resume 버튼 숨기기
        }
    }
    // AI와 만들 때의 이미지 로드
    private void loadImage(String imgName) {
        String[] parts = imgName.split("_");
        String uid = parts[0];
        String theme = parts[1];
        String index = parts[2];
        String uidIndex = uid + "_" + index + ".png";

        StorageReference imgRef = storageRef.child("background/" + theme + "/" + uidIndex);
        // 이미지 로드
        imgRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(this).load(uri).into(backgroundImageView);
        });
    }

    // 혼자 만들기일 때의 이미지 로드
    private void loadImage(String imgName, int num) {
        String[] parts = imgName.split("_");
        String uid = parts[0];
        String theme = parts[1];
        String index = parts[2];
        String title = parts[3].replace(".png", "");

        String fileName = uid + "_" + index + "_" + title + "_" + num + ".png";

        StorageReference aloneRef = storageRef.child("background/" + theme + "/" + fileName);

        // 이미지 로드
        aloneRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(this).load(uri).into(background);
        });
    }

    // 이전 페이지로 이동
    private void backPage() {
        if (currentPage > 1) {
            currentPage--;
            loadImage(imgName, currentPage);
            showPage(currentPage); // 페이지 내용 업데이트
        } else {
            Toast.makeText(this, "첫 번째 페이지입니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 다음 페이지로 이동
    private void nextPage() {
        if (currentPage < pageContents.size()) {
            currentPage++;
            loadImage(imgName, currentPage);
            showPage(currentPage); // 페이지 내용 업데이트
        } else {
            Toast.makeText(this, "마지막 페이지입니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // Storage에서 텍스트 로드
    private void loadText(String imgName) {
        if(imgName.contains("개인")) {
            String uidThemeIndex = aloneFiles(imgName);
            // Storage에서 txt 파일 목록 가져오기
            StorageReference txtRef = storageRef.child("stories/" + uidThemeIndex);

            txtRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
                String textContent = new String(bytes);
                bookTextContent(textContent);
                showPage(currentPage);
            });
        }
        else {
            String uidThemeIndex = compareFiles(imgName);
            // Storage에서 txt 파일 목록 가져오기
            StorageReference txtRef = storageRef.child("stories/" + uidThemeIndex);
            txtRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
                String textContent = new String(bytes);
                bookTextContent(textContent);
                showPage(currentPage);
            });
        }
    }

    // AI와 함께 imgName으로 파일명을 txt 파일로 변환
    private String compareFiles(String imgName) {
        String[] parts = imgName.split("_");
        String uid = parts[0];
        String theme = parts[1];
        String index = parts[2];

        return uid + "_" + theme + "_" + index + ".txt";
    }

    // 개인 imgName으로 파일명을 txt 파일로 변환
    private String aloneFiles(String imgName) {
        String[] parts = imgName.split("_");
        String uid = parts[0];
        String theme = parts[1];
        String index = parts[2];
        String title = parts[3].replace(".png", ".txt");

        return uid + "_" + theme + "_" + index + "_" + title;
    }

    // 전체 텍스트를 페이지 + 숫자 별로 분리
    private void bookTextContent(String textContent) {
        String[] pages = textContent.split("(?=페이지 \\d+)");
        for (String page : pages) {
            // "페이지"와 숫자 부분을 제거하고, 나머지 텍스트만 추가
            String cleanedPage = page.replaceAll("페이지 \\d+", "").trim();
            if (!cleanedPage.isEmpty()) pageContents.add(cleanedPage); // 빈 페이지 제거
        }
    }

    // 페이지 보여주기
    private void showPage(int num) {
        if (num > 0 && num <= pageContents.size()) {
            String pageText = pageContents.get(num - 1);
            storyTextView.setText(""); // 텍스트뷰를 초기화
            index = 0; // 문자 인덱스 초기화

            // 핸들러 및 Runnable 초기화
            if (textHandler != null) {
                textHandler.removeCallbacksAndMessages(null); // 이전 작업 취소
            }
            textHandler = new Handler();
            textRunnable = new Runnable() {
                @Override
                public void run() {
                    if (index < pageText.length()) {
                        storyTextView.append(String.valueOf(pageText.charAt(index)));
                        index++;
                        textHandler.postDelayed(this, DELAY);
                    }
                }
            };
            textHandler.post(textRunnable);

            pageTextView.setText(currentPage + " / " + pageContents.size());
            storyTextView.scrollTo(0, 0); // 스크롤뷰를 맨 위로 초기화

            // 클릭 리스너 설정
            storyTextView.setOnClickListener(v -> {
                // 핸들러의 모든 작업 취소하고 전체 텍스트 표시
                if (textHandler != null) {
                    textHandler.removeCallbacksAndMessages(null);
                }
                storyTextView.setText(pageText); // 전체 텍스트 표시
            });
        }
    }

    // 현재 페이지 읽기
    private void readCurrentPage() {
        String read = pageContents.get(currentPage - 1);
        synthesizeSpeech(read);
    }

    // 아마존 폴리 초기화
    private void initPolly() {
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                COGNITO_POOL_ID,
                MY_REGION
        );
        pollyClient = new AmazonPollyPresigningClient(credentialsProvider);
    }

    // 텍스트를 음성으로 변환
    public void synthesizeSpeech(String text) {
        SynthesizeSpeechRequest synthReq = new SynthesizeSpeechRequest()
                .withText(text)
                .withTextType("text")
                .withVoiceId(VoiceId.Seoyeon)
                .withOutputFormat(OutputFormat.Pcm);

        new Thread(() -> {
            try {
                SynthesizeSpeechResult synthRes = pollyClient.synthesizeSpeech(synthReq);
                audioStream = synthRes.getAudioStream(); // audioStream을 저장
                playAudioStream(audioStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // 음성 스트림 재생
    private void playAudioStream(InputStream stream) throws Exception {
        int sampleRate = 16000;
        int bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
        );

        audioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize,
                AudioTrack.MODE_STREAM
        );

        audioTrack.play();

        byte[] buffer = new byte[bufferSize];
        int readBytes;

        while ((readBytes = stream.read(buffer)) != -1) {
            audioTrack.write(buffer, 0, readBytes);
        }

        // 음성 스트림이 끝났을 때 음성 중단
        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
            audioTrack = null;
        }

        // 음성 스트림이 끝났을 때 UI 업데이트
        runOnUiThread(() -> {
            play.setVisibility(View.VISIBLE);     // play 버튼 보이기
            resume.setVisibility(View.INVISIBLE); // resume 버튼 숨기기
        });
        stream.close();
    }

    // 음성 중단
    public void stopSpeech() {
        play.setVisibility(View.VISIBLE);     // play 버튼 보이기
        resume.setVisibility(View.INVISIBLE); // resume 버튼 숨기기

        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
            audioTrack = null;
        }
    }

    private void playBackgroundMusic(String theme) {
        // 테마에 따라 음악 리소스 ID 설정
        int musicResId = R.raw.ocean_theme_music; // 기본값

        switch (theme.toLowerCase()) {
            case "숲":
                musicResId = R.raw.forest_theme_music;
                break;
            case "바다":
                musicResId = R.raw.ocean_theme_music;
                break;
            case "사막":
                musicResId = R.raw.desert_theme_music;
                break;
            case "우주":
                musicResId = R.raw.space_theme_music;
                break;
            case "궁전":
                musicResId = R.raw.castle_theme_music;
                break;
            case "마을":
                musicResId = R.raw.village_theme_music;
                break;
            default:
                musicResId = R.raw.ocean_theme_music;
                break;
        }

        // MusicService에 음악 변경 요청
        Intent intent = new Intent(this, MusicService.class);
        intent.setAction("CHANGE_MUSIC");
        intent.putExtra("MUSIC_RES_ID", musicResId);
        startService(intent);
    }

    // 효과음
    public void sound() {
        isSoundOn = pref.getBoolean("on&off2", true);
        Intent intent = new Intent(this, SoundService.class);
        if (isSoundOn) startService(intent); // 효과음 on
        else stopService(intent);            // 효과음 off
    }
}