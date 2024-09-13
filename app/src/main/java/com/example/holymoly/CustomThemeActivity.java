package com.example.holymoly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class CustomThemeActivity extends AppCompatActivity implements UserInfoLoader{

    private EditText editTextTheme;
    private ImageButton btnOk;
    private ImageView profile;
    private TextView name, nickname;

    private UserInfo userInfo = new UserInfo();

    /* 효과음 */
    private SharedPreferences pref;
    private boolean isSoundOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_theme);
        pref = getSharedPreferences("music", MODE_PRIVATE); // 효과음 초기화

        editTextTheme = findViewById(R.id.customTheme);
        btnOk = findViewById(R.id.ib_nextStep);
        name = findViewById(R.id.mini_name);
        nickname = findViewById(R.id.mini_nickname);
        profile = findViewById(R.id.mini_profile);

        loadUserInfo(profile, name, nickname);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound();
                String customTheme = editTextTheme.getText().toString().trim();
                if (customTheme.isEmpty()) {
                    Toast.makeText(CustomThemeActivity.this, "테마를 입력하세요!", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Intent를 사용하여 입력된 테마를 SelectthemaActivity로 전달
                Intent resultIntent = new Intent();
                resultIntent.putExtra("customTheme", customTheme);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    @Override
    public void loadUserInfo(ImageView profile, TextView name, TextView nickname) {
        userInfo.loadUserInfo(profile, name, nickname);
    }

    // 효과음
    public void sound() {
        isSoundOn = pref.getBoolean("on&off2", true);
        Intent intent = new Intent(this, SoundService.class);
        if (isSoundOn) startService(intent); // 효과음 on
        else stopService(intent);            // 효과음 off
    }
}
