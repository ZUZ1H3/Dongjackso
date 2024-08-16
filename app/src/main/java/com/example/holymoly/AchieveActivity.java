package com.example.holymoly;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AchieveActivity extends AppCompatActivity implements UserInfoLoader {
    //라디오버튼2개 라디오그룹1 나가기버튼1 등록
    //동화 업적 - ~~개 만들기 텍스트뷰 수정, 오른쪽에 진행도 퍼센트+보상받기 텍스트뷰 등록 / 이미지버튼 등록
    //작가 호칭 - 프로그래스바, 프로그래스바 위 텍스트뷰, 이미지버튼 등록 (+사진도 등록 진척도에 따라 버튼이 활성화 되게끔함)
    //라디오버튼에 따라 레이아웃의 visible 수정 - 2개니깐 이렇게 하는게 효율적인 듯 ~
    //그리고 동화 업적에 받은 트로피의 개수도 연동되어야 하니 하나의 자바에서 처리하는게 효율

    private RadioGroup rgTrophyCategory;
    private ImageButton ibCloseWindow;
    private ImageView profile;
    private TextView name;

    private UserInfo userInfo = new UserInfo();

    //완료된 동화 테마 개수 세기
    private int seaTrophyCount, forestTrophyCount,
                castleTrophyCount, villageTrophyCount,
                universeTrophyCount, desertTrophyCount,
                customTrophyCount;

    //동화 테마 목표 개수
    private int goalSeaTrophyCount, goalForestTrophyCount, goalcastleTrophyCount,
                goalVillageTrophyCount, goalUniverseTrophyCount, goalDesertTrophyCount,
                goalCustomTrophyCount;

    //동화 업적 텍스트뷰
    private TextView tvSeaTrophy, tvSeaTrophyPercent,
            tvForestTrophy, tvForestTrophyPercent,
            tvCastleTrophy, tvCastleTrophyPercent,
            tvVillageTrophy, tvVillageTrophyPercent,
            tvUniverseTrophy, tvUniverseTrophyPercent,
            tvDesertTrophy, tvDesertTrophyPercent,
            tvCustomTrophy, tvCustomTrophyPercent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achieve);

        //동화업적, 작가호칭 선택 라디오그룹
        rgTrophyCategory = findViewById(R.id.rg_trophyCategory);

        //이미지버튼
        ibCloseWindow = findViewById(R.id.ib_closeWindow);

        //동화업적 개수 초기화?
        seaTrophyCount = 0; forestTrophyCount = 0; castleTrophyCount = 0; villageTrophyCount = 0; universeTrophyCount = 0;

        name = findViewById(R.id.mini_name);
        profile = findViewById(R.id.mini_profile);

        loadUserInfo(profile, name);

        //동화업적 텍스트뷰 연결
        tvSeaTrophy = findViewById(R.id.tv_seaTrophy); tvSeaTrophyPercent = findViewById(R.id.tv_seaTrophyPercent);
        tvForestTrophy = findViewById(R.id.tv_forestTrophy); tvForestTrophyPercent = findViewById(R.id.tv_forestTrophyPercent);
        tvCastleTrophy = findViewById(R.id.tv_castleTrophy); tvCastleTrophyPercent = findViewById(R.id.tv_castleTrophyPercent);
        tvVillageTrophy = findViewById(R.id.tv_villageTrophy); tvVillageTrophyPercent = findViewById(R.id.tv_villageTrophyPercent);
        tvUniverseTrophy = findViewById(R.id.tv_universeTrophy); tvUniverseTrophyPercent = findViewById(R.id.tv_universeTrophyPercent);

        //디폴트 상태 설정 - 처음에 키면 동화업적이 띄워져있는걸로함
        findViewById(R.id.fairytaleAchievementLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.writerappellationLayout).setVisibility(View.GONE);

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

        //동화업적창 알고리즘
        
        //완료된 바다 숲 궁전 마을 우주 동화 개수 세기

        //만들어질때마다 업데이트 + 그에 맞도록 퍼센트 업데이트 textView.setText("New Text from Java Code");

        //트로피 받기를 하면 창이 갱신되면서 (이때 애니메이션을 넣으면 좋을듯?) 새로 나타나게함
        //얻은 트로피는 어디에 표시?

        //작가호칭창
        //트로피 얻은 개수 저장
        //트로피 개수만큼 프로그래스바 업데이트
        //목표치를 채우면 사용버튼의 이미지가 바뀌면서 버튼이 활성화됨 - 이 부분은 버튼의 이미지가 바뀌고 바뀐 이미지면 그에 맞는 행동을 넣어놓음
        //얻은 호칭은 프로필에 나오도록함(업데이트)

    }

    @Override
    public void loadUserInfo(ImageView profile, TextView name) {
        userInfo.loadUserInfo(profile, name);
    }
}