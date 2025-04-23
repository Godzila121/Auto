package com.example.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 3000; // 3 секунди
    private TextView greetingTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ініціалізація TextView
        greetingTextView = findViewById(R.id.greetingTextView);

        if (greetingTextView != null) {
            greetingTextView.setText("Ласкаво просимо!");
        }

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this, SecondActivity.class);
            startActivity(intent);
            finish(); // закриваємо splash
        }, SPLASH_TIME_OUT);
    }
}
