package com.example.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        Button buttonOne = findViewById(R.id.button_one);
        Button buttonTwo = findViewById(R.id.button_two);

        buttonOne.setOnClickListener(v ->
                Toast.makeText(this, "Натиснута кнопка 1", Toast.LENGTH_SHORT).show()
        );

        buttonTwo.setOnClickListener(v ->
                Toast.makeText(this, "Натиснута кнопка 2", Toast.LENGTH_SHORT).show()
        );
    }
}
