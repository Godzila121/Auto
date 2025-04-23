package com.example.myapp;

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

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Тут можна додати дії після затримки
            }
        }, SPLASH_TIME_OUT);
    }
}
