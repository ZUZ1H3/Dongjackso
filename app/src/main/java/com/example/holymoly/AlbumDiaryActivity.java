package com.example.holymoly;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class AlbumDiaryActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView calendar, leftTV, rightTV;
    private ImageView leftIV, rightIV, leftWT, rightWT;
    private ImageButton back, next, stop;
    private long backPressedTime = 0;

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();
    private String uid = user.getUid();

    private List<Diary> diaries = new ArrayList<>();
    private List<Diary> filteredDiaries = new ArrayList<>();
    private int currentIndex = 0;
    private int month;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_diary);

        // UI 요소 초기화
        calendar = findViewById(R.id.calendar);
        leftTV = findViewById(R.id.tv_left);
        rightTV = findViewById(R.id.tv_right);
        leftIV = findViewById(R.id.leftImage);
        rightIV = findViewById(R.id.rightImage);
        leftWT = findViewById(R.id.leftWeather);
        rightWT = findViewById(R.id.rightWeather);
        back = findViewById(R.id.backPage);
        next = findViewById(R.id.nextPage);
        stop = findViewById(R.id.ib_stopReading);

        // 클릭 리스너 설정
        leftIV.setOnClickListener(this);
        rightIV.setOnClickListener(this);
        calendar.setOnClickListener(this);
        back.setOnClickListener(this);
        next.setOnClickListener(this);
        stop.setOnClickListener(this);

        // 현재 날짜를 구해 캘린더에 표시
        Calendar cal = Calendar.getInstance();
        month = cal.get(Calendar.MONTH) + 1;
        calendar.setText(month + "월");

        // 이미지, 날짜, 날씨를 가져옴
        loadAll();
    }

    @Override
    public void onClick(View v) {
        sound();
        if (v.getId() == R.id.backPage) {  // 이전 페이지로 이동
            if (currentIndex > 0) {
                currentIndex -= 2;
                if (currentIndex < 0) {
                    // 현재 월의 첫 페이지에서 이전 페이지로 이동할 경우
                    if (month > 1) {
                        int previousMonth = month - 1;
                        if (filterDiariesByMonth(previousMonth)) {
                            month = previousMonth;
                            calendar.setText(month + "월");
                            currentIndex = filteredDiaries.size() - 2; // 마지막 페이지로 설정
                            displayImages();
                        } else {
                            Toast.makeText(this, "일기가 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "첫 번째 페이지입니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    displayImages();
                }
            } else {
                // 현재 월의 첫 페이지에서 이전 페이지로 이동할 경우
                if (month > 1) {
                    int previousMonth = month - 1;
                    if (filterDiariesByMonth(previousMonth)) {
                        month = previousMonth;
                        calendar.setText(month + "월");
                        currentIndex = filteredDiaries.size() - 2; // 마지막 페이지로 설정
                        displayImages();
                    } else {
                        Toast.makeText(this, "일기가 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "첫 번째 페이지입니다.", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (v.getId() == R.id.nextPage) { // 다음 페이지로 이동
            if (currentIndex + 2 < filteredDiaries.size()) {
                currentIndex += 2;
                displayImages();
            } else {
                // 현재 월의 마지막 페이지에서 다음 페이지로 이동할 경우
                if (month < 12) {
                    int nextMonth = month + 1;
                    if (filterDiariesByMonth(nextMonth)) {
                        month = nextMonth;
                        calendar.setText(month + "월");
                        currentIndex = 0; // 첫 페이지로 설정
                        displayImages();
                    } else {
                        Toast.makeText(this, "일기가 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "마지막 페이지입니다.", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (v.getId() == R.id.ib_stopReading) {  // 종료 버튼
            if (System.currentTimeMillis() - backPressedTime >= 2000) {
                backPressedTime = System.currentTimeMillis();
                Toast.makeText(this, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, Home2Activity.class);
                startActivity(intent);
            } else {
                finish(); // 현재 액티비티 종료
            }
        } else if (v.getId() == R.id.leftImage) { // 왼쪽 이미지
            // yyyyMMdd 형식의 날짜 가져오기
            String date = filteredDiaries.get(currentIndex).getDate();
            Intent intent = new Intent(this, MakeDiaryActivity.class);
            intent.putExtra("date", date);
            startActivity(intent);
        } else if (v.getId() == R.id.rightImage) { // 오른쪽 이미지
            // 이미지와 텍스트가 비어있는 경우
            if (rightTV.getText().toString().isEmpty()) {
                Toast.makeText(this, "내일 일기를 작성하세요.", Toast.LENGTH_SHORT).show();
            } else {
                // yyyyMMdd 형식의 날짜 가져오기
                String date = filteredDiaries.get(currentIndex + 1).getDate();
                Intent intent = new Intent(this, MakeDiaryActivity.class);
                intent.putExtra("date", date);
                startActivity(intent);
            }
        } else if (v.getId() == R.id.calendar) { // 달력 클릭 시 월 변경
            showMonthPickerDialog();
        }
    }

    // Storage에서 이미지와 날짜 가져옴
    private void loadAll() {
        storageRef.child("diaries").listAll().addOnSuccessListener(result -> {
            List<StorageReference> items = result.getItems();
            List<String> pendingUris = new ArrayList<>();

            for (StorageReference item : items) {
                String fileName = item.getName();
                // user ID로 시작하고 png 파일로 끝나는 파일들만
                if (fileName.startsWith(user.getUid()) && fileName.endsWith(".png")) {
                    // '_'로 분리해서 파일 정렬
                    String[] parts = fileName.split("_");
                    if (parts.length > 1) {
                        String dateString = parts[1].replace(".png", "");
                        pendingUris.add(dateString); // 처리 대기 중인 URI를 저장
                        item.getDownloadUrl().addOnSuccessListener(uri -> {
                            diaries.add(new Diary(dateString, uri.toString()));
                            pendingUris.remove(dateString); // 처리된 URI를 제거
                            if (pendingUris.isEmpty()) {
                                Collections.sort(diaries, (entry1, entry2) -> entry1.getDate().compareTo(entry2.getDate()));
                                filterDiariesByMonth(month); // 초기 필터링
                                displayImages();
                            }
                        });
                    }
                }
            }
        });
    }

    // 지정된 월에 해당하는 일기들로 필터링
    private boolean filterDiariesByMonth(int monthToFilter) {
        filteredDiaries.clear();
        for (Diary diary : diaries) {
            if (Integer.parseInt(diary.getDate().substring(4, 6)) == monthToFilter) {
                filteredDiaries.add(diary);
            }
        }
        return !filteredDiaries.isEmpty(); // 필터링된 결과가 있을 경우 true 반환
    }

    // 현재 인덱스에 해당하는 이미지와 날짜를 표시
    private void displayImages() {
        if (currentIndex < filteredDiaries.size()) {
            loadImageIntoView(filteredDiaries.get(currentIndex).getImageUrl(), leftIV, leftTV, filteredDiaries.get(currentIndex).getDate(), leftWT);
        } else {
            clearLeftView(); // 이미지가 없을 경우 초기화
        }

        if (currentIndex + 1 < filteredDiaries.size()) {
            loadImageIntoView(filteredDiaries.get(currentIndex + 1).getImageUrl(), rightIV, rightTV, filteredDiaries.get(currentIndex + 1).getDate(), rightWT);
        } else {
            clearRightView(); // 이미지가 없을 경우 초기화
        }
    }

    // 이미지와 텍스트를 지정된 뷰에 표시
    private void loadImageIntoView(String imageUrl, ImageView imageView, TextView textView, String date, ImageView weather) {
        Glide.with(this).load(imageUrl).into(imageView);
        textView.setText(formatDateToText(date));
        loadWeather(date, weather);
    }

    // 날짜를 "dd 요일" 형식으로 변환
    private String formatDateToText(String dateString) {
        try {
            int year = Integer.parseInt(dateString.substring(0, 4));
            int month = Integer.parseInt(dateString.substring(4, 6)) - 1;
            int day = Integer.parseInt(dateString.substring(6, 8));

            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

            String[] daysOfWeek = {"일", "월", "화", "수", "목", "금", "토"};

            return day + " " + daysOfWeek[dayOfWeek - 1];
        } catch (NumberFormatException e) {
            return "날짜 오류";
        }
    }

    // 날씨 아이콘 설정
    private void setWeatherIcon(int selected, ImageView weather) {
        if (selected == R.id.ib_sunny) weather.setImageResource(R.drawable.weather_sunny);
        else if (selected == R.id.ib_suncloudy) weather.setImageResource(R.drawable.weather_suncloudy);
        else if (selected == R.id.ib_cloudy) weather.setImageResource(R.drawable.weather_cloudy);
        else if (selected == R.id.ib_rainy) weather.setImageResource(R.drawable.weather_rainy);
        else if (selected == R.id.ib_snowy) weather.setImageResource(R.drawable.weather_snowy);
    }
    // 날씨 불러오기
    private void loadWeather(String date, ImageView weather) {
        DocumentReference weatherRef = db.collection("weather").document(uid).collection("dates").document(date);

        weatherRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            int selectedId = document.getLong("selectedId").intValue();
                            // 날씨 아이콘 설정
                            setWeatherIcon(selectedId, weather);
                        }
                    }
                });
    }

    // Diary 클래스 정의
    private static class Diary {
        private final String date;
        private final String imageUrl;

        public Diary(String date, String imageUrl) {
            this.date = date;
            this.imageUrl = imageUrl;
        }

        public String getDate() {
            return date;
        }

        public String getImageUrl() {
            return imageUrl;
        }
    }

    // leftIV와 leftTV를 초기화
    private void clearLeftView() {
        leftIV.setImageResource(android.R.color.transparent);
        leftTV.setText("");
        leftWT.setImageResource(android.R.color.transparent);
    }

    // rightIV와 rightTV를 초기화
    private void clearRightView() {
        rightIV.setImageResource(android.R.color.transparent);
        rightTV.setText("");
        rightWT.setImageResource(android.R.color.transparent);
    }

    // 달 클릭 시 다이얼로그 표시
    private void showMonthPickerDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_month, null);

        NumberPicker monthPicker = dialogView.findViewById(R.id.numberPickerMonth);
        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setValue(month);

        // NumberPicker의 텍스트 색상과 크기 조정
        setNumberPickerTextStyle(monthPicker, "#F383A9", 32);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("월 선택하세요");
        builder.setView(dialogView);
        builder.setPositiveButton("확인", (dialog, which) -> {
            int selectedMonth = monthPicker.getValue();
            if (filterDiariesByMonth(selectedMonth)) {
                month = selectedMonth;
                calendar.setText(month + "월");
                currentIndex = 0; // 페이지를 처음으로 설정
                displayImages();
            } else {
                Toast.makeText(this, "일기가 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("취소", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // NumberPicker의 텍스트 색상과 크기 설정 메서드
    private void setNumberPickerTextStyle(NumberPicker numberPicker, String color, float textSize) {
        try {
            // TextView 필드 이름
            Field field = NumberPicker.class.getDeclaredField("mInputText");
            field.setAccessible(true);

            TextView inputText = (TextView) field.get(numberPicker);
            inputText.setTextColor(Color.parseColor(color)); // 텍스트 색상 설정
            inputText.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize); // 텍스트 크기 설정
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    // 효과음
    public void sound() {
        Intent intent = new Intent(this, SoundService.class);
        startService(intent);
    }
}
