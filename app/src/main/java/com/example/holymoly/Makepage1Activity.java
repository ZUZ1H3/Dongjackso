package com.example.holymoly;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.Executor;

public class Makepage1Activity extends AppCompatActivity {
    private Executor executor = new MainThreadExecutor(); // MainThreadExecutor 사용
protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectcharacter);
    }
}