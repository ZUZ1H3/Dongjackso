package com.example.holymoly;

import java.util.HashMap;
import java.util.Map;

public class CharacterData {
    // 테마별 캐릭터 이름 및 이미지 경로 저장
    public static Map<String, CharacterInfo[]> themeCharacterMap = new HashMap<>();

    static {
        // 예제 데이터: 테마 "fairytale"와 "adventure"에 대한 캐릭터 이름 및 이미지 경로
        themeCharacterMap.put("바다", new CharacterInfo[]{
                new CharacterInfo("인어공주", R.drawable.ic_sea1),
                new CharacterInfo("왕자", R.drawable.ic_sea2),
                new CharacterInfo("돌고래", R.drawable.ic_sea3),
                new CharacterInfo("상어", R.drawable.ic_sea4),
                new CharacterInfo("고래", R.drawable.ic_sea5),
                new CharacterInfo("바다거북", R.drawable.ic_sea6),
                new CharacterInfo("어부", R.drawable.ic_sea7),
                new CharacterInfo("해적", R.drawable.ic_sea8),
                new CharacterInfo("해파리", R.drawable.ic_sea9),
                new CharacterInfo("?", R.drawable.ic_customcharacter)
        });

        themeCharacterMap.put("궁전", new CharacterInfo[]{
                new CharacterInfo("공주", R.drawable.ic_castle1),
                new CharacterInfo("왕자", R.drawable.ic_castle2),
                new CharacterInfo("왕", R.drawable.ic_castle3),
                new CharacterInfo("여왕", R.drawable.ic_castle4),
                new CharacterInfo("고양이", R.drawable.ic_castle5),
                new CharacterInfo("기사", R.drawable.ic_castle6),
                new CharacterInfo("마법사", R.drawable.ic_castle7),
                new CharacterInfo("난쟁이", R.drawable.ic_castle8),
                new CharacterInfo("요리사", R.drawable.ic_castle9),
                new CharacterInfo("?", R.drawable.ic_customcharacter)
        });
        themeCharacterMap.put("숲", new CharacterInfo[]{
                new CharacterInfo("부엉이", R.drawable.ic_forest1),
                new CharacterInfo("도깨비", R.drawable.ic_forest2),
                new CharacterInfo("나무꾼", R.drawable.ic_forest3),
                new CharacterInfo("여우", R.drawable.ic_forest4),
                new CharacterInfo("곰", R.drawable.ic_forest5),
                new CharacterInfo("요정", R.drawable.ic_forest6),
                new CharacterInfo("아이", R.drawable.ic_forest7),
                new CharacterInfo("토끼", R.drawable.ic_forest8),
                new CharacterInfo("새", R.drawable.ic_forest9),
                new CharacterInfo("?", R.drawable.ic_customcharacter)
        });

        themeCharacterMap.put("마을", new CharacterInfo[]{
                new CharacterInfo("소년", R.drawable.ic_village1),
                new CharacterInfo("소녀", R.drawable.ic_village2),
                new CharacterInfo("엄마", R.drawable.ic_village3),
                new CharacterInfo("아빠", R.drawable.ic_village4),
                new CharacterInfo("강아지", R.drawable.ic_village5),
                new CharacterInfo("할아버지", R.drawable.ic_village6),
                new CharacterInfo("할머니", R.drawable.ic_village7),
                new CharacterInfo("다람쥐", R.drawable.ic_village8),
                new CharacterInfo("농부", R.drawable.ic_village9),
                new CharacterInfo("?", R.drawable.ic_customcharacter)
        });

        themeCharacterMap.put("우주", new CharacterInfo[]{
                new CharacterInfo("별", R.drawable.ic_space1),
                new CharacterInfo("우주인", R.drawable.ic_space2),
                new CharacterInfo("우주괴물", R.drawable.ic_space3),
                new CharacterInfo("외계인", R.drawable.ic_space4),
                new CharacterInfo("달토끼", R.drawable.ic_space5),
                new CharacterInfo("별의 요정", R.drawable.ic_space6),
                new CharacterInfo("혜성", R.drawable.ic_space7),
                new CharacterInfo("시간여행자", R.drawable.ic_space8),
                new CharacterInfo("탐험가", R.drawable.ic_space9),
                new CharacterInfo("?", R.drawable.ic_customcharacter)
        });

    }

    // 캐릭터 정보 클래스
    public static class CharacterInfo {
        String name;
        int imageResId;

        public CharacterInfo(String name, int imageResId) {
            this.name = name;
            this.imageResId = imageResId;
        }
    }
}
