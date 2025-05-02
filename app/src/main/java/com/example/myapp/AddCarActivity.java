package com.example.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddCarActivity extends AppCompatActivity {

    private EditText inputCarName, inputCarType, inputCarYear, inputCarCountry, inputCarPrice, inputEngineCapacity, inputCarAge;
    private Button buttonAddNewCar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_car);

        inputCarName = findViewById(R.id.input_car_name);
        inputCarType = findViewById(R.id.input_car_type);
        inputCarYear = findViewById(R.id.input_car_year);
        inputCarCountry = findViewById(R.id.input_car_country);
        inputCarPrice = findViewById(R.id.input_car_price);
        inputEngineCapacity = findViewById(R.id.input_engine_capacity);
        inputCarAge = findViewById(R.id.input_car_age);
        buttonAddNewCar = findViewById(R.id.button_add_new_car);

        buttonAddNewCar.setOnClickListener(v -> {
            String name = inputCarName.getText().toString().trim();
            String type = inputCarType.getText().toString().trim();
            String yearStr = inputCarYear.getText().toString().trim();
            String country = inputCarCountry.getText().toString().trim();
            String priceStr = inputCarPrice.getText().toString().trim();
            String engineCapacityStr = inputEngineCapacity.getText().toString().trim();
            String ageStr = inputCarAge.getText().toString().trim();

            if (name.isEmpty() || type.isEmpty() || yearStr.isEmpty() || country.isEmpty() || priceStr.isEmpty() || engineCapacityStr.isEmpty() || ageStr.isEmpty()) {
                Toast.makeText(this, "Будь ласка, заповніть всі поля", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int year = Integer.parseInt(yearStr);
                double price = Double.parseDouble(priceStr);
                int engineCapacity = Integer.parseInt(engineCapacityStr);
                int age = Integer.parseInt(ageStr);

                Car newCar = new Car(name, type, year, country, price, engineCapacity, age);

                Intent resultIntent = new Intent();
                resultIntent.putExtra("new_car", newCar);
                setResult(RESULT_OK, resultIntent);
                finish();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Невірний формат року, ціни, об'єму двигуна або віку", Toast.LENGTH_SHORT).show();
            }
        });
    }
}