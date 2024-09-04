package com.example.holymoly;

import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class MakeStory {
    private Gemini gemini;
    private String theme;
    private String characters;
    private String storySoFar = "";
    private MakeStoryActivity makeStoryActivity;
    private static final int MAX_RETRY_COUNT = 3; // 최대 재시도 횟수
    private int retryCount = 0; // 현재 재시도 횟수

    public MakeStory(MakeStoryActivity activity, String theme, ArrayList<String> characters, Gemini gemini) {
        this.makeStoryActivity = activity;
        this.theme = theme;
        this.characters = String.join(", ", characters);
        this.gemini = gemini;
    }

    // 동화 도입부 생성
    public void generateInitialStory() {
        String prompt = buildInitialPrompt();
        gemini.generateText(prompt, new Gemini.Callback() {
            @Override
            public void onSuccess(String text) {
                retryCount = 0; // 성공 시 재시도 횟수 초기화
                storySoFar += text + " ";
                if (makeStoryActivity != null) {
                    makeStoryActivity.runOnUiThread(() -> {
                        makeStoryActivity.translate(text); // 선택한 테마와 캐릭터를 번역하는 함수(이미지를 만들기 위함)
                        Toast.makeText(makeStoryActivity, "도입부 생성 성공", Toast.LENGTH_SHORT).show(); // 성공 시 토스트 메시지
                    });
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (retryCount < MAX_RETRY_COUNT) {
                    retryCount++;
                    generateInitialStory(); // 실패 시 다시 시도
                } else {
                    if (makeStoryActivity != null) {
                        makeStoryActivity.runOnUiThread(() ->
                                Toast.makeText(makeStoryActivity, "도입부 생성 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            }
        });
    }

    // 선택지 생성
    public void generateChoices(int page) {
        String prompt = buildChoicePrompt(page);
        gemini.generateText(prompt, new Gemini.Callback() {
            @Override
            public void onSuccess(String text) {
                retryCount = 0; // 성공 시 재시도 횟수 초기화
                if (makeStoryActivity != null) {
                    makeStoryActivity.runOnUiThread(() -> {
                        makeStoryActivity.showChoices(text); //선택지를 화면에 띄움
                        Toast.makeText(makeStoryActivity, "선택지 생성 성공", Toast.LENGTH_SHORT).show(); // 성공 시 토스트 메시지
                    });
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (retryCount < MAX_RETRY_COUNT) {
                    retryCount++;
                    generateChoices(page); // 실패 시 다시 시도
                } else {
                    if (makeStoryActivity != null) {
                        makeStoryActivity.runOnUiThread(() ->
                                Toast.makeText(makeStoryActivity, "선택지 생성 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            }
        });
    }

    // 다음 스토리 부분 생성
    public void generateNextStoryPart(String choice, int page) {
        storySoFar += choice + " ";
        String prompt = buildNextStoryPrompt(page);
        gemini.generateText(prompt, new Gemini.Callback() {
            @Override
            public void onSuccess(String text) {
                retryCount = 0; // 성공 시 재시도 횟수 초기화
                storySoFar += text + " ";
                if (makeStoryActivity != null) {
                    makeStoryActivity.runOnUiThread(() -> {
                        makeStoryActivity.displayStoryText(text);
                        Toast.makeText(makeStoryActivity, "다음 스토리 생성 성공", Toast.LENGTH_SHORT).show(); // 성공 시 토스트 메시지
                    });
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (retryCount < MAX_RETRY_COUNT) {
                    retryCount++;
                    generateNextStoryPart(choice, page); // 실패 시 다시 시도
                } else {
                    if (makeStoryActivity != null) {
                        makeStoryActivity.runOnUiThread(() ->
                                Toast.makeText(makeStoryActivity, "다음 스토리 생성 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            }
        });
    }

    // 스토리 결말 생성
    public void generateEndStoryPart(String choice) {
        storySoFar += choice + " ";
        String prompt = buildEndStoryPrompt();
        gemini.generateText(prompt, new Gemini.Callback() {
            @Override
            public void onSuccess(String text) {
                retryCount = 0; // 성공 시 재시도 횟수 초기화
                storySoFar += text + " ";
                if (makeStoryActivity != null) {
                    makeStoryActivity.runOnUiThread(() -> {
                        makeStoryActivity.displayStoryText(text);
                        Toast.makeText(makeStoryActivity, "결말 생성 성공", Toast.LENGTH_SHORT).show(); // 성공 시 토스트 메시지
                    });
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (retryCount < MAX_RETRY_COUNT) {
                    retryCount++;
                    generateEndStoryPart(choice); // 실패 시 다시 시도
                } else {
                    if (makeStoryActivity != null) {
                        makeStoryActivity.runOnUiThread(() ->
                                Toast.makeText(makeStoryActivity, "결말 생성 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            }
        });
    }

    // 선택한 테마를 영어로 번역
    public void translateTheme(String theme, TranslationCallback callback) {
        String prompt = "Translate the following theme to English: " + theme + ". Please provide a concise, single-word or short-phrase answer.";
        gemini.generateText(prompt, new Gemini.Callback() {
            @Override
            public void onSuccess(String text) {
                callback.onSuccess(text.trim());
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    // 선택한 캐릭터를 영어로 번역
    public void translateCharacters(ArrayList<String> characters, TranslationCallback callback) {
        StringBuilder promptBuilder = new StringBuilder("Translate the following character names to English and prepend 'a cute ' before each noun. Separate the nouns with commas: ");
        for (int i = 0; i < characters.size(); i++) {
            promptBuilder.append(characters.get(i));
            if (i < characters.size() - 1) {
                promptBuilder.append(", ");
            }
        }
        String prompt = promptBuilder.toString();

        gemini.generateText(prompt, new Gemini.Callback() {
            @Override
            public void onSuccess(String text) {
                callback.onSuccess(text.trim());
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    private String buildInitialPrompt() { //도입부 생성
        return "동화 배경: " + theme + "\n" +
                "등장인물: " + characters + "\n" +
                "어린이가 대상인 동화의 도입부를 작성해 주세요. 이야기는 즐겁고, 긍정적이고, 흥미롭게 시작되어야 합니다." +
                "반드시 등장인물이 언급되어야 합니다. 2~3문장으로 작성해주세요. 문장이 끝나면 줄바꿈을 해주세요.";
    }

    private String buildChoicePrompt(int page) { //다음 이야기의 선택지 생성
        return  "등장인물: " + characters + "\n" +
                "현재 이야기: " + storySoFar + "\n" +
                "현재 페이지: 전체 6장 중 현재는 " + page+1 + "장 " +
                "이 이야기의 다음 문장을 작성해주세요. 현재 이야기와 자연스럽게 이어지도록 두 가지 버전으로 생성해 주세요. " +
                "각각 문장은 20자 이하의 짧은 1문장으로 작성해주세요. 문장 사이에는 '/'로 이어주세요." +
                "답변 예시: 공주는 그 음식을 먹었어요./공주는 그 음식을 버렸어요.";
    }

    private String buildNextStoryPrompt(int page) { // 선택지에 따른 다음 이야기 생성
        return  "등장인물: " + characters + "\n" +
                "현재 이야기: " + storySoFar + "\n" +
                "현재 페이지: 전체 6장 중 현재는 " + page+1 + "장 " +
                "다음에 이어질 내용: " + "\n" +
                "이 이야기의 다음 문장을 작성해주세요. 현재 이야기와 자연스럽게 이어지도록 만들어주세요." +
                "이야기는 즐겁고, 긍정적이고, 흥미롭게 시작되어야 합니다. 2~3문장으로 작성해주세요. 문장이 끝나면 줄바꿈을 해주세요.";
    }

    private String buildEndStoryPrompt() {
        return  "등장인물: " + characters + "\n" +
                "현재 이야기: " + storySoFar + "\n" +
                "다음에 이어질 내용: " + "\n" +
                "다음에 이어질 내용을 참고하여, 이야기를 완결내주세요. 현재 이야기와 자연스럽게 이어지도록 만들어주세요." +
                "이야기는 즐겁고, 긍정적이고, 교훈적이어야 합니다. 2~3문장으로 작성해주세요. 문장이 끝나면 줄바꿈을 해주세요.";
    }
}
