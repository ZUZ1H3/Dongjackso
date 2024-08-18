package com.example.holymoly;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.FutureCallback;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DiaryActivity extends AppCompatActivity {
    // GenerativeModel 객체와 ChatFutures 객체
    private GenerativeModel model;
    private ChatFutures chat;
    private TextView userMessages;
    private TextView chatBotResponses;
    private EditText userInput;
    private ImageButton sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);

        // UI 요소 초기화
        userMessages = findViewById(R.id.userMessages); // 사용자 메시지를 표시할 TextView
        chatBotResponses = findViewById(R.id.chatBotResponses); // 챗봇 응답을 표시할 TextView
        userInput = findViewById(R.id.userInput); // 사용자 입력을 받는 EditText
        sendButton = findViewById(R.id.sendButton); // 메시지 전송 버튼

        // GenerativeModel 초기화
        model = new GenerativeModel("gemini-1.5-flash", "AIzaSyB5Vf0Nk67nJOKk4BADvPDQhRGNyYTVxjU");
        GenerativeModelFutures modelFutures = GenerativeModelFutures.from(model);

        // 이전 채팅 기록 생성
        Content.Builder userContentBuilder = new Content.Builder();
        userContentBuilder.setRole("user");
        userContentBuilder.addText("Hello, I have 2 dogs in my house."); // 사용자 메시지
        Content userContent = userContentBuilder.build();

        Content.Builder modelContentBuilder = new Content.Builder();
        modelContentBuilder.setRole("model");
        modelContentBuilder.addText("Great to meet you. What would you like to know?"); // 챗봇 응답
        Content modelContent = modelContentBuilder.build();

        List<Content> history = Arrays.asList(userContent, modelContent);

        // 채팅 초기화
        chat = modelFutures.startChat(history);

        // 버튼 클릭 리스너 설정
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(); // 버튼 클릭 시 메시지 전송
            }
        });
    }

    private void sendMessage() {
        String userMessageText = userInput.getText().toString(); // 사용자 입력 텍스트 가져오기
        if (userMessageText.isEmpty()) {
            return; // 입력이 비어있으면 아무 작업도 하지 않음
        }

        // 사용자 메시지 생성
        Content.Builder userMessageBuilder = new Content.Builder();
        userMessageBuilder.setRole("user");
        userMessageBuilder.addText(userMessageText); // 사용자 메시지 설정
        Content userMessage = userMessageBuilder.build();

        // Executor 생성
        Executor executor = Executors.newSingleThreadExecutor();

        // 메시지 전송
        ListenableFuture<GenerateContentResponse> response = chat.sendMessage(userMessage);

        // 응답 처리
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText(); // 챗봇 응답 텍스트 가져오기
                // UI 업데이트를 메인 스레드에서 수행
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateChat(userMessageText, resultText); // UI 업데이트
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace(); // 오류 출력
            }
        }, executor);

        // 입력 필드 비우기
        userInput.setText("");
    }

    private void updateChat(String userMessage, String botResponse) {
        // 사용자 메시지와 챗봇 응답을 각각 업데이트
        userMessages.setText(userMessage); // 사용자 메시지 표시
        chatBotResponses.setText(botResponse); // 챗봇 응답 표시
    }
}
