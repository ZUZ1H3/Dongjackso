package com.example.holymoly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AchieveActivity extends AppCompatActivity implements UserInfoLoader {
    private static final String PREFS_NAME = "AchievementPrefs";
    private SharedPreferences sharedPreferences;

    private RadioGroup rgTrophyCategory;
    private ImageButton ibCloseWindow;
    private ImageView profile;
    private TextView name;

    private UserInfo userInfo = new UserInfo();

    //완료된 동화 테마 개수
    private int seaTrophyCount = 0, forestTrophyCount = 1,
                castleTrophyCount = 3, villageTrophyCount = 5,
                universeTrophyCount = 10, desertTrophyCount = 15,
                customTrophyCount = 20;

    //동화 업적 텍스트뷰
    private TextView tvSeaTrophy, tvSeaTrophyPercent,
            tvForestTrophy, tvForestTrophyPercent,
            tvCastleTrophy, tvCastleTrophyPercent,
            tvVillageTrophy, tvVillageTrophyPercent,
            tvUniverseTrophy, tvUniverseTrophyPercent,
            tvDesertTrophy, tvDesertTrophyPercent,
            tvCustomTrophy, tvCustomTrophyPercent;

    //목표 개수 리스트 설정
    private int[] trophyGoals = {1, 5, 10, 20, 50};
    private int seaTrophyIndex=0, forestTrophyIndex=0, castleTrophyIndex=0, villageTrophyIndex=0,
                universeTrophyIndex=0, desertTrophyIndex=0, customTrophyIndex=0;
    private ImageButton seaButton, forestButton, villageButton, castleButton, universeButton,
                        desertButton, customButton;

    //작가호칭 - 프로그래스바 목표
    private int totalTrophyCount=0;
    private final int[] totalTrophyGoals = {1, 5, 10, 50};
    private ProgressBar[] pbTrophy = new ProgressBar[4];
    private TextView[] tvTrophy = new TextView[4];
    private ImageButton[] ibTrophy = new ImageButton[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achieve);

        name = findViewById(R.id.mini_name);
        profile = findViewById(R.id.mini_profile);
        loadUserInfo(profile, name);

        //UI설정
        settingUI();

        // SharedPreferences에서 값을 로드
        loadTrophyCounts();
        //UI설정 업데이트
        updateUIForAllThemes();
        //프로그래스바 업데이트
        updateProgressBars();

        //디폴트 상태 설정 - 처음에 키면 동화업적이 띄워져있는걸로함
        findViewById(R.id.fairytaleAchievementLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.writerappellationLayout).setVisibility(View.GONE);

        // 작가 호칭 보상 버튼을 초기에는 비활성화
        for (ImageButton btn : ibTrophy) {
            btn.setEnabled(false);
        }

        Button resetButton = findViewById(R.id.resetButton);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 초기화 메서드 호출
                resetTrophyCounts();
                Toast.makeText(AchieveActivity.this, "모든 데이터가 초기화되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        //동화업적, 작가호칭 선택 리스너
        rgTrophyCategory.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.rb_trophy_fairytaleachievement) {
                    findViewById(R.id.fairytaleAchievementLayout).setVisibility(View.VISIBLE);
                    findViewById(R.id.writerappellationLayout).setVisibility(View.GONE);
                }
                else if(checkedId == R.id.rb_trophy_writerappellation) {
                    findViewById(R.id.fairytaleAchievementLayout).setVisibility(View.GONE);
                    findViewById(R.id.writerappellationLayout).setVisibility(View.VISIBLE);
                }
            }
        });

        //X버튼 리스너
        ibCloseWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AchieveActivity.this, TrophyActivity.class);
                startActivity(intent);
            }
        });

        seaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 버튼 클릭 시 onRewardButtonClicked 호출
                onRewardButtonClicked("바다");
            }
        });

        forestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 버튼 클릭 시 onRewardButtonClicked 호출
                onRewardButtonClicked("숲");
            }
        });

        castleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 버튼 클릭 시 onRewardButtonClicked 호출
                onRewardButtonClicked("궁전");
            }
        });

        villageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 버튼 클릭 시 onRewardButtonClicked 호출
                onRewardButtonClicked("마을");
            }
        });

        universeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 버튼 클릭 시 onRewardButtonClicked 호출
                onRewardButtonClicked("우주");
            }
        });

        desertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 버튼 클릭 시 onRewardButtonClicked 호출
                onRewardButtonClicked("사막");
            }
        });

        customButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 버튼 클릭 시 onRewardButtonClicked 호출
                onRewardButtonClicked("커스텀");
            }
        });

        // 버튼 클릭 리스너 설정
        //for (int i = 0; i < totalTrophyGoals.length; i++) {
        //    final int index = i;
        //    ibTrophy[i].setOnClickListener(new View.OnClickListener() {
        //        @Override
        //        public void onClick(View v) {
        //            handleTrophyReward(index);
        //        }
        //    });
        //}

        ibTrophy[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleTrophyReward(0);
            }
        });

        ibTrophy[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleTrophyReward(1);
            }
        });

        ibTrophy[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleTrophyReward(2);
            }
        });

        ibTrophy[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleTrophyReward(3);
            }
        });
    }

    // SharedPreferences에서 TrophyCounts 로드
    private void loadTrophyCounts() {
        SharedPreferences sharedPreferences = getSharedPreferences("TrophyPrefs", MODE_PRIVATE);

        seaTrophyCount = sharedPreferences.getInt("seaTrophyCount", 0); // 여기서 0은 기본값이지만 기본적으로 설정된 값으로 바꿔줌
        forestTrophyCount = sharedPreferences.getInt("forestTrophyCount", 1);
        castleTrophyCount = sharedPreferences.getInt("castleTrophyCount", 3);
        villageTrophyCount = sharedPreferences.getInt("villageTrophyCount", 5);
        universeTrophyCount = sharedPreferences.getInt("universeTrophyCount", 10);
        desertTrophyCount = sharedPreferences.getInt("desertTrophyCount", 15);
        customTrophyCount = sharedPreferences.getInt("customTrophyCount", 20);

        seaTrophyIndex = sharedPreferences.getInt("seaTrophyIndex", 0);
        forestTrophyIndex = sharedPreferences.getInt("forestTrophyIndex", 0);
        castleTrophyIndex = sharedPreferences.getInt("castleTrophyIndex", 0);
        villageTrophyIndex = sharedPreferences.getInt("villageTrophyIndex", 0);
        universeTrophyIndex = sharedPreferences.getInt("universeTrophyIndex", 0);
        desertTrophyIndex = sharedPreferences.getInt("desertTrophyIndex", 0);
        customTrophyIndex = sharedPreferences.getInt("customTrophyIndex", 0);

        totalTrophyCount = sharedPreferences.getInt("totalTrophyCount", 0);
    }

    // 업데이트된 TrophyCounts를 SharedPreferences에 저장
    private void saveTrophyCounts() {
        SharedPreferences sharedPreferences = getSharedPreferences("TrophyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("seaTrophyCount", seaTrophyCount);
        editor.putInt("forestTrophyCount", forestTrophyCount);
        editor.putInt("castleTrophyCount", castleTrophyCount);
        editor.putInt("villageTrophyCount", villageTrophyCount);
        editor.putInt("universeTrophyCount", universeTrophyCount);
        editor.putInt("desertTrophyCount", desertTrophyCount);
        editor.putInt("customTrophyCount", customTrophyCount);

        editor.putInt("seaTrophyIndex", seaTrophyIndex);
        editor.putInt("forestTrophyIndex", forestTrophyIndex);
        editor.putInt("castleTrophyIndex", castleTrophyIndex);
        editor.putInt("villageTrophyIndex", villageTrophyIndex);
        editor.putInt("universeTrophyIndex", universeTrophyIndex);
        editor.putInt("desertTrophyIndex", desertTrophyIndex);
        editor.putInt("customTrophyIndex", customTrophyIndex);

        editor.putInt("totalTrophyCount", totalTrophyCount);

        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTrophyCounts(); // Load state
        updateUIForAllThemes(); // Update UI
        updateProgressBars();
    }

    private void updateUIForAllThemes() {
        updateUI("바다");
        updateUI("숲");
        updateUI("궁전");
        updateUI("마을");
        updateUI("우주");
        updateUI("사막");
        updateUI("커스텀");
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveTrophyCounts(); // Activity가 일시 정지될 때 값을 저장
    }

    public void handleTrophyReward(int trophyIndex) {
        if(ibTrophy[trophyIndex].isEnabled()) { //활성화된 상태에서 눌렀다면
            //작가호칭 보상
            Toast.makeText(this, "작가 호칭 선택완료!", Toast.LENGTH_SHORT).show();

            // 상태 저장
            saveTrophyRewardStatus(trophyIndex);

            //UI 업데이트
            updateProgressBars();

            for(int i=0; i < totalTrophyGoals.length; i++) {
                int goal = totalTrophyGoals[i];

                //목표 달성 시 이미지 버튼 활성화 + 배경 이미지 바꿈
                if (totalTrophyCount >= goal) {
                    ibTrophy[i].setEnabled(true);
                    ibTrophy[i].setBackgroundResource(R.drawable.ic_btn_appellation);
                }
                // 잠금해제 이미지로 변경
                else {
                    ibTrophy[i].setEnabled(false);
                    ibTrophy[i].setBackgroundResource(R.drawable.ic_btn_appellation_locked); // 잠금 이미지로 변경
                }
            }

            ibTrophy[trophyIndex].setEnabled(false);
            ibTrophy[trophyIndex].setBackgroundResource(R.drawable.ic_btn_appellation_done); // 완료 이미지로 변경
        }
    }

    private void saveTrophyRewardStatus(int trophyIndex) {
        SharedPreferences sharedPreferences = getSharedPreferences("TrophyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // 저장할 상태
        editor.putBoolean("trophyReward" + trophyIndex, true);

        editor.apply();
    }

    private boolean isTrophyRewardClaimed(int trophyIndex) {
        SharedPreferences sharedPreferences = getSharedPreferences("TrophyPrefs", MODE_PRIVATE);
        return sharedPreferences.getBoolean("trophyReward" + trophyIndex, false);
    }

    public void updateProgressBars() {
        for(int i=0; i < totalTrophyGoals.length; i++) {
            int goal = totalTrophyGoals[i];
            int current = Math.min(totalTrophyCount, goal); //둘 중 작은걸 선택 - 이미 목표치를 채우면 100퍼만 되게끔하기 위해 사용
            int percentage = (int) ((current / (float) goal) * 100); //퍼센트 계산 (프로그래스바)

            //프로그래스바 없데이트
            pbTrophy[i].setProgress(percentage);

            //텍스트뷰 업데이트
            String text = String.format("트로피 %d / %d", current, goal);
            tvTrophy[i].setText(text);

            //목표 달성 시 이미지 버튼 활성화 + 배경 이미지 바꿈
            if(totalTrophyCount >= goal) {
                if(isBackgroundImage(ibTrophy[i], R.drawable.ic_btn_appellation_done)) {
                    //이미 완료선택한거면
                }

                else {
                    ibTrophy[i].setEnabled(true);
                    ibTrophy[i].setBackgroundResource(R.drawable.ic_btn_appellation);
                }
            }
            // 잠금해제 이미지로 변경
            else {
                ibTrophy[i].setEnabled(false);
                ibTrophy[i].setBackgroundResource(R.drawable.ic_btn_appellation_locked); // 잠금 이미지로 변경
            }
        }
    }

    public boolean isBackgroundImage(ImageButton button, int drawableId) {
        // 현재 배경 Drawable 객체 가져오기
        Drawable currentDrawable = button.getBackground();

        // 비교할 Drawable 객체 가져오기
        Drawable drawableToCompare = ContextCompat.getDrawable(this, drawableId);

        // Drawable 객체의 id를 비교하여 일치 여부 확인
        return currentDrawable != null && drawableToCompare != null && currentDrawable.getConstantState().equals(drawableToCompare.getConstantState());
    }

    public void updateUI(String thema) {
        int currentCount = 0;
        int currentIndex = 0;
        TextView goalTextView = null;
        TextView percentageTextView = null;

        if (thema.equals("바다")) {
            currentCount = seaTrophyCount;
            currentIndex = seaTrophyIndex;
            goalTextView = tvSeaTrophy;
            percentageTextView = tvSeaTrophyPercent;
        } else if (thema.equals("숲")) {
            currentCount = forestTrophyCount;
            currentIndex = forestTrophyIndex;
            goalTextView = tvForestTrophy;
            percentageTextView = tvForestTrophyPercent;
        } else if (thema.equals("궁전")) {
            currentCount = castleTrophyCount;
            currentIndex = castleTrophyIndex;
            goalTextView = tvCastleTrophy;
            percentageTextView = tvCastleTrophyPercent;
        } else if (thema.equals("마을")) {
            currentCount = villageTrophyCount;
            currentIndex = villageTrophyIndex;
            goalTextView = tvVillageTrophy;
            percentageTextView = tvVillageTrophyPercent;
        } else if (thema.equals("우주")) {
            currentCount = universeTrophyCount;
            currentIndex = universeTrophyIndex;
            goalTextView = tvUniverseTrophy;
            percentageTextView = tvUniverseTrophyPercent;
        } else if (thema.equals("사막")) {
            currentCount = desertTrophyCount;
            currentIndex = desertTrophyIndex;
            goalTextView = tvDesertTrophy;
            percentageTextView = tvDesertTrophyPercent;
        } else if (thema.equals("커스텀")) {
            currentCount = customTrophyCount;
            currentIndex = customTrophyIndex;
            goalTextView = tvCustomTrophy;
            percentageTextView = tvCustomTrophyPercent;
        }

        if (currentCount >= trophyGoals[currentIndex]) {
            percentageTextView.setText("보상 받기");
        } else {
            int percentage = (currentCount * 100) / trophyGoals[currentIndex];
            percentageTextView.setText(percentage + "%");
        }

        String goalText = String.format("%s 동화 %d개 만들기", thema, trophyGoals[currentIndex]);
        goalTextView.setText(goalText);
    }

    public void onRewardButtonClicked(String thema) {
        TextView percentageTextView = getPercentageTextView(thema);

        if (percentageTextView != null && percentageTextView.getText().toString().equals("보상 받기")) {
            totalTrophyCount++;
            updateTrophyIndex(thema);
            saveTrophyCounts(); // Save state
            updateProgressBars();
        }
        updateUI(thema);
    }

    private TextView getPercentageTextView(String thema) {
        switch (thema) {
            case "바다": return tvSeaTrophyPercent;
            case "숲": return tvForestTrophyPercent;
            case "궁전": return tvCastleTrophyPercent;
            case "마을": return tvVillageTrophyPercent;
            case "우주": return tvUniverseTrophyPercent;
            case "사막": return tvDesertTrophyPercent;
            case "커스텀": return tvCustomTrophyPercent;
            default: return null;
        }
    }

    private void updateTrophyIndex(String thema) {
        switch (thema) {
            case "바다": seaTrophyIndex++; break;
            case "숲": forestTrophyIndex++; break;
            case "궁전": castleTrophyIndex++; break;
            case "마을": villageTrophyIndex++; break;
            case "우주": universeTrophyIndex++; break;
            case "사막": desertTrophyIndex++; break;
            case "커스텀": customTrophyIndex++; break;
        }
    }

    public void settingUI() {
        //동화업적, 작가호칭 선택 라디오그룹
        rgTrophyCategory = findViewById(R.id.rg_trophyCategory);

        //이미지버튼
        ibCloseWindow = findViewById(R.id.ib_closeWindow);

        //동화업적 텍스트뷰 연결
        tvSeaTrophy = findViewById(R.id.tv_seaTrophy);
        tvSeaTrophyPercent = findViewById(R.id.tv_seaTrophyPercent);
        tvForestTrophy = findViewById(R.id.tv_forestTrophy);
        tvForestTrophyPercent = findViewById(R.id.tv_forestTrophyPercent);
        tvCastleTrophy = findViewById(R.id.tv_castleTrophy);
        tvCastleTrophyPercent = findViewById(R.id.tv_castleTrophyPercent);
        tvVillageTrophy = findViewById(R.id.tv_villageTrophy);
        tvVillageTrophyPercent = findViewById(R.id.tv_villageTrophyPercent);
        tvUniverseTrophy = findViewById(R.id.tv_universeTrophy);
        tvUniverseTrophyPercent = findViewById(R.id.tv_universeTrophyPercent);
        tvDesertTrophy = findViewById(R.id.tv_desertTrophy);
        tvDesertTrophyPercent = findViewById(R.id.tv_desertTrophyPercent);
        tvCustomTrophy = findViewById(R.id.tv_customTrophy);
        tvCustomTrophyPercent = findViewById(R.id.tv_customTrophyPercent);

        //보상버튼
        seaButton = findViewById(R.id.ib_seaTrophy);
        forestButton = findViewById(R.id.ib_forestTrophy);
        castleButton = findViewById(R.id.ib_castleTrophy);
        villageButton = findViewById(R.id.ib_villageTrophy);
        universeButton = findViewById(R.id.ib_universeTrophy);
        desertButton = findViewById(R.id.ib_desertTrophy);
        customButton = findViewById(R.id.ib_customTrophy);

        //작가호칭 보상 이미지 버튼
        ibTrophy[0] = findViewById(R.id.ib_littleWriter);
        ibTrophy[1] = findViewById(R.id.ib_beginnerWriter);
        ibTrophy[2] = findViewById(R.id.ib_veteranWriter);
        ibTrophy[3] = findViewById(R.id.ib_masterWriter);

        //작가호칭 프로그래스바
        pbTrophy[0] = findViewById(R.id.pb_trophyCount1);
        pbTrophy[1] = findViewById(R.id.pb_trophyCount5);
        pbTrophy[2] = findViewById(R.id.pb_trophyCount10);
        pbTrophy[3] = findViewById(R.id.pb_trophyCount50);

        //작가호칭 텍스트뷰
        tvTrophy[0] = findViewById(R.id.tv_trophyCount1);
        tvTrophy[1] = findViewById(R.id.tv_trophyCount5);
        tvTrophy[2] = findViewById(R.id.tv_trophyCount10);
        tvTrophy[3] = findViewById(R.id.tv_trophyCount50);
    }

    private void resetTrophyCounts() {
        SharedPreferences sharedPreferences = getSharedPreferences("TrophyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // 저장된 모든 데이터를 삭제
        editor.clear();
        editor.apply();

        // 트로피와 관련된 변수들을 기본값으로 초기화
        seaTrophyCount = 0;
        forestTrophyCount = 1;
        castleTrophyCount = 3;
        villageTrophyCount = 5;
        universeTrophyCount = 10;
        desertTrophyCount = 15;
        customTrophyCount = 20;

        seaTrophyIndex = 0;
        forestTrophyIndex = 0;
        castleTrophyIndex = 0;
        villageTrophyIndex = 0;
        universeTrophyIndex = 0;
        desertTrophyIndex = 0;
        customTrophyIndex = 0;

        totalTrophyCount = 0;

        // UI 업데이트
        updateUIForAllThemes();
        updateProgressBars();
    }

    @Override
    public void loadUserInfo(ImageView profile, TextView name) {
        userInfo.loadUserInfo(profile, name);
    }


}