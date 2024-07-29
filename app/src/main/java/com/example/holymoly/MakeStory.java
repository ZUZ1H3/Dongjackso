package com.example.holymoly;

public class MakeStory {
    private Gemini gemini;
    private String theme;
    private String characters;
    private String storySoFar = "";
    private Makepage1Activity makepage1Activity; // Makepage1Activity를 멤버 변수로 선언

    public MakeStory(Makepage1Activity activity, String theme, String characters) {
        this.makepage1Activity = activity;
        this.theme = theme;
        this.characters = characters;
        this.gemini = new Gemini(); // Gemini 인스턴스 생성
    }

    // 동화의 도입부 프롬프트
    public String buildInitialPrompt() {
        return "동화 배경: " + theme + "\n" +
                "등장인물: " + characters + "\n" +
                "어린이가 대상인 동화의 도입부를 작성해 주세요. 이야기는 즐겁고, 긍정적이고, 흥미롭게 시작되어야 합니다." +
                "배경은 더 구체적으로 설정하면 좋습니다. 등장인물도 소개되어야 합니다. 2~3문장으로 작성해주세요. 문장이 끝나면 줄바꿈을 해주세요.";
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



    //동화 도입부 생성
    public void generateInitialStory() {
        String prompt = buildInitialPrompt();
        gemini.generateText(prompt, new Gemini.Callback() {
            @Override
            public void onSuccess(String text) {
                handleInitialStorySuccess(text);
            }

            @Override
            public void onFailure(Throwable t) {
                // 실패 시 처리
            }
        });
    }





    //생성된 동화 도입부를 들고 makepage1Activity로 이동한다.
    private void handleInitialStorySuccess(String text) {
        if (makepage1Activity != null) {
            makepage1Activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    makepage1Activity.onStoryGenerated(text);
                }
            });
        }
    }

    // 동화의 나머지 부분을 생성하는 메서드들 (예: generateMoreStory, generateIncident, generateChoices 등) 추가 가능
}
