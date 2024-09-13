package com.example.holymoly;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.LayoutInflater;
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

import com.bumptech.glide.Glide;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MakeStoryAloneActivity extends AppCompatActivity {
    private Gemini gemini;
    private SpeechRecognizer mRecognizer;
    private Typeface typeface;
    private StringBuilder recognizedText = new StringBuilder();

    private ImageView Mic, alertIc, scriptBg, touch;
    private ImageButton before, next, stop, again, create, micStop;
    private TextView howabout, alertTxt, scriptTxt, pageNumber;
    private RadioButton bookmark_AI, bookmark_Mic, bookmark_OK, bookmark_write;
    private EditText story_txt;
    private FlexboxLayout keywordsLayout;

    private final int PERMISSION = 1;
    private ArrayList<String> selectedKeywords = new ArrayList<>();
    
    private SharedPreferences pref;
    private boolean isSoundOn;

    /* firebase 초기화 */
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference backgroundRef = storage.getReference().child("background/개인");
    private StorageReference storiesRef = storage.getReference().child("stories");
    String uid = user.getUid();

    private long backPressedTime = 0;
    private int num = 1;
    private String title = "none";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_make_story_alone);
        pref = getSharedPreferences("music", MODE_PRIVATE); // 효과음 초기화

        gemini = new Gemini();

        initializeUI();
        initializePermissions();

        loadImage(num); // 첫 이미지 로드
        loadTxt(num); // 첫 텍스트 로드

        touch.setOnClickListener(v -> {
            sound();
            Intent intent = new Intent(this, DrawStoryActivity.class);
            intent.putExtra("index", num);
            startActivity(intent);
        });

        // 쓰기 모드
        bookmark_write.setOnClickListener(view -> {
            sound();
            setButtonVisibility(View.VISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
            scriptTxt.setVisibility(View.INVISIBLE); // AI가 생성한 text 숨김
        });

        // AI랑 쓰기 모드
        bookmark_AI.setOnClickListener(view -> {
            sound();
            setButtonVisibility(View.INVISIBLE, View.VISIBLE, View.VISIBLE, View.VISIBLE, View.INVISIBLE);
            scriptTxt.setVisibility(View.VISIBLE); // AI가 생성한 text 보이기
            story_txt.setVisibility(View.VISIBLE); // 동화 작성 text 보이기

            if (keywordsLayout.getChildCount() == 0) {
                requestKeywordsFromGemini();
            }
        });

        // 음성으로 쓰기 모드
        bookmark_Mic.setOnClickListener(view -> {
            sound();
            setButtonVisibility(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.VISIBLE);
            scriptTxt.setVisibility(View.INVISIBLE); // AI가 생성한 text 숨김
            story_txt.setVisibility(View.VISIBLE);   // 동화 작성 text 보이기
        });

        // 완성 후 저장 모드
        bookmark_OK.setOnClickListener(view -> {
            sound();
            scriptTxt.setVisibility(View.INVISIBLE); // AI가 생성한 text 숨김
            story_txt.setVisibility(View.INVISIBLE); // 동화 작성 text 보이기

            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_title, null);
            EditText editTitle = dialogView.findViewById(R.id.title);

            // 다이얼로그 생성
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("제목을 입력하세요")
                    .setView(dialogView)
                    .setPositiveButton("확인", (dialog, which) -> {
                        String title = editTitle.getText().toString().trim();
                        if (title.isEmpty()) {
                            Toast.makeText(this, "제목을 입력하세요.", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            mergeAndDelete(title); // 파일 병합
                            updateImageName(title); // 이미지 파일명 수정
                            Intent intent = new Intent(this, MakeBookcoverActivity.class);
                            intent.putExtra("from", "개인");
                            intent.putExtra("title", title);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .setNegativeButton("취소", (dialog, which) -> dialog.dismiss());

            // 다이얼로그 표시
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        // 키워드 가지고 AI가 동화 생성하기 버튼
        create.setOnClickListener(view -> {
            sound();
            if (!selectedKeywords.isEmpty()) {
                requestStoryFromGemini(selectedKeywords);
            } else {
                Toast.makeText(MakeStoryAloneActivity.this, "키워드를 선택해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        // 음성 인식 시작 버튼
        Mic.setOnClickListener(v -> {
            sound();
            startVoiceRecognition();
        });

//        // 음성 인식 멈춤 버튼
//        micStop.setOnClickListener(v -> {
//            sound();
//            stopVoiceRecognition();
//        });

        // 이전 장 버튼
        before.setOnClickListener(v -> {
            sound();
            if(num == 1) Toast.makeText(this, "첫번째 장입니다.", Toast.LENGTH_SHORT).show();
            else {
                saveTxt(num);
                num--;
                pageNumber.setText(String.valueOf(num)); // 페이지 번호 감소
                loadImage(num);
                loadTxt(num);
            }
        });

        // 이후 장 버튼
        next.setOnClickListener(v -> {
            sound();
            saveTxt(num);
            num++;
            pageNumber.setText(String.valueOf(num)); // 페이지 번호 증가
            loadImage(num);
            loadTxt(num);
            story_txt.setText("");
        });

        // 종료 버튼
        stop.setOnClickListener(v -> {
            if (System.currentTimeMillis() - backPressedTime >= 2000) {
                backPressedTime = System.currentTimeMillis();
                Toast.makeText(this, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
            } else {
                finish(); // 2초 이내에 다시 누르면 종료
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // DrawStoryAlone 액티비티 종료 후 재실행
        loadImage(num);
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
        pageNumber = findViewById(R.id.pageNumber);
        keywordsLayout = findViewById(R.id.fl_keywords);
        touch = findViewById(R.id.iv_touch);
    }

    // 음성 퍼미션
    private void initializePermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.INTERNET,
                    Manifest.permission.RECORD_AUDIO}, PERMISSION);
        }
    }

    // 모드마다 아이콘 VISIBLE 설정
    private void setButtonVisibility(int writeVisibility, int aiVisibility, int createVisibility, int keywordsVisibility, int micVisibility) {
        // Write 모드
        story_txt.setVisibility(writeVisibility);
        touch.setVisibility(writeVisibility);
        // AI 모드
        howabout.setVisibility(aiVisibility);
        alertIc.setVisibility(aiVisibility);
        alertTxt.setVisibility(aiVisibility);
        scriptBg.setVisibility(aiVisibility);
        again.setVisibility(aiVisibility);
        create.setVisibility(createVisibility);
        keywordsLayout.setVisibility(keywordsVisibility);
        // Mic 모드
        Mic.setVisibility(micVisibility);
        micStop.setVisibility(micVisibility);
    }

    // 음성 인식 시작
    private void startVoiceRecognition() {
        // 음성 인식 시작 시 이전 음성 인식 텍스트 초기화
        recognizedText.setLength(0); // StringBuilder 초기화 (기존 음성 인식 내용 삭제)

        if (mRecognizer != null) {
            mRecognizer.destroy(); // 이전 인스턴스가 있을 경우 해제
            mRecognizer = null; // 참조 초기화
        }

        mRecognizer = SpeechRecognizer.createSpeechRecognizer(this); // 새 인스턴스 생성
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
                    // 이전에 저장된 텍스트를 지우지 않고 음성 인식된 텍스트만 추가
                    story_txt.append(recognizedText.toString());
                }
            }
        });

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

        mRecognizer.startListening(intent); // 음성 인식 시작
    }


    // 음성 인식 멈춤
    private void stopVoiceRecognition() {
        if (mRecognizer != null) {
            mRecognizer.stopListening(); // 음성 인식 중지
            mRecognizer.destroy(); // 리소스 해제
            mRecognizer = null; // 인스턴스 초기화
            story_txt.setText(recognizedText.toString()); // 인식된 텍스트 화면에 표시
        }
    }


    // 음성 인식 오류(신경 안 써도 됨)
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

    // AI한테 키워드 뽑아달라고 요청
    private void requestKeywordsFromGemini() {
        String prompt = "동화 주제에 대한 키워드 6개를 생성해주세요. 단답형으로 작성해주세요. 단어와 단어 사이에 ','로 연결해주세요.'";
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

    // 키워드 체크박스 생성
    private void displayKeywords(String keywordText) {
        keywordsLayout.removeAllViews();
        selectedKeywords.clear();

        String[] keywords = keywordText.split(",");

        // 각 키워드마다 커스텀 체크박스를 생성하여 레이아웃에 추가합니다.
        for (String keyword : keywords) {
            createKeywordTextView(keyword.trim());
        }
    }

    // 키워드 배치
    private void createKeywordTextView(String keyword) {
        // item_checkbox.xml 레이아웃을 인플레이트하여 View로 가져옵니다.
        View keywordView = getLayoutInflater().inflate(R.layout.item_checkbox, null);

        // 인플레이트된 View에서 TextView를 찾고 키워드를 설정합니다.
        TextView keywordTextView = keywordView.findViewById(R.id.messageTextView);
        keywordTextView.setText(keyword);

        // 클릭 리스너를 설정하여 체크 여부를 확인하고 선택된 키워드 리스트를 업데이트합니다.
        keywordView.setOnClickListener(v -> {
            if (selectedKeywords.contains(keyword)) {
                selectedKeywords.remove(keyword);
                keywordTextView.setTextColor(Color.parseColor("#9F9F9F")); // 체크 해제된 경우 회색
                keywordTextView.setBackgroundResource(R.drawable.checkbox_background); // 배경을 변경
            } else {
                selectedKeywords.add(keyword);
                keywordTextView.setTextColor(Color.parseColor("#639699")); // 체크된 경우 검정색
                keywordTextView.setBackgroundResource(R.drawable.checkbox_background2); // 배경을 변경

            }
        });

        // FlexboxLayout에 추가합니다.
        keywordsLayout.addView(keywordView);
    }

    // 키워드 토대로 짧은 이야기 생성 요청
    private void requestStoryFromGemini(ArrayList<String> selectedKeywords) {
        String prompt = String.join(", ", selectedKeywords) + "키위드를 가지고 동화 도입부 한 줄을 생성해주세요.";
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

    // 매 페이지마다 글 저장
    private void saveTxt(int index) {
        String fileName = uid + "_개인_" + title + "_" + index + ".txt";
        String currentText = story_txt.getText().toString(); // 현재 텍스트 갖고 옴

        // 텍스트가 비어 있으면 저장하지 않음
        if (currentText.isEmpty()) return;

        // 저장할 텍스트에 페이지 번호 추가
        String finalText = "페이지 " + index + "\n" + currentText;

        // 기존 파일 내용 읽기
        StringBuilder fileContent = new StringBuilder();
        try {
            FileInputStream fis = openFileInput(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                fileContent.append(line).append("\n");
            }
            fis.close();
        } catch (IOException e) { }

        // 기존 내용과 비교해 동일하면 저장하지 않음
        if (fileContent.toString().trim().equals(finalText.trim())) {
            return;
        }

        // 파일 저장
        try {
            FileOutputStream fos = openFileOutput(fileName, MODE_PRIVATE);
            fos.write(finalText.getBytes("UTF-8"));  // 한글 인코딩을 위해 UTF-8 사용
            fos.close();
            uploadFile(fileName);  // 새로운 문장이 추가되면 저장한 후 업로드
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Firebase Storage에 텍스트 파일 업로드
    private void uploadFile(String fileName) {
        // 참조 경로
        StorageReference fileRef = storiesRef.child(fileName);

        // 저장된 파일 읽기
        try {
            FileInputStream fis = openFileInput(fileName);
            byte[] data = new byte[fis.available()];
            fis.read(data);
            fis.close();

            // 파일 메타데이터 생성 (MIME 타입 설정)
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("text/plain")
                    .build();

            // 텍스트 파일 업로드
            UploadTask uploadTask = fileRef.putBytes(data, metadata);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                Toast.makeText(this, "업로드 성공", Toast.LENGTH_SHORT).show();
            });
        } catch (IOException e) { }
    }

    // 텍스트 파일 로드
    private void loadTxt(int index) {
        String fileName = uid + "_개인_" + title + "_" + index + ".txt";
        story_txt.setText(""); // 텍스트뷰 초기화

        StorageReference fileRef = storiesRef.child(fileName);
        fileRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
            String text = new String(bytes, StandardCharsets.UTF_8);  // 파일을 UTF-8로 읽어옴

            // 페이지 n 제외한 나머지 내용만 보여줌
            String prefix = "페이지 " + index + "\n";
            String content = text.substring(prefix.length());

            story_txt.setText(content);  // 텍스트뷰에 로드한 내용을 설정
            story_txt.setSelection(content.length());  // 커서를 텍스트 끝으로 이동
            story_txt.scrollTo(0, 0);  // 스크롤을 맨 위로 초기화
        });
    }

    // 매 페이지마다 이미지 가져오기
    private void loadImage(int index) {
        Glide.with(this).clear(touch); // 기존에 생성된 이미지 초기화

        backgroundRef.listAll().addOnSuccessListener(listResult -> {
            List<StorageReference> items = listResult.getItems();
            boolean imageFound = false; // 이미지가 있는지 확인

            for (StorageReference item : items) {
                String img = item.getName();

                // 저장된 파일 명 : uid_개인_제목_번호.png
                if (img.startsWith(uid + "_" + title + "_" + index)) {
                    item.getDownloadUrl().addOnSuccessListener(uri -> {
                        Glide.with(this).load(uri).into(touch);
                    });
                    imageFound = true; // 이미지가 있으면 true로 설정
                    break;
                }
            }

            if (!imageFound) { // 이미지가 없으면
                touch.setImageResource(R.drawable.ic_touch2); // 기본 이미지 설정
            }
        });
    }

    private void updateImageName(String newTitle) {
        backgroundRef.listAll().addOnSuccessListener(listResult -> {
            List<StorageReference> items = listResult.getItems();
            List<Task<Void>> tasks = new ArrayList<>(); // 삭제할 파일 리스트

            for (StorageReference item : items) {
                String img = item.getName();

                // 저장된 파일 명 : uid_제목_번호.png
                if (img.startsWith(uid + "_" + title)) {
                    String[] parts = img.split("_");
                    String num = parts[2]; // 번호 추출

                    // 저장될 새 파일 명 : uid_새 제목_번호.png
                    String newFileName = uid + "_" + newTitle + "_" + num;
                    StorageReference newFileRef = backgroundRef.child(newFileName);

                    item.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
                        UploadTask uploadTask = newFileRef.putBytes(bytes);
                        uploadTask.addOnSuccessListener(taskSnapshot -> {
                            // 업로드가 성공하면 기존 파일들 삭제
                            tasks.add(item.delete());
                        });
                    });
                }
            }

            // 모든 파일들이 삭제되면 메세지 띄움
            if (!tasks.isEmpty()) {
                Tasks.whenAll(tasks).addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "파일 이름 변경 및 삭제 성공", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // 병합된 파일 저장 및 기존 파일들 삭제
    private void mergeAndDelete(String title) {
        storiesRef.listAll().addOnSuccessListener(listResult -> {
            StringBuilder mergedContent = new StringBuilder();

            // 기존에 uid_개인_index로 이루어진 파일이 있는지 확인
            int index = 1;
            for (StorageReference item : listResult.getItems()) {
                String file = item.getName();
                String[] parts = file.split("_");

                if (file.startsWith(uid + "_개인") && isNumeric(parts[2])) {
                    index++;
                }
            }

            // 파일을 저장하는 경로 생성
            String mergedFileName = uid + "_개인_" + index + "_" + title + ".txt";
            StorageReference mergedFileRef = storiesRef.child(mergedFileName);

            // 모든 파일을 읽고 병합
            List<Task<byte[]>> tasks = new ArrayList<>();
            for (StorageReference item : listResult.getItems()) {
                String fileName = item.getName();
                if (fileName.startsWith(uid + "_개인_none")) {
                    tasks.add(item.getBytes(Long.MAX_VALUE));
                }
            }

            // 파일 읽기 완료 후 병합
            Tasks.whenAllSuccess(tasks).addOnSuccessListener(results -> {
                for (Object result : results) {
                    String content = new String((byte[]) result);
                    mergedContent.append(content).append("\n");  // 파일 내용을 병합
                }

                // 병합된 파일을 Storage에 업로드
                byte[] mergedFileData = mergedContent.toString().getBytes();

                // 메타 데이터 설정
                StorageMetadata metadata = new StorageMetadata.Builder()
                        .setContentType("text/plain")
                        .build();

                UploadTask uploadTask = mergedFileRef.putBytes(mergedFileData, metadata);

                uploadTask.addOnSuccessListener(taskSnapshot -> {
                    Toast.makeText(this, "파일 병합 성공", Toast.LENGTH_SHORT).show();
                    deleteFiles();  // 병합 후 기존 파일들 삭제
                });
            });
        });
    }

    // 기존 파일 삭제
    private void deleteFiles() {
        storiesRef.listAll().addOnSuccessListener(listResult -> {
            List<Task<Void>> tasks = new ArrayList<>();

            // 파일 이름에 "none"이 포함된 파일들 삭제
            for (StorageReference item : listResult.getItems()) {
                String fileName = item.getName();

                // 삭제할 파일들을 task 리스트에 추가
                if (fileName.startsWith(uid) && fileName.contains("none")) {
                    tasks.add(item.delete());
                }
            }

            // 모든 파일들이 삭제되면 메세지 띄움
            if (!tasks.isEmpty()) {
                Tasks.whenAll(tasks).addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "파일 삭제 성공", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // 문자열이 숫자인지 확인하는 메소드
    private boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // 효과음
    public void sound() {
        isSoundOn = pref.getBoolean("on&off2", true);
        Intent intent = new Intent(this, SoundService.class);
        if (isSoundOn) startService(intent); // 효과음 on
        else stopService(intent);            // 효과음 off
    }
}
