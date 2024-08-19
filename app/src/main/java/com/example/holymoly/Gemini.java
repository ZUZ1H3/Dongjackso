package com.example.holymoly;

import android.util.Log;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Gemini {
    private static final String GEMINI_API_KEY = "AIzaSyB5Vf0Nk67nJOKk4BADvPDQhRGNyYTVxjU"; // Gemini API 키
    private Executor executor = Executors.newSingleThreadExecutor(); // 백그라운드 작업을 위한 Executor

    public interface Callback {
        void onSuccess(String text);
        void onFailure(Throwable t);
    }

    public void generateText(String prompt, Callback callback) {
        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", GEMINI_API_KEY);
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        Content content = new Content.Builder()
                .addText(prompt)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText();
                // Log 결과 출력
                Log.d("GeminiResponse", "AI 분석 결과: " + resultText);
                callback.onSuccess(resultText);
            }

            @Override
            public void onFailure(Throwable t) {
                // Log 오류 출력
                Log.e("GeminiError", "AI 분석 실패", t);
                callback.onFailure(t);
            }
        }, executor);
    }
}