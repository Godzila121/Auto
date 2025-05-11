package com.example.myapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log; // Додайте для логування
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView; // Імпорт для SearchView

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
import java.util.Locale; // Для toLowerCase

public class SecondActivity extends AppCompatActivity {

    private ImageView buttonSearch;
    private ImageView buttonFavorite;
    private ImageView buttonAccount;
    private ImageView buttonProfile;
    private RecyclerView recyclerView;
    private CarAdapter carAdapter;
    private List<Car> carList; // Список, який відображається в адаптері (фільтрований)
    private List<Car> originalCarList; // Повний список автомобілів з Firebase
    private List<String> favoriteCarIds = new ArrayList<>();
    private FloatingActionButton fabAddCar;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseRef;
    private ValueEventListener carsValueEventListener; // Зберігаємо посилання на слухача

    private SearchView searchViewCars; // Поле для SearchView

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Car newCar = (Car) result.getData().getSerializableExtra("new_car");
                    if (newCar != null) {
                        // Не додаємо тут до carList напряму, addCarToFirebase оновить дані через слухача
                        addCarToFirebase(newCar);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        originalCarList = new ArrayList<>(); // Ініціалізуємо повний список
        carList = new ArrayList<>();         // Ініціалізуємо список для адаптера

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseRef = mDatabase.getReference("cars");

        buttonSearch = findViewById(R.id.button_search);
        buttonFavorite = findViewById(R.id.button_favorite);
        buttonAccount = findViewById(R.id.button_account);
        buttonProfile = findViewById(R.id.button_profile);
        recyclerView = findViewById(R.id.recyclerView);
        fabAddCar = findViewById(R.id.fab_add_car);
        searchViewCars = findViewById(R.id.search_view_cars); // Ініціалізація SearchView

        // Перевірка стану входу
        FirebaseUser currentUser = mAuth.getCurrentUser(); // Використовуємо Firebase Auth як джерело істини
        if (currentUser == null) {
            // Якщо SharedPreferencesHelper.getLoginStatus(this) це застаріла перевірка,
            // краще покладатися на currentUser == null
            Intent accountIntent = new Intent(SecondActivity.this, AccountActivity.class);
            startActivity(accountIntent);
            finish();
            return;
        }

        carAdapter = new CarAdapter(this, carList, favoriteCarIds); // Використовуємо this.carList
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(carAdapter);

        // Завантажуємо ID улюблених (переконайтеся, що це джерело актуальне)
        // Якщо улюблені також у Firebase, краще завантажувати звідти
        if (mAuth.getCurrentUser() != null) {
            FirebaseHelper.getFavoriteCarIds(receivedFavoriteIds -> { // Передаємо ТІЛЬКИ слухача
                this.favoriteCarIds.clear();
                if (receivedFavoriteIds != null) { // Додайте перевірку на null про всяк випадок
                    this.favoriteCarIds.addAll(receivedFavoriteIds); // 'receivedFavoriteIds' тепер точно буде List<String>
                }
                if (carAdapter != null) {
                    carAdapter.updateFavoriteCarIds(this.favoriteCarIds);
                } else {
                    Log.e("SecondActivity", "carAdapter is null when updating favorite IDs");
                }
            });
        }


        setupSearchViewListener(); // Налаштування слухача для SearchView
        loadCarsFromFirebase();    // Завантажуємо автомобілі

        // Обробники кнопок (залишаються без змін, окрім buttonProfile для консистентності)
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
            FirebaseUser user = mAuth.getCurrentUser(); // Отримуємо актуального користувача
            if (user != null) {
                Toast.makeText(this, "Ви залогінені як: " + user.getEmail(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Будь ласка, увійдіть", Toast.LENGTH_SHORT).show();
                Intent accountIntent = new Intent(SecondActivity.this, AccountActivity.class);
                startActivity(accountIntent);
                // finish(); // Можливо, не потрібно закривати, якщо просто перенаправляємо
            }
        });
        fabAddCar.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null) {
                Intent intent = new Intent(this, AddCarActivity.class);
                launcher.launch(intent);
            } else {
                Toast.makeText(this, "Будь ласка, увійдіть, щоб додавати автомобілі", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, AccountActivity.class));
            }
        });
    }

    private void setupSearchViewListener() {
        if (searchViewCars == null) {
            Log.e("SecondActivity", "SearchView search_view_cars не знайдено у розмітці!");
            return;
        }
        searchViewCars.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Користувач натиснув "пошук" на клавіатурі (зазвичай не потрібно для живого пошуку)
                // filterCarList(query); // Вже обробляється в onQueryTextChange
                searchViewCars.clearFocus(); // Приховати клавіатуру
                return true; // Подія оброблена
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Текст змінився, фільтруємо список
                filterCarList(newText);
                return true; // Подія оброблена
            }
        });
    }

    private void filterCarList(String query) {
        List<Car> filteredList = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            filteredList.addAll(originalCarList); // Якщо запит порожній, показуємо всі авто
        } else {
            String lowerCaseQuery = query.toLowerCase(Locale.getDefault()).trim();
            for (Car car : originalCarList) {
                // Критерії пошуку: назва, тип, країна, рік. Можна додати більше.
                boolean nameMatches = car.getName() != null && car.getName().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery);
                boolean typeMatches = car.getType() != null && car.getType().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery);
                boolean countryMatches = car.getCountry() != null && car.getCountry().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery);
                // Для року можна зробити просту перевірку входження, або більш складну логіку
                boolean yearMatches = String.valueOf(car.getYear()).contains(lowerCaseQuery);

                if (nameMatches || typeMatches || countryMatches || yearMatches) {
                    filteredList.add(car);
                }
            }
        }

        carList.clear(); // Очищуємо поточний список в адаптері
        carList.addAll(filteredList); // Додаємо відфільтровані результати

        if (carAdapter != null) {
            carAdapter.notifyDataSetChanged(); // Оновлюємо RecyclerView
        } else {
            Log.e("SecondActivity", "carAdapter is null in filterCarList");
        }
    }

    public void addCarToFirebase(Car car) {
        if (car != null) {
            String carId = mDatabaseRef.push().getKey();
            car.setId(carId);
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
        // НЕ викликаємо loadCarsFromFirebase() тут, ValueEventListener оновить список автоматично
    }

    public void loadCarsFromFirebase() {
        if (carsValueEventListener == null) { // Додаємо слухача тільки якщо він ще не існує
            carsValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<Car> tempList = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Car car = snapshot.getValue(Car.class);
                        if (car != null) {
                            String carKey = snapshot.getKey();
                            car.setId(carKey);
                            tempList.add(car);
                        }
                    }
                    originalCarList.clear();
                    originalCarList.addAll(tempList); // Зберігаємо повний список
                    Log.d("SecondActivity", "Дані завантажено з Firebase, originalCarList розмір: " + originalCarList.size());

                    // Застосовуємо поточний фільтр (якщо є) після завантаження/оновлення даних
                    String currentQuery = (searchViewCars != null) ? searchViewCars.getQuery().toString() : "";
                    filterCarList(currentQuery);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(SecondActivity.this, "Помилка при завантаженні даних: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("SecondActivity", "Firebase onCancelled: ", databaseError.toException());
                }
            };
            mDatabaseRef.addValueEventListener(carsValueEventListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (carsValueEventListener != null) {
            mDatabaseRef.removeEventListener(carsValueEventListener); // Важливо видаляти слухача
        }
    }
}