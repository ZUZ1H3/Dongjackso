package com.example.holymoly;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MakeStory {
    private Executor executor = Executors.newSingleThreadExecutor(); // 백그라운드 작업을 위한 Executor
    private static final String GEMINI_API_KEY = "AIzaSyB5Vf0Nk67nJOKk4BADvPDQhRGNyYTVxjU"; // Gemini API 키
    private String theme;
    private String characters;
    private String storySoFar = "";
    private Makepage1Activity makepage1Activity; // Makepage1Activity를 멤버 변수로 선언

    public MakeStory(Makepage1Activity activity, String theme, String characters) {
        this.makepage1Activity = activity;
        this.theme = theme;
        this.characters = characters;
    }

    // 동화의 도입부 프롬프트
    public String buildInitialPrompt() {
        return "동화 배경: " + theme + "\n" +
                "등장인물: " + characters + "\n" +
                "어린이가 대상인 동화의 도입부를 작성해 주세요. 이야기는 즐겁고, 긍정적이고, 흥미롭게 시작되어야 합니다." +
                "배경은 더 구체적으로 설정하면 좋습니다. 2문장 정도로 작성해주세요." +
                "다음 내용이 포함되어야 합니다: 구체적인 배경 설명, 등장인물 소개, 이야기를 이끌어갈 서사적 요소.";
    }

    // 동화의 나머지 부분을 위한 프롬프트 빌드
    private String buildPrompt() {
        return "동화 배경: " + theme + "\n" +
                "등장인물: " + characters + "\n" +
                "현재 이야기: " + storySoFar + "\n" +
                "이야기를 계속 이어가 주세요. 이야기는 즐겁고, 긍정적이고, 흥미로워야 합니다. 2~3문장으로 작성해주세요.";
    }

    // 사건 프롬프트 빌드
    private String buildIncidentPrompt() {
        return "동화 배경: " + theme + "\n" +
                "등장인물: " + characters + "\n" +
                "현재 이야기: " + storySoFar + "\n" +
                "이야기를 계속 이어가 주세요. '그러나', '그런데', '갑자기' 등을 사용하여, 가볍고 흥미로운 사건이 시작되는 이야기로 작성해주세요." +
                "2~3문장으로 작성해주세요.";
    }

    // 선택지 프롬프트 빌드
    private String buildChoicePrompt() {
        return "동화 배경: " + theme + "\n" +
                "등장인물: " + characters + "\n" +
                "현재 이야기: " + storySoFar + "\n" +
                "어린이인 사용자에게 선택지를 제공하기 위해 두 가지 가능한 이야기를 생성해 주세요. 각각 1문장으로 작성해주세요.";
    }

    public void generateInitialStory() {
        String prompt = buildInitialPrompt();

        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", GEMINI_API_KEY);
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        Content content = new Content.Builder()
                .addText(prompt)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                handleInitialStorySuccess(result);
            }

            @Override
            public void onFailure(Throwable t) {
                // 실패 시 처리 (여기서는 생략)
            }
        }, executor);
    }

    private void handleInitialStorySuccess(GenerateContentResponse result) {
        final String resultText = result.getText();
        makepage1Activity.updateStoryTextView(resultText); // TextView 업데이트
        storySoFar = resultText;
        // generateChoices(); // 초기 이야기 후 선택지 생성 (선택적으로 호출)
    }
}
