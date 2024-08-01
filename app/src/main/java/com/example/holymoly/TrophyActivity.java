package com.example.holymoly;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class TrophyActivity extends AppCompatActivity implements View.OnClickListener, UserInfoLoader{
    private TextView name;
    private ImageButton trophy, home, edit;
    private ImageView profile;

    private UserInfo userInfo = new UserInfo();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trophy);

        name = findViewById(R.id.mini_name);
        profile = findViewById(R.id.mini_profile);
        trophy = findViewById(R.id.ib_bictrophy);
        home = findViewById(R.id.ib_homebutton);
        edit = findViewById(R.id.ib_edit);

        loadUserInfo(profile, name);

        trophy.setOnClickListener(this);
        home.setOnClickListener(this);
        edit.setOnClickListener(this);
    }
    public void onClick(View v) {
        if(v.getId() == R.id.ib_bictrophy) {
            Intent intent = new Intent(this, AchieveActivity.class);
            startActivity(intent);
        }
        else if(v.getId() == R.id.ib_homebutton) {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        }
        else if(v.getId() == R.id.ib_edit) {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void loadUserInfo(ImageView profile, TextView name) {
        userInfo.loadUserInfo(profile, name);
    }
}