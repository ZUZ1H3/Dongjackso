package com.example.holymoly;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Karlo {
    private static final String KARLO_API_KEY = "6191d7ae7edbee42dba70ef2ce2d7643"; // 실제 API 키로 대체하세요.
    private static final String API_URL = "https://api.kakaobrain.com/v2/inference/karlo/t2i";

    public interface Callback {
        void onSuccess(String imageUrl);
        void onFailure(Exception e);
    }

    private int width;
    private int height;

    // 기본 생성자, 기본값을 사용할 때 사용
    public Karlo() {
        this.width = 768; // 기본 가로 크기
        this.height = 768; // 기본 세로 크기
    }

    // 생성자 오버로드, 사용자 정의 가로 및 세로를 지정할 수 있도록
    public Karlo(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void requestImage(String prompt, Callback callback) {
        new Thread(() -> {
            try {
                String imageUrl = requestImageFromKarlo(prompt);
                callback.onSuccess(imageUrl);
            } catch (Exception e) {
                callback.onFailure(e);
            }
        }).start();
    }

    private String requestImageFromKarlo(String prompt) throws Exception {
        JSONObject json = new JSONObject();
        json.put("version", "v2.1");
        json.put("prompt", prompt);
        json.put("negative_prompt", "");
        json.put("height", height);
        json.put("width", width);
        json.put("image_quality", 100);

        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "KakaoAK " + KARLO_API_KEY);
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = json.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        InputStream responseStream = conn.getInputStream();
        StringBuilder responseStrBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream, "utf-8"))) {
            String responseLine;
            while ((responseLine = reader.readLine()) != null) {
                responseStrBuilder.append(responseLine.trim());
            }
        }

        JSONObject responseObject = new JSONObject(responseStrBuilder.toString());
        JSONArray images = responseObject.getJSONArray("images");
        return images.getJSONObject(0).getString("image");
    }
}
