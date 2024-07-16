package com.example.holymoly;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.Executor;

public class SelectcharacterActivity extends AppCompatActivity {
    private Executor executor = new MainThreadExecutor(); // MainThreadExecutor 사용
    private TextView tv;
    private String[] character = new String[10]; // 캐릭터 이름을 저장할 배열

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectcharacter);

        tv = findViewById(R.id.tv);

        Intent intent = getIntent();
        String thema = intent.getStringExtra("selectedThema");
        if (thema != null) {
            // 테마에 따른 AI 요청
            requestCharacterNames(thema);
        } else {
            Toast.makeText(this, "테마가 없습니다", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestCharacterNames(String thema) {
        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", "AIzaSyB5Vf0Nk67nJOKk4BADvPDQhRGNyYTVxjU");
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);
//요청 텍스트 생성
        String requestText = thema + "테마를 주제로 동화를 만들려고 합니다. 동화에 어울릴만한 등장인물의 후보가 10개 필요합니다. 1~5글자로 단답형으로 답해주세요. 후보와 후보 사이에는 ', '로 띄어주세요." +
                "그러면 저희는 당신이 정해준 후보들 중 몇 개를 선택하여 동화를 만들 것입니다. 당신이 답변 할 형식의 예시는 이렇습니다. '공주, 왕자, 물고기, 나무, 토끼, 거북이, 호랑이, 나비, 엄마, 동생'. ";

        Content content = new Content.Builder()
                .addText(requestText)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                final String resultText = result.getText();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 결과를 배열에 저장
                        parseCharacterNames(resultText);
                        // 결과를 TextView에 설정
                        tv.setText(resultText);
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv.setText("AI 요청 실패: " + t.getMessage());
                    }
                });
            }
        }, executor);
    }

    private void parseCharacterNames(String resultText) {
        if (resultText != null && !resultText.isEmpty()) {
            String[] names = resultText.split(", ");
            if (names.length == 10) {
                System.arraycopy(names, 0, character, 0, names.length);
            } else {
                tv.setText("AI가 10개의 이름을 반환하지 않았습니다. 반환된 이름 수: " + names.length);
            }
        } else {
            tv.setText("AI 응답이 비어있습니다");
        }
    }

        /*

        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash","AIzaSyB5Vf0Nk67nJOKk4BADvPDQhRGNyYTVxjU");
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        Content content = new Content.Builder()
                .addText("바다속 테마 동화 속에 등장할 가상의 캐릭터 이름을 5개 정해주세요." +
                        "1~5글자의 이름으로 단답형으로 답해주세요. 이름과 이름 사이에는 ', '로 띄어주세요. ")
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText();
                tv.setText(resultText);
            }

            @Override
            public void onFailure(Throwable t) {
                tv.setText(t.toString());
            }
        }, executor);
        */
}
