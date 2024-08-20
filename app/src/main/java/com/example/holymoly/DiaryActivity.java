package com.example.holymoly;

import android.graphics.Color;
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
    private TextView who, when, where, what, how, why, mood;

    // 육하원칙 및 감정 플래그
    private boolean hasWho = false;
    private boolean hasWhen = false;
    private boolean hasWhere = false;
    private boolean hasWhat = false;
    private boolean hasHow = false;
    private boolean hasWhy = false;
    private boolean hasMood = false;

    private Gemini gemini = new Gemini();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);

        // UI 요소 초기화
        recyclerView = findViewById(R.id.recyclerView);
        userInput = findViewById(R.id.userInput);
        sendButton = findViewById(R.id.sendButton);
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
                "오늘 있었던 일을 주제로 대화할 건데, 육하원칙을 중심으로 그 경험에 대해 물어봐주세요." +
                "감정도 함께 이야기할 수 있도록 유도해주세요. 한 번에 하나씩만 질문해주세요. 폐쇄형 질문이 아닌 개방형 질문으로 해주세요." +
                "누구와 함께했는지에 대해 말하지 않았다면, 함께한 사람이 있었는지 물어봐주세요");
        Content userContent = userContentBuilder.build();

        Content.Builder modelContentBuilder = new Content.Builder();
        modelContentBuilder.setRole("model");
        modelContentBuilder.addText("응, 알겠어.");
        Content modelContent = modelContentBuilder.build();

        List<Content> history = Arrays.asList(userContent, modelContent);
        // 채팅 초기화
        chat = modelFutures.startChat(history);

        // AI의 첫 메시지 추가
        String firstBotMessageText = "안녕 꼬마작가님! 오늘 어떤 일이 있었는지 이야기해줄래?";
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
        String prompt = "아래 문장에서 육하원칙 즉, '누구와', '언제', '어디서', '무엇을', '어떻게', '왜, 그리고 '기분'에 해당하는 정보가 있을 경우, ','로 분리하여 키워드를 뽑아 단답으로 답변하세요.\n" +
                " ex) 문장:친구와 아침에 만났는데 즐거웠다.\n" +
                "답변:누구와:친구,언제:아침,어디서: ,무엇을:만남,어떻게: ,왜: ,기분:즐거움\n" +
                "키워드가 없을 경우 공백으로 나타냅니다. 단, 인사하는 것은 감정이 아닙니다." +
                "'누구와' 정보에 대해 누구와 함께했는지, 혹은 혼자였는지에 대한 언급이 없으면, 공백입니다." +
                "'언제' 정보에 대해 '오늘', '어제', '내일'등은 포함되지 않습니다. '언제'는 특정 시각, 아침, 점심, 등 구체적인 시간대를 언급할 경우에만 키워드로 인정합니다." +
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
        // 결과 문자열을 분리하여 각 항목을 파싱
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

        // 대화 종료 조건 확인
        if (hasWho && hasWhen && hasWhere && hasWhat && hasHow && hasWhy && hasMood) {
            endConversation();
        } else {
            sendBotMessage(resultText);
        }
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
        if (resultText.contains("누구")) {
            hasWho = true;
            who.setTextColor(Color.WHITE);
        }
        if (resultText.contains("언제")) {
            hasWhen = true;
            when.setTextColor(Color.WHITE);
        }
        if (resultText.contains("어디서")) {
            hasWhere = true;
            where.setTextColor(Color.WHITE);
        }
        if (resultText.contains("무엇을")) {
            hasWhat = true;
            what.setTextColor(Color.WHITE);
        }
        if (resultText.contains("어떻게")) {
            hasHow = true;
            how.setTextColor(Color.WHITE);
        }
        if (resultText.contains("왜")) {
            hasWhy = true;
            why.setTextColor(Color.WHITE);
        }
        if (resultText.contains("기분")) {
            hasMood= true;
            mood.setTextColor(Color.WHITE);
        }
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