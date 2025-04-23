package com.example.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AnimationUtils;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 3000; // 3 секунди

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Handler().postDelayed(() -> {
            // Анімація зникнення
            findViewById(android.R.id.content).startAnimation(
                    AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_out)
            );

            // Затримка для завершення анімації перед переходом
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }, 1000); // 1 секунда = тривалість fade_out

        }, SPLASH_TIME_OUT);
    }
}
