package com.example.holymoly;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
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
    private GenerativeModel model;
    private ChatFutures chat;
    private EditText userInput;
    private ImageView rectangles;
    private ImageButton sendButton, makeDiaryButton, moreButton;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);
        recyclerView = findViewById(R.id.recyclerView);
        userInput = findViewById(R.id.userInput);
        rectangles= findViewById(R.id.rectangles);
        sendButton = findViewById(R.id.sendButton);
        makeDiaryButton = findViewById(R.id.makeDiary);
        moreButton = findViewById(R.id.more);
        who = findViewById(R.id.who);
        when = findViewById(R.id.when);
        where = findViewById(R.id.where);
        what = findViewById(R.id.what);
        how = findViewById(R.id.how);
        why = findViewById(R.id.why);
        mood = findViewById(R.id.mood);
        // RecyclerView 설정
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);
        // GenerativeModel 초기화 및 이전 채팅 기록 설정
        model = new GenerativeModel("gemini-1.5-flash", "AIzaSyB5Vf0Nk67nJOKk4BADvPDQhRGNyYTVxjU");
        GenerativeModelFutures modelFutures = GenerativeModelFutures.from(model);
        // 이전 채팅 기록 생성
        Content.Builder userContentBuilder = new Content.Builder();
        userContentBuilder.setRole("user");
        userContentBuilder.addText("당신이 대화할 대상은 어린이입니다. 다정하고 친근한 반말 말투로 말해주세요." +
                "사용자는 오늘 있었던 일에 대해 이야기할 것입니다. 리액션과 함께 그 경험에 대해 구체적으로 대답할 수 있도록 물어봐주세요." +
                "사용자가 '누구와 함께했는지, 언제, 어디서, 무엇을, 어떻게, 왜'에 대한 이야기를 하지 않았다면, 이를 이야기할 수 있도록 물어봐주세요."+
                "감정도 함께 이야기할 수 있도록 유도해주세요. 한 번에 한가지만 질문해주세요. 폐쇄형 질문이 아닌 개방형 질문으로 해주세요." +
                "누군가와 함께 했는지, 혼자했는지 여부를 알 수 없다면, 함께한 사람이 있었는지 물어봐주세요."+
                "그러나 질문만 계속 하지는 말고 가끔 주제에 맞는 재밌는 이야기도 하며 수다를 떨어주세요."+
                "'ㅋㅋㅋ', 'ㅎㅎ'와 같은 초성으로 대화하지 마세요. 표준어를 사용해주세요.");
        Content userContent = userContentBuilder.build();
        Content.Builder modelContentBuilder = new Content.Builder();
        modelContentBuilder.setRole("model");
        modelContentBuilder.addText("안녕, 오늘 어떤 일이 있었어?");
        Content modelContent = modelContentBuilder.build();
        List<Content> history = Arrays.asList(userContent, modelContent);
        chat = modelFutures.startChat(history);
        // 데이터베이스에서 이름을 가져옴
        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String userName = document.getString("name");
                        name = userName;
                        showFirstMessageWithDelay();
                    } else {
                        name = ""; // 이름을 가져오는 데 실패했을 때 기본 이름으로 설정
                        showFirstMessageWithDelay();
                    }
                })
                .addOnFailureListener(e -> {
                    name = "";// 이름을 가져오는 데 실패했을 때 기본 이름으로 설정
                    showFirstMessageWithDelay();
                });
        // 버튼 클릭 리스너 설정
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        // 더 대화하기 버튼 클릭 리스너 설정
        moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleMoreButtonClick();
            }
        });
        // 동화 제작 버튼 클릭 리스너 설정
        makeDiaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleMakeDiaryButtonClick();
            }
        });
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
        //isConversationEnded = false; // 대화가 계속 진행되도록 플래그 업데이트
        // UI 업데이트: 대화 입력 필드와 버튼을 보이도록 설정
        sendButton.setVisibility(View.VISIBLE);
        userInput.setVisibility(View.VISIBLE);
        rectangles.setVisibility(View.VISIBLE);
        makeDiaryButton.setVisibility(View.INVISIBLE);
        moreButton.setVisibility(View.INVISIBLE);
        // 대화 재개 메시지 또는 설정을 추가할 수 있습니다.
        Message moreConversationMessage = new Message("좋아! 나랑 더 이야기하자!😊 동화를 만들러 가고 싶다면 언제든지 OK 버튼을 눌러줘.", Message.TYPE_BOT);
        messageList.add(moreConversationMessage);
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);
    }
    private void handleMakeDiaryButtonClick() {
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
            story += userMessageText + "\n"; // 각 메시지를 줄 바꿈으로 구분
        }
        // 사용자 메시지 생성
        Content.Builder userMessageBuilder = new Content.Builder();
        userMessageBuilder.setRole("user");
        userMessageBuilder.addText(userMessageText.replace("\n", " ")); // 줄 바꿈 문자 제거
        Content userMessageContent = userMessageBuilder.build();
        if (!isConversationEnded) {
            // 메시지 분석 (Gemini를 활용)
            analyzeUserMessageWithGemini(userMessageText);
        } else {
            // 대화가 종료된 상태에서는 메시지 분석을 하지 않음
            sendBotMessage(userMessageText);
        }
        // 입력 필드 비우기
        userInput.setText("");
    }

    private void analyzeUserMessageWithGemini(String message) {
                String prompt = "아래 문장에서 육하원칙 즉, '누구와', '언제', '어디서', '무엇을', '어떻게', '왜, 그리고 '기분'에 해당하는 정보가 있을 경우, " +
                "','로 분리하여 키워드를 뽑아 단답으로 답변하세요.\n" +
                " ex) 문장:친구와 아침에 만났는데 즐거웠다.\n" +
                "답변:누구와:친구,언제:아침,어디서: ,무엇을:만남,어떻게: ,왜: ,기분:즐거움\n" +
                "키워드가 없을 경우 공백으로 나타냅니다. 단, 인사하는 것은 감정이 아닙니다." +
                "'누구와' 정보에 대해 누구와 함께했는지, 혹은 혼자였는지에 대한 언급이 없으면, 공백입니다." +
                "'언제' 정보에 대해 '오늘', '어제', '내일'등은 포함되지 않습니다. '언제'는 특정 시각, 아침, 낮, 저녁 등 구체적인 시간대에 대한 언급이 없으면, 공백입니다." +
                "이모지를 사용하지 마세요. 'ㅋㅋㅋ', 'ㅎㅎ' 등 초성을 사용하지 마세요.\n" +
                "ex)사용자: 나 오늘 도서관에서 공부했어.\n" +
                "당신: 오늘 도서관에서 공부를 했구나! 힘들었을텐데 정말 대단하다. 도서관에는 언제 갔니? 낮에 간거야?\n"+
                "이제 아래의 문장에 대해 답변해주세요." +
                " \n문장: " + message;
        gemini.generateText(prompt, new Gemini.Callback() {
            @Override
            public void onSuccess(String resultText) {
                Log.d("AnalyzeResult", "분석 결과: " + resultText);
                // 분석된 결과를 추가적으로 처리
                processGeminiResult(resultText);
            }
            @Override
            public void onFailure(Throwable t) {
                Log.e("AnalyzeError", "분석 실패", t);
            }
        });
    }
    private void processGeminiResult(String resultText) {
        // Result processing and UI updates need to be done on the main thread
        runOnUiThread(() -> {
            // Results processing
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
            // End conversation if all required fields are filled
            if (hasWho && hasWhen && hasWhere && hasWhat && hasHow && hasWhy && hasMood && !isConversationEnded) {
                endConversation();
            } else {
                sendBotMessage(resultText);
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
            String endMessage = "너의 이야기를 동화로 제작할 준비가 끝났어! 이제 만들러 가볼까?";
            Message endBotMessage = new Message(endMessage, Message.TYPE_BOT);
            messageList.add(endBotMessage);
            messageAdapter.notifyItemInserted(messageList.size() - 1);
            recyclerView.scrollToPosition(messageList.size() - 1);
            Log.d("Story", "사용자가 작성한 전체 스토리:\n" + story);
            makeDiaryButton.setVisibility(View.VISIBLE);
            moreButton.setVisibility(View.VISIBLE);
            rectangles.setVisibility(View.INVISIBLE);
            sendButton.setVisibility(View.INVISIBLE);
            userInput.setVisibility(View.INVISIBLE);
            isConversationEnded = true;
        });
    }
}