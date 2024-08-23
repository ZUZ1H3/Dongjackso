package com.example.holymoly;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CustomCharacterActivity extends AppCompatActivity implements UserInfoLoader{

    private EditText editTextCharacter;
    private ImageButton btnOk;
    private ImageView profile;
    private TextView name;

    private UserInfo userInfo = new UserInfo();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_character);

        editTextCharacter = findViewById(R.id.customCharacter);
        btnOk = findViewById(R.id.ib_nextStep);
        name = findViewById(R.id.mini_name);
        profile = findViewById(R.id.mini_profile);

        loadUserInfo(profile, name);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String customCharacter = editTextCharacter.getText().toString().trim();
                if (customCharacter.isEmpty()) {
                    Toast.makeText(CustomCharacterActivity.this, "캐릭터를 입력하세요!", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Intent를 사용하여 입력된 테마를 SelectthemaActivity로 전달
                Intent resultIntent = new Intent();
                resultIntent.putExtra("customCharacter", customCharacter);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    @Override
    public void loadUserInfo(ImageView profile, TextView name) {
        userInfo.loadUserInfo(profile, name);
    }
}