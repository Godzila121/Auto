package com.example.myapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SecondActivity extends AppCompatActivity {

    private ImageView buttonSearch;
    private ImageView buttonFavorite;
    private ImageView buttonAccount;
    private ImageView buttonProfile;
    private RecyclerView recyclerView;
    private CarAdapter carAdapter;
    private List<Car> carList;
    private List<String> favoriteCarIds = new ArrayList<>(); // Зберігаємо ID улюблених
    private FloatingActionButton fabAddCar;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseRef;

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Car newCar = (Car) result.getData().getSerializableExtra("new_car");
                    if (newCar != null) {
                        carList.add(newCar);
                        carAdapter.notifyItemInserted(carList.size() - 1);
                        addCarToFirebase(newCar);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        // 🔹 Ініціалізація списків
        carList = new ArrayList<>();

        // 🔹 Ініціалізація Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseRef = mDatabase.getReference("cars");

        // 🔹 Прив’язка елементів інтерфейсу
        buttonSearch = findViewById(R.id.button_search);
        buttonFavorite = findViewById(R.id.button_favorite);
        buttonAccount = findViewById(R.id.button_account);
        buttonProfile = findViewById(R.id.button_profile);
        recyclerView = findViewById(R.id.recyclerView);
        fabAddCar = findViewById(R.id.fab_add_car);

        // ➡️ Перевірка стану входу при створенні Activity
        if (!SharedPreferencesHelper.getLoginStatus(this)) {
            Intent accountIntent = new Intent(SecondActivity.this, AccountActivity.class);
            startActivity(accountIntent);
            finish();
            return;
        }

        // 🔹 Ініціалізація адаптера (передаємо порожній список улюблених на початку)
        carAdapter = new CarAdapter(this, carList, favoriteCarIds);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(carAdapter);

        // 🔄 Завантажуємо ID улюблених автомобілів
        SharedPreferencesHelper.getFavoriteCarIds(this, ids -> {
            this.favoriteCarIds.addAll(ids);
            carAdapter.updateFavoriteCarIds(this.favoriteCarIds);
        });

        // 🔹 Завантажуємо авто з Firebase при запуску
        loadCarsFromFirebase();

        // 🔹 Кнопки
        buttonSearch.setOnClickListener(v ->
                Toast.makeText(this, "Ви вже на цій сторінці", Toast.LENGTH_SHORT).show()
        );

        buttonFavorite.setOnClickListener(v -> {
            Intent favoriteIntent = new Intent(this, FavoriteActivity.class);
            startActivity(favoriteIntent);
            overridePendingTransition(0, 0);
        });

        buttonAccount.setOnClickListener(v -> {
            startActivity(new Intent(this, AccountActivity.class));
            overridePendingTransition(0, 0);
        });

        buttonProfile.setOnClickListener(v -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                Toast.makeText(this, "Ви залогінені як: " + currentUser.getEmail(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Будь ласка, увійдіть", Toast.LENGTH_SHORT).show();
                Intent accountIntent = new Intent(SecondActivity.this, AccountActivity.class);
                startActivity(accountIntent);
                finish();
            }
        });

        fabAddCar.setOnClickListener(v -> {
            if (SharedPreferencesHelper.getLoginStatus(this)) {
                Intent intent = new Intent(this, AddCarActivity.class);
                launcher.launch(intent);
            } else {
                Toast.makeText(this, "Будь ласка, увійдіть, щоб додавати автомобілі", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, AccountActivity.class));
            }
        });
    }

    private boolean isUserRegistered() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return prefs.getBoolean("isRegistered", false);
    }

    public void addCarToFirebase(Car car) {
        if (car != null) {
            String carId = mDatabaseRef.push().getKey();
            car.setId(carId); // Встановлюємо carId перед збереженням
            if (carId != null) {
                mDatabaseRef.child(carId).setValue(car)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(SecondActivity.this, "Автомобіль успішно додано", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SecondActivity.this, "Помилка додавання автомобіля", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
        loadCarsFromFirebase();
    }

    // ... ваш існуючий код в SecondActivity.java ...

    public void loadCarsFromFirebase() {
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                carList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Car car = snapshot.getValue(Car.class);
                    if (car != null) {
                        // === ПОЧАТОК ВИПРАВЛЕННЯ ===
                        String carKey = snapshot.getKey(); // Отримуємо ключ вузла (це і є ваш carId)
                        car.setId(carKey);                 // Встановлюємо цей ключ як ID об'єкта Car
                        // === КІНЕЦЬ ВИПРАВЛЕННЯ ===

                        carList.add(car);
                    }
                }
                // Оновлюємо адаптер після завантаження та оновлення всіх ID
                if (carAdapter != null) { // Додайте перевірку, чи адаптер вже ініціалізовано
                    carAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SecondActivity.this, "Помилка при завантаженні даних: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

// ... решта вашого коду ...
}