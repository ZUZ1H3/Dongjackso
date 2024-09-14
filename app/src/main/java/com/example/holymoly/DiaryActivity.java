package com.example.holymoly;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.*;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.*;
import com.google.ai.client.generativeai.type.*;
import com.google.common.util.concurrent.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.*;
import java.util.concurrent.*;

public class DiaryActivity extends AppCompatActivity {
    private Intent intent;
    private SpeechRecognizer mRecognizer;
    private GenerativeModel model;
    private ChatFutures chat;
    private EditText userInput;
    private ImageView rectangles;
    private ImageButton stopMakingBtn, sendButton, makeDiaryButton, moreButton, miniArrow, miniMic;
    private long backPressedTime = 0;
    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private List<Message> messageList = new ArrayList<>();
    private TextView who, when, where, what, how, why, mood;
    private boolean hasWho = false, hasWhen = false, hasWhere = false, hasWhat = false, hasHow = false, hasWhy = false, hasMood = false;
    private String name = "";
    private Gemini gemini = new Gemini();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();
    private String story = ""; // 사용자가 작성한 메시지를 저장할 변수
    private boolean isConversationEnded = false; // 대화 종료 여부를 체크하는 플래그
    private SharedPreferences pref;
    private boolean isSoundOn;
    final int PERMISSION = 1;
    private StringBuilder recognizedText = new StringBuilder();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);
        pref = getSharedPreferences("music", MODE_PRIVATE); // 효과음 초기화

        initViews();
        setupClickListeners();
        fetchUserName(); // 데이터베이스에서 이름을 가져옴

        if (Build.VERSION.SDK_INT >= 23) {
            // 퍼미션 체크
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.INTERNET,
                    Manifest.permission.RECORD_AUDIO}, PERMISSION);
        }

        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

        // RecyclerView 설정
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);

        //AI 대화 수위 조절
        SafetySetting harassmentSafety = new SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.NONE);
        SafetySetting hateSpeechSafety = new SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.NONE);
        SafetySetting hateDangerousSafety = new SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE);

        // GenerativeModel 초기화 및 이전 채팅 기록 설정
        model = new GenerativeModel("gemini-1.5-flash", "AIzaSyB5Vf0Nk67nJOKk4BADvPDQhRGNyYTVxjU", null,
                Arrays.asList(harassmentSafety, hateSpeechSafety, hateDangerousSafety));

        GenerativeModelFutures modelFutures = GenerativeModelFutures.from(model);

        // 이전 채팅 기록 생성
        Content.Builder userContentBuilder = new Content.Builder();
        userContentBuilder.setRole("user");
        userContentBuilder.addText("당신의 대화 대상은 유치원에서 초등학생 정도의 어린이입니다. 어린이는 자신이 오늘 있었던 이야기를 육하원칙에 맞게 말해야 하며," +
                "당신은 육하원칙에 맞게 말하는 것을 도와주는 교육용 로봇입니다. 다정하고 친절한 반말 말투로 말해주세요." +
                "만약 사용자가 욕을 사용하거나 성적인 말 등 적절하지 않은 말을 사용한다면, 그런 말 쓰지 말라고 따끔하게 혼내주세요." +
                "사용자가 오늘 있었던 일에 대해 이야기한다면 리액션과 함께 그 경험에 대해 구체적으로 대답할 수 있도록 물어봐주세요." +
                "사용자가 '누구와 함께했는지, 언제, 어디서, 무엇을, 어떻게, 왜'에 대한 이야기를 하지 않았다면, 이 중 한가지에 대해 물어봐주세요." +
                "감정도 함께 이야기할 수 있도록 유도해주세요. 답장은 한 번에 한가지만 질문해주세요." +
                "누군가와 함께 했는지, 혼자였는지 여부를 알 수 없다면, 함께한 사람이 있었는지 물어봐주세요." +
                "그러나 질문만 계속 하지는 말고 가끔 주제에 맞는 재밌는 이야기도 하며 수다를 떨어주세요." +
                "'ㅋㅋㅋ', 'ㅎㅎ'와 같은 초성으로 대화하지 마세요. 표준어를 사용하세요." +
                "2~4문장 정도로 대답해주세요.\n" +
                "예시)사용자: 나 오늘 도서관에서 공부했어.\n" +
                "당신: 오늘 도서관에서 공부를 했구나! 힘들었을텐데 정말 대단하다. 도서관에는 언제 갔니? 낮에 간거야?\n");
        Content userContent = userContentBuilder.build();
        Content.Builder modelContentBuilder = new Content.Builder();
        modelContentBuilder.setRole("model");
        modelContentBuilder.addText("안녕, 오늘 어떤 일이 있었어?");
        Content modelContent = modelContentBuilder.build();
        List<Content> history = Arrays.asList(userContent, modelContent);
        chat = modelFutures.startChat(history);

    }

    private void initViews() {
        pref = getSharedPreferences("music", MODE_PRIVATE);
        recyclerView = findViewById(R.id.recyclerView);
        userInput = findViewById(R.id.userInput);
        rectangles = findViewById(R.id.rectangles);
        stopMakingBtn = findViewById(R.id.ib_stopMaking);
        sendButton = findViewById(R.id.sendButton);
        makeDiaryButton = findViewById(R.id.makeDiary);
        miniArrow = findViewById(R.id.ib_makingDiary);
        moreButton = findViewById(R.id.more);
        who = findViewById(R.id.who);
        when = findViewById(R.id.when);
        where = findViewById(R.id.where);
        what = findViewById(R.id.what);
        how = findViewById(R.id.how);
        why = findViewById(R.id.why);
        mood = findViewById(R.id.mood);
        miniMic = findViewById(R.id.ib_mini_mic);
    }

    private void setupClickListeners() {
        sendButton.setOnClickListener(v -> {
            sound();
            sendMessage();
        });

        stopMakingBtn.setOnClickListener(v -> {
            sound();
            handleBackPress();
        });

        moreButton.setOnClickListener(v -> {
            sound();
            handleMoreButtonClick();
        });

        makeDiaryButton.setOnClickListener(v -> {
            sound();
            handleMakeDiaryButtonClick();
        });

        miniArrow.setOnClickListener(v -> {
            sound();
            handleMakeDiaryButtonClick();
        });

        miniMic.setOnClickListener(v -> {
            sound();
            startVoiceRecognition();
        });
    }

    private void fetchUserName() {
        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    name = document.exists() ? document.getString("name") : "";
                    showFirstMessageWithDelay();
                })
                .addOnFailureListener(e -> {
                    name = "";
                    showFirstMessageWithDelay();
                });
    }

    private void handleBackPress() {
        if (System.currentTimeMillis() - backPressedTime >= 2000) {
            backPressedTime = System.currentTimeMillis();
            Toast.makeText(this, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
        } else {
            finish();
        }
    }


    private void showFirstMessageWithDelay() {
        String firstBotMessageText = "안녕, " + name + " 꼬마 작가님! 오늘 어떤 일이 있었어?";
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Message firstBotMessage = new Message(firstBotMessageText, Message.TYPE_BOT);
            messageList.add(firstBotMessage);
            messageAdapter.notifyItemInserted(messageList.size() - 1);
            recyclerView.scrollToPosition(messageList.size() - 1);
        }, 2000); // 3000 밀리초 = 3초
    }

    private void handleMoreButtonClick() {
        sound();
        sendButton.setVisibility(View.VISIBLE);
        userInput.setVisibility(View.VISIBLE);
        rectangles.setVisibility(View.VISIBLE);
        miniMic.setVisibility(View.VISIBLE);
        makeDiaryButton.setVisibility(View.INVISIBLE);
        moreButton.setVisibility(View.INVISIBLE);
        Message moreConversationMessage = new Message("좋아! 나랑 더 이야기하자!😊 동화를 만들러 가고 싶다면 언제든지 화살표 버튼을 눌러줘.", Message.TYPE_BOT);
        messageList.add(moreConversationMessage);
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);
    }

    private void handleMakeDiaryButtonClick() {
        sound();
        Intent intent = new Intent(DiaryActivity.this, MakeDiaryActivity.class);
        intent.putExtra("story", story); // 스토리를 전달
        intent.putExtra("name", name); // 스토리를 전달
        startActivity(intent);
    }

    private void sendMessage() {
        String userMessageText = userInput.getText().toString();
        if (userMessageText.isEmpty()) {
            return; // 입력이 비어있으면 아무것도 하지 않음
        }
        // 메시지 추가 처리
        Message userMessage = new Message(userMessageText, Message.TYPE_USER);
        messageList.add(userMessage);
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);
        // 스토리에 메시지를 추가하지 않을 경우 처리
        if (!isConversationEnded) {
            story += "user: " + userMessageText + "\n"; // 각 메시지를 줄 바꿈으로 구분
        }

        // 사용자 메시지 생성
        Content.Builder userMessageBuilder = new Content.Builder();
        userMessageBuilder.setRole("user");
        userMessageBuilder.addText(userMessageText.replace("\n", " ")); // 줄 바꿈 문자 제거
        //Content userMessageContent = userMessageBuilder.build();

        if (!isConversationEnded) {
            analyzeUserMessageWithGemini(userMessageText);
        } else {
            // 대화가 종료된 상태에서는 메시지 분석을 하지 않음
            sendBotMessage(userMessageText);
        }
        // 입력 필드 비우기
        userInput.setText("");
    }

    private void analyzeUserMessageWithGemini(String message) {
        String prompt = "당신은 채팅 메시지에 육하원칙 및 감정이 존재하는지 분석하는 역할입니다." +
                "아래의 채팅에서 육하원칙('누구와', '언제', '어디서', '무엇을', '어떻게', '왜') 그리고 '기분'에 해당하는 정보가 있다면, " +
                "','로 구분하여 키워드를 뽑아 단답으로 답변하세요.\n" +
                "ex) 문장:안녕? 나 오늘 친구와 만났는데 즐거웠어.\n" +
                "답변:누구와:친구,언제: ,어디서: ,무엇을:만남,어떻게: ,왜: ,기분:즐거움\n" +
                "정보가 없는 항목은 공백으로 남깁니다.  단, 인사하는 것은 감정이 아닙니다." +
                "'누구와' 정보에 대해 누구와 함께했는지, 혹은 혼자였는지에 대한 언급이 없으면, 공백입니다. 혼자 했다고 말할 경우 키워드는 '혼자'입니다." +
                "'언제' 정보에 대해 '오늘', '어제', '내일'등은 포함하지 않습니다. '언제'는 특정 시각, 아침, 낮, 저녁 등 구체적인 시간대에 대한 언급이 없으면, 공백입니다." +
                "이제 아래 문장에 대해 분석하세요." +
                "\n문장: " + message;
        gemini.generateText(prompt, new Gemini.Callback() {
            @Override
            public void onSuccess(String resultText) {
                Log.d("AnalyzeResult", "분석 결과: " + resultText);
                // 분석된 결과를 추가적으로 처리
                processGeminiResult(resultText);
                // transformedMessage 설정
                StringBuilder transformedMessageBuilder = new StringBuilder();
                List<String> missingQuestions = new ArrayList<>();

                if (!hasWho) {
                    missingQuestions.add("누구와");
                }
                if (!hasWhen) {
                    missingQuestions.add("언제");
                }
                if (!hasWhere) {
                    missingQuestions.add("어디서");
                }
                if (!hasWhat) {
                    missingQuestions.add("무엇을");
                }
                if (!hasHow) {
                    missingQuestions.add("어떻게");
                }
                if (!hasWhy) {
                    missingQuestions.add("왜");
                }
                if (!hasMood) {
                    missingQuestions.add("감정");
                }

                if (missingQuestions.size() > 0) {
                    transformedMessageBuilder.append("'")
                            .append(String.join(", ", missingQuestions))
                            .append("는 현재까지 사용자가 한 번도 이야기한 적 없는 정보입니다. 대화의 흐름에 맞는 경우, 이 중 한가지를 질문해주세요." +
                                    "그러나 반드시 질문할 필요는 없습니다. 사용자와 즐거운 이야기를 나누는 것도 중요합니다. \n문장: ");
                }

                transformedMessageBuilder.append(message);
                String transformedMessage = transformedMessageBuilder.toString();
                sendBotMessage(transformedMessage);

            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("AnalyzeError", "분석 실패", t);
            }
        });
    }

    private void processGeminiResult(String resultText) {
        runOnUiThread(() -> {
            String[] parts = resultText.split(",");
            for (String part : parts) {
                String[] keyValue = part.split(":");
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim();
                    switch (key) {
                        case "누구와":
                            if (!value.isEmpty()) {
                                hasWho = true;
                                who.setTextColor(Color.WHITE);
                            }
                            break;
                        case "언제":
                            if (!value.isEmpty()) {
                                hasWhen = true;
                                when.setTextColor(Color.WHITE);
                            }
                            break;
                        case "어디서":
                            if (!value.isEmpty()) {
                                hasWhere = true;
                                where.setTextColor(Color.WHITE);
                            }
                            break;
                        case "무엇을":
                            if (!value.isEmpty()) {
                                hasWhat = true;
                                what.setTextColor(Color.WHITE);
                            }
                            break;
                        case "어떻게":
                            if (!value.isEmpty()) {
                                hasHow = true;
                                how.setTextColor(Color.WHITE);
                            }
                            break;
                        case "왜":
                            if (!value.isEmpty()) {
                                hasWhy = true;
                                why.setTextColor(Color.WHITE);
                            }
                            break;
                        case "기분":
                            if (!value.isEmpty()) {
                                hasMood = true;
                                mood.setTextColor(Color.WHITE);
                            }
                            break;
                    }
                }
            }

        });
    }

    private void sendBotMessage(String userMessageText) {
        // 사용자 메시지 생성
        Content.Builder userMessageBuilder = new Content.Builder();
        userMessageBuilder.setRole("user");
        userMessageBuilder.addText(userMessageText.replace("\n", " ")); // 줄 바꿈 문자 제거
        Content userMessageContent = userMessageBuilder.build();
        // Executor 생성
        Executor executor = Executors.newSingleThreadExecutor();
        // 메시지 전송
        ListenableFuture<GenerateContentResponse> response = chat.sendMessage(userMessageContent);
        // 응답 처리
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText().replace("\n", " "); // 줄 바꿈 문자 제거
                // UI 업데이트를 메인 스레드에서 수행
                runOnUiThread(() -> {
                    Message botMessage = new Message(resultText, Message.TYPE_BOT);
                    messageList.add(botMessage);
                    messageAdapter.notifyItemInserted(messageList.size() - 1);
                    recyclerView.scrollToPosition(messageList.size() - 1);
                    if (!isConversationEnded) {
                        story += "AI: " + resultText + "\n";
                        if (hasWho && hasWhen && hasWhere && hasWhat && hasHow && hasWhy && hasMood && !isConversationEnded) {
                            endConversation();
                        }
                    }

                });
            }

            @Override
            public void onFailure(Throwable t) {
                // Log 오류 출력
                Log.e("BotMessageError", "메시지 전송 실패", t);
            }
        }, executor);
    }

    private void endConversation() {
        runOnUiThread(() -> {
            isConversationEnded = true;
            String endMessage = "너의 이야기를 동화로 제작할 준비가 끝났어! 이제 만들러 가볼까?";
            Message endBotMessage = new Message(endMessage, Message.TYPE_BOT);
            messageList.add(endBotMessage);
            messageAdapter.notifyItemInserted(messageList.size() - 1);
            recyclerView.scrollToPosition(messageList.size() - 1);
            Log.d("Story", "대화 내역:\n" + story);
            makeDiaryButton.setVisibility(View.VISIBLE);
            moreButton.setVisibility(View.VISIBLE);
            miniMic.setVisibility(View.INVISIBLE);
            rectangles.setVisibility(View.INVISIBLE);
            miniArrow.setVisibility(View.VISIBLE);
            sendButton.setVisibility(View.INVISIBLE);
            userInput.setVisibility(View.INVISIBLE);
        });
    }

    // 음성 인식 시작
    private void startVoiceRecognition() {
        recognizedText.setLength(0);
        mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Toast.makeText(getApplicationContext(), "음성인식을 시작합니다.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float rmsdB) {
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
            }

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
                    userInput.setText(recognizedText.toString());
                }
            }
        });
        mRecognizer.startListening(new Intent());
    }

    // 음성 인식 시 필요한 에러 텍스트
    private String getErrorText(int errorCode) {
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                return "오디오 에러";
            case SpeechRecognizer.ERROR_CLIENT:
                return "클라이언트 에러";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return "퍼미션 없음";
            case SpeechRecognizer.ERROR_NETWORK:
                return "네트워크 에러";
            case SpeechRecognizer.ERROR_NO_MATCH:
                return "일치하는 결과 없음";
            default:
                return "알 수 없는 에러";
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