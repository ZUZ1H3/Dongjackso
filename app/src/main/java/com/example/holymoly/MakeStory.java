package com.example.holymoly;

import android.widget.Toast;

import java.util.ArrayList;

public class MakeStory {
    private Gemini gemini;
    private String theme;
    private String characters;
    private String storySoFar = "";
    private MakeStoryActivity makepage1Activity;

    public MakeStory(MakeStoryActivity activity, String theme, ArrayList<String> characters, Gemini gemini) {
        this.makepage1Activity = activity;
        this.theme = theme;
        this.characters = String.join(", ", characters);
        this.gemini = gemini;
    }

    //동화 도입부 생성
    public void generateInitialStory() {
        String prompt = buildInitialPrompt();
        gemini.generateText(prompt, new Gemini.Callback() {
            @Override
            public void onSuccess(String text) {
                storySoFar += text + " ";
                if (makepage1Activity != null) {
                    makepage1Activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            makepage1Activity.translate(text); //화면에 동화 출력하기위함
                            //generateChoices();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (makepage1Activity != null) {
                    makepage1Activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(makepage1Activity, "도입부 생성 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

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

    public void generateChoices() {
        String prompt = buildChoicePrompt();
        gemini.generateText(prompt, new Gemini.Callback() {
            @Override
            public void onSuccess(String text) {
                if (makepage1Activity != null) {
                    makepage1Activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            makepage1Activity.showChoices(text);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (makepage1Activity != null) {
                    makepage1Activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(makepage1Activity, "선택지 생성 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private String buildInitialPrompt() {
        return "동화 배경: " + theme + "\n" +
                "등장인물: " + characters + "\n" +
                "어린이가 대상인 동화의 도입부를 작성해 주세요. 이야기는 즐겁고, 긍정적이고, 흥미롭게 시작되어야 합니다." +
                "반드시 등장인물이 언급되어야 합니다. 2~3문장으로 작성해주세요. 문장이 끝나면 줄바꿈을 해주세요.";
    }

    private String buildChoicePrompt() {
        return "현재 이야기: " + storySoFar + "\n" +
                "이 이야기의 다음 문장을 작성해주세요. 현재 이야기와 자연스럽게 이어지도록 두 가지 버전으로 생성해 주세요. " +
                "각각 문장은 20자 이하의 짧은 1문장으로 작성해주세요. 문장 사이에는 '/'로 이어주세요." +
                "답변 예시: 공주는 그 음식을 먹었어요./공주는 그 음식을 버렸어요.";
    }
}
