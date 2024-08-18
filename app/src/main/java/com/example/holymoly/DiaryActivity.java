package com.example.holymoly;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.FutureCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DiaryActivity extends AppCompatActivity {
    private GenerativeModel model;
    private ChatFutures chat;
    private EditText userInput;
    private ImageButton sendButton;
    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private List<Message> messageList = new ArrayList<>();

    @Override
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

        // GenerativeModel 초기화
        model = new GenerativeModel("gemini-1.5-flash", "AIzaSyB5Vf0Nk67nJOKk4BADvPDQhRGNyYTVxjU");
        GenerativeModelFutures modelFutures = GenerativeModelFutures.from(model);

        // 이전 채팅 기록 생성
        Content.Builder userContentBuilder = new Content.Builder();
        userContentBuilder.setRole("user");
        userContentBuilder.addText("Hello, I have 2 dogs in my house.");
        Content userContent = userContentBuilder.build();

        Content.Builder modelContentBuilder = new Content.Builder();
        modelContentBuilder.setRole("model");
        modelContentBuilder.addText("Great to meet you. What would you like to know?");
        Content modelContent = modelContentBuilder.build();

        List<Content> history = Arrays.asList(userContent, modelContent);

        // 채팅 초기화
        chat = modelFutures.startChat(history);

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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Message botMessage = new Message(resultText, Message.TYPE_BOT);
                        messageList.add(botMessage);
                        messageAdapter.notifyItemInserted(messageList.size() - 1);
                        recyclerView.scrollToPosition(messageList.size() - 1);
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

}
