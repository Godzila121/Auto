package com.example.myapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddCarActivity extends AppCompatActivity {

    private EditText inputCarName, inputCarYear, inputCarCountry, inputCarPrice, inputEngineCapacity;
    private Spinner spinnerCarType;
    private Button buttonAddNewCar;

    private DatabaseReference carsRef;
    private TextView displayAddCarTotalPrice;

    private FirebaseAuth mAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_car);

        mAuth = FirebaseAuth.getInstance();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        carsRef = database.getReference("cars");

        inputCarName = findViewById(R.id.input_car_name);
        spinnerCarType = findViewById(R.id.spinner_car_type);
        inputCarYear = findViewById(R.id.input_car_year);
        inputCarCountry = findViewById(R.id.input_car_country);
        inputCarPrice = findViewById(R.id.input_car_price);
        inputEngineCapacity = findViewById(R.id.input_engine_capacity);
        buttonAddNewCar = findViewById(R.id.button_add_new_car);
        displayAddCarTotalPrice = findViewById(R.id.display_add_car_total_price);

        if (displayAddCarTotalPrice == null) {
            Log.e("AddCarActivity", "УВАГА: TextView з ID 'display_add_car_total_price' не знайдено у activity_add_car.xml!");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Седан", "Джип", "Хетчбек", "Універсал"}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCarType.setAdapter(adapter);

        buttonAddNewCar.setOnClickListener(v -> {
            String name = inputCarName.getText().toString().trim();
            String type = spinnerCarType.getSelectedItem().toString();
            String yearStr = inputCarYear.getText().toString().trim();
            String country = inputCarCountry.getText().toString().trim();
            String priceStr = inputCarPrice.getText().toString().trim();
            String engineCapacityStr = inputEngineCapacity.getText().toString().trim();

            if (name.isEmpty() || yearStr.isEmpty() || country.isEmpty()
                    || priceStr.isEmpty() || engineCapacityStr.isEmpty()) {
                Toast.makeText(this, "Будь ласка, заповніть всі поля", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!name.matches("[a-zA-Zа-яА-ЯіІїЇєЄґҐ\\s]+")) {
                Toast.makeText(this, "Назва має містити лише літери", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!country.matches("[a-zA-Zа-яА-ЯіІїЇєЄґҐ\\s]+")) {
                Toast.makeText(this, "Країна має містити лише літери", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int year = Integer.parseInt(yearStr);
                if (year > 2025 || year < 1886 || yearStr.length() != 4) {
                    Toast.makeText(this, "Рік має бути 4 цифри і не більше 2025 і не менше 1886", Toast.LENGTH_SHORT).show();
                    return;
                }
                double price = Double.parseDouble(priceStr);
                double engineCapacity = Double.parseDouble(engineCapacityStr);
                if (engineCapacity < 1.1 || engineCapacity > 13.4) {
                    Toast.makeText(this, "Обʼєм двигуна має бути від 1.1 до 13.4", Toast.LENGTH_SHORT).show();
                    return;
                }

                int carAge = 2025 - year;
                double customsTax = price * 0.12;
                double totalPrice = price + customsTax;

                if (displayAddCarTotalPrice != null) {
                    displayAddCarTotalPrice.setText(String.format(java.util.Locale.US, "%.2f USD", totalPrice));
                }

                Car newCar = new Car(name, type, year, country, price, engineCapacity, carAge);

                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    newCar.setUserId(currentUser.getUid());
                } else {

                    Toast.makeText(AddCarActivity.this, "Помилка: користувача не автентифіковано.", Toast.LENGTH_LONG).show();
                    Log.e("AddCarActivity", "Неможливо встановити userId, користувач не автентифікований.");
                    return;
                }

                String carId = carsRef.push().getKey();
                if (carId != null) {
                    newCar.setId(carId);
                    carsRef.child(carId).setValue(newCar)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(AddCarActivity.this, "Автомобіль додано успішно", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(AddCarActivity.this, SecondActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(AddCarActivity.this, "Помилка при додаванні автомобіля: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e("AddCarActivity", "Firebase setValue failed", e);
                            });
                }

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Невірний формат числових полів", Toast.LENGTH_SHORT).show();
            }
        });
    }
}