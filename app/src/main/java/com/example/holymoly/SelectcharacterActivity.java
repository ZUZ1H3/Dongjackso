package com.example.holymoly;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class SelectcharacterActivity extends AppCompatActivity {
    private Executor executor = new MainThreadExecutor(); // MainThreadExecutor 사용
    private CheckBox[] checkBoxes = new CheckBox[10]; // 체크박스를 저장할 배열
    private String[] character = new String[10]; // 캐릭터 이름을 저장할 배열
    private ImageButton nextStepButton; // 다음 버튼

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectcharacter);

        // 체크박스 초기화
        checkBoxes[0] = findViewById(R.id.charname1);
        checkBoxes[1] = findViewById(R.id.charname2);
        checkBoxes[2] = findViewById(R.id.charname3);
        checkBoxes[3] = findViewById(R.id.charname4);
        checkBoxes[4] = findViewById(R.id.charname5);
        checkBoxes[5] = findViewById(R.id.charname6);
        checkBoxes[6] = findViewById(R.id.charname7);
        checkBoxes[7] = findViewById(R.id.charname8);
        checkBoxes[8] = findViewById(R.id.charname9);
        checkBoxes[9] = findViewById(R.id.charname10);

        nextStepButton = findViewById(R.id.ib_nextStep); // 다음 버튼 초기화

        Intent intent = getIntent();
        String thema = intent.getStringExtra("selectedThema");
        if (thema != null) {
            // 테마에 따른 AI 요청
            requestCharacterNames(thema);
        } else {
            Toast.makeText(this, "테마가 없습니다", Toast.LENGTH_SHORT).show();
        }

        nextStepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleNextStep();
            }
        });
    }

    private void requestCharacterNames(String thema) {
        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", "YOUR_API_KEY_HERE");
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);
        // 요청 텍스트 생성
        String requestText = thema + "테마를 주제로 동화를 만들려고 합니다. 동화에 어울릴만한 등장인물의 후보가 10개 필요합니다. 1~5글자로 단답형으로 답해주세요. 후보와 후보 사이에는 ', '로 띄어주세요." +
                "그러면 저희는 당신이 정해준 후보들 중 몇 개를 선택하여 동화를 만들 것입니다. 당신이 답변 할 형식의 예시는 이렇습니다. '공주, 왕자, 물고기, 나무, 토끼, 거북이, 호랑이, 나비, 엄마, 동생'.";

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
                        // 결과를 Toast 메시지로 표시
                        Toast.makeText(SelectcharacterActivity.this, resultText, Toast.LENGTH_LONG).show();
                        // 체크박스에 이름 설정
                        setCheckBoxNames();
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SelectcharacterActivity.this, "AI 요청 실패: " + t.getMessage(), Toast.LENGTH_LONG).show();
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
                Toast.makeText(this, "AI가 10개의 이름을 반환하지 않았습니다. 반환된 이름 수: " + names.length, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "AI 응답이 비어있습니다", Toast.LENGTH_LONG).show();
        }
    }

    private void setCheckBoxNames() {
        for (int i = 0; i < checkBoxes.length; i++) {
            checkBoxes[i].setText(character[i]);
        }
    }

    private void handleNextStep() {
        List<String> selectedCharacters = new ArrayList<>();
        for (CheckBox checkBox : checkBoxes) {
            if (checkBox.isChecked()) {
                selectedCharacters.add(checkBox.getText().toString());
            }
        }

        if (selectedCharacters.size() < 2) {
            Toast.makeText(this, "최소 2개의 캐릭터를 선택해주세요", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(this, Makepage1Activity.class);
            intent.putStringArrayListExtra("selectedCharacters", new ArrayList<>(selectedCharacters));
            startActivity(intent);
        }
    }

}
