package com.example.holymoly;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.*;
import com.google.ai.client.generativeai.type.*;
import com.google.common.util.concurrent.*;
import java.util.*;
import java.util.concurrent.*;

public class DiaryActivity extends AppCompatActivity {
    private GenerativeModel model;
    private ChatFutures chat;
    private EditText userInput;
    private ImageButton sendButton;
    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private List<Message> messageList = new ArrayList<>();

    // 육하원칙 및 감정 플래그
    private boolean hasWho = false;
    private boolean hasWhen = false;
    private boolean hasWhere = false;
    private boolean hasWhat = false;
    private boolean hasHow = false;
    private boolean hasWhy = false;

    private Gemini gemini = new Gemini();
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);

        // UI 요소 초기화
        recyclerView = findViewById(R.id.recyclerView);
        userInput = findViewById(R.id.userInput);
        sendButton = findViewById(R.id.sendButton);

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
                "오늘 있었던 일에 대해 이야기할 건데, 육하원칙을 기준으로 그 경험에 대해 하나하나 물어봐주세요." +
                "감정도 함께 이야기해줄 수 있도록 유도해주세요. 한 번에 하나씩만 질문해주세요.");
        Content userContent = userContentBuilder.build();

        Content.Builder modelContentBuilder = new Content.Builder();
        modelContentBuilder.setRole("model");
        modelContentBuilder.addText("응, 알겠어.");
        Content modelContent = modelContentBuilder.build();

        List<Content> history = Arrays.asList(userContent, modelContent);
        // 채팅 초기화
        chat = modelFutures.startChat(history);

        // AI의 첫 메시지 추가
        String firstBotMessageText = "안녕! 오늘 어떤 일이 있었는지 이야기해줄래?";
        Message firstBotMessage = new Message(firstBotMessageText, Message.TYPE_BOT);
        messageList.add(firstBotMessage);
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);

        // 버튼 클릭 리스너 설정
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void sendMessage() {
        String userMessageText = userInput.getText().toString();
        if (userMessageText.isEmpty()) {
            return; // 입력이 비어있으면 아무것도 하지 않음
        }

        // 사용자 메시지 추가
        Message userMessage = new Message(userMessageText, Message.TYPE_USER);
        messageList.add(userMessage);
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);

        // 사용자 메시지 생성
        Content.Builder userMessageBuilder = new Content.Builder();
        userMessageBuilder.setRole("user");
        userMessageBuilder.addText(userMessageText.replace("\n", " ")); // 줄 바꿈 문자 제거
        Content userMessageContent = userMessageBuilder.build();

        // 메시지 분석 (Gemini를 활용)
        analyzeUserMessageWithGemini(userMessageText);

        // 입력 필드 비우기
        userInput.setText("");
    }

    private void analyzeUserMessageWithGemini(String message) {
        String prompt = "아래 문장에서 육하원칙 즉, '누구', '언제', '어디서', '무엇을', '어떻게', '왜'에 해당하는 정보가 있을 경우, 이렇게 단답으로 답변하세요." +
                " ex)친구와 아침에 만났다. 답변: '누구:친구, 언제:아침, 무엇을:만났다'\n문장: " + message;

        gemini.generateText(prompt, new Gemini.Callback() {
            @Override
            public void onSuccess(String resultText) {
                // Log 분석 결과 출력
                Log.d("AnalyzeResult", "분석 결과: " + resultText);
                // 분석된 결과를 바탕으로 플래그 설정
                updatePrincipleFlags(resultText);

                // 대화 종료 조건 확인
                if (hasWho && hasWhen && hasWhere && hasWhat && hasHow && hasWhy) {
                    endConversation();
                } else {
                    sendBotMessage(message);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                // Log 오류 출력
                Log.e("AnalyzeError", "분석 실패", t);
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

    private void updatePrincipleFlags(String resultText) {
        if (resultText.contains("언제")) { hasWhen = true; }
        if (resultText.contains("누구")) { hasWho = true; }
        if (resultText.contains("어디서")) { hasWhere = true; }
        if (resultText.contains("무엇을")) { hasWhat = true; }
        if (resultText.contains("어떻게")) { hasHow = true; }
        if (resultText.contains("왜")) { hasWhy = true; }
    }

    private void endConversation() {
        runOnUiThread(() -> {
            String endMessage = "모든 이야기가 끝났어! 이제 이야기를 만들어볼까?";
            Message endBotMessage = new Message(endMessage, Message.TYPE_BOT);
            messageList.add(endBotMessage);
            messageAdapter.notifyItemInserted(messageList.size() - 1);
            recyclerView.scrollToPosition(messageList.size() - 1);
        });
    }
}