package com.example.holymoly;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class CustomThemeActivity extends AppCompatActivity {

    private EditText editTextTheme;
    private ImageButton btnOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_theme);

        editTextTheme = findViewById(R.id.customTheme);
        btnOk = findViewById(R.id.ib_nextStep);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
}
