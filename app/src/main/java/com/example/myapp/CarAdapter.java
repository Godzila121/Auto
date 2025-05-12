package com.example.myapp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {

    private Context context;
    private List<Car> carList;
    private List<String> favoriteCarIds;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
    private boolean allowDeletion;
    private boolean allowPurchase;

    public CarAdapter(Context context, List<Car> carList, List<String> favoriteCarIds, boolean allowDeletion, boolean allowPurchase) {
        this.context = context;
        this.carList = (carList != null) ? carList : new ArrayList<>();
        this.favoriteCarIds = (favoriteCarIds != null) ? favoriteCarIds : new ArrayList<>();
        this.allowDeletion = allowDeletion;
        this.allowPurchase = allowPurchase;
        Log.d("CarAdapter_Lifecycle", "Adapter created. allowDeletion: " + allowDeletion + ", allowPurchase: " + allowPurchase);
    }

    public CarAdapter(Context context, List<Car> carList, List<String> favoriteCarIds, boolean allowDeletion) {
        this(context, carList, favoriteCarIds, allowDeletion, false);
    }

    public CarAdapter(Context context, List<Car> carList, List<String> favoriteCarIds) {
        this(context, carList, favoriteCarIds, false, false);
    }


    @Override
    public CarViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_car, parent, false);
        return new CarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CarViewHolder holder, int position) {
        Car car = carList.get(position);
        Log.d("CarAdapter", "onBindViewHolder for position: " + position +
                ", Car: " + (car != null ? car.getName() : "null car") +
                ", ID: " + (car != null && car.getId() != null ? car.getId() : "null id"));
        if (car != null) {
            holder.bind(car);
        } else {
            Log.e("CarAdapter", "Car object at position " + position + " is null.");
        }
    }

    @Override
    public int getItemCount() {
        return (carList != null) ? carList.size() : 0;
    }

    public void updateFavoriteCarIds(List<String> newFavoriteCarIds) {
        this.favoriteCarIds.clear();
        if (newFavoriteCarIds != null) {
            this.favoriteCarIds.addAll(newFavoriteCarIds);
        }
        notifyDataSetChanged();
    }

    public void updateCarList(List<Car> newCars) {
        this.carList.clear();
        if (newCars != null) {
            this.carList.addAll(newCars);
        }
        notifyDataSetChanged();
    }


    public class CarViewHolder extends RecyclerView.ViewHolder {
        ImageView carImage;
        TextView carName, carType, carYear, carCountry, carPrice, customsDuty, totalPriceTextView;
        ImageButton heartButton, deleteCarButton;
        Button buyCarButton;
        Car currentCar;

        public CarViewHolder(View itemView) {
            super(itemView);
            carImage = itemView.findViewById(R.id.car_image);
            carName = itemView.findViewById(R.id.car_name);
            carType = itemView.findViewById(R.id.car_type);
            carYear = itemView.findViewById(R.id.car_year);
            carCountry = itemView.findViewById(R.id.car_country);
            carPrice = itemView.findViewById(R.id.car_price);
            customsDuty = itemView.findViewById(R.id.customs_duty);
            totalPriceTextView = itemView.findViewById(R.id.input_total_price);
            heartButton = itemView.findViewById(R.id.heart_button);
            deleteCarButton = itemView.findViewById(R.id.button_delete_car);
            buyCarButton = itemView.findViewById(R.id.button_buy_car);

            if (buyCarButton == null) {
                Log.e("CarAdapter_ViewHolder", "Кнопка button_buy_car НЕ ЗНАЙДЕНА в item_car.xml!");
            } else {
                Log.d("CarAdapter_ViewHolder", "Кнопка button_buy_car успішно знайдена.");
            }
            if (deleteCarButton == null) {
                Log.e("CarAdapter_ViewHolder", "Кнопка button_delete_car НЕ ЗНАЙДЕНА в item_car.xml!");
            }


            heartButton.setOnClickListener(v -> {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    if (currentCar != null && currentCar.getId() != null) {
                        String carId = currentCar.getId();
                        Log.d("CarAdapter", "Heart clicked for car ID: " + carId);
                        if (favoriteCarIds.contains(carId)) {
                            FirebaseHelper.removeFavorite(context, carId);
                            favoriteCarIds.remove(carId);
                            heartButton.setImageResource(R.drawable.ic_favorite);
                        } else {
                            FirebaseHelper.addFavorite(context, carId);
                            favoriteCarIds.add(carId);
                            heartButton.setImageResource(R.drawable.heart_button);
                        }
                    } else {
                        Log.e("CarAdapter", "Heart clicked: currentCar or carId is null!");
                    }
                } else {
                    Toast.makeText(context, "Будь ласка, увійдіть, щоб керувати улюбленими", Toast.LENGTH_SHORT).show();
                    context.startActivity(new Intent(context, AccountActivity.class));
                }
            });

            if (deleteCarButton != null) {
                deleteCarButton.setOnClickListener(v -> {
                    if (currentCar == null || currentCar.getId() == null) {
                        Log.e("CarAdapter", "Delete clicked: currentCar or carId is null");
                        Toast.makeText(context, "Помилка: Неможливо отримати дані автомобіля.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null && currentCar.getUserId() != null && currentCar.getUserId().equals(currentUser.getUid())) {
                        new AlertDialog.Builder(context)
                                .setTitle("Підтвердження видалення")
                                .setMessage("Ви впевнені, що хочете видалити автомобіль \"" + currentCar.getName() + "\"?\nЦю дію неможливо буде скасувати.")
                                .setPositiveButton("Так, видалити", (dialog, which) -> {
                                    Log.d("CarAdapter", "Видалення автомобіля з ID: " + currentCar.getId());
                                    FirebaseHelper.deleteCarFromRealtimeDatabase(context, currentCar.getId());
                                })
                                .setNegativeButton("Ні", null)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    } else {
                        Log.w("CarAdapter", "Спроба видалення не власником або користувач не залогінений.");
                        Toast.makeText(context, "Ви не можете видалити цей автомобіль.", Toast.LENGTH_SHORT).show();
                    }
                });
            }


            if (buyCarButton != null) {
                buyCarButton.setOnClickListener(v -> {
                    if (currentCar == null || currentCar.getId() == null || currentCar.getUserId() == null || currentCar.getName() == null) {
                        Log.e("CarAdapter", "Buy clicked: currentCar or its essential fields are null");
                        Toast.makeText(context, "Помилка: Недостатньо даних про автомобіль.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    FirebaseUser buyer = FirebaseAuth.getInstance().getCurrentUser();
                    if (buyer == null) {
                        Toast.makeText(context, "Будь ласка, увійдіть, щоб купити автомобіль.", Toast.LENGTH_SHORT).show();
                        context.startActivity(new Intent(context, AccountActivity.class));
                        return;
                    }
                    if (currentCar.getUserId().equals(buyer.getUid())) {
                        Toast.makeText(context, "Ви не можете купити свій власний автомобіль.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    PurchaseRequest request = new PurchaseRequest(
                            currentCar.getId(),
                            currentCar.getName(),
                            currentCar.getUserId(),
                            null,
                            buyer.getUid(),
                            buyer.getEmail()
                    );
                    FirebaseHelper.createPurchaseRequest(context, request);
                });
            }
        }

        public void bind(Car car) {
            currentCar = car;

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            String currentUserIdForLog = (currentUser != null) ? currentUser.getUid() : "null_user (not logged in)";
            String carNameForLog = (car != null && car.getName() != null) ? car.getName() : "N/A_CAR_NAME";
            String carOwnerIdForLog = (car != null && car.getUserId() != null) ? car.getUserId() : "null_car_owner_id";

            Log.d("CarAdapter_BuyButton", "------------------------------------------");
            Log.d("CarAdapter_BuyButton", "Binding car: " + carNameForLog + " (ID: " + (car != null ? car.getId() : "N/A") + ")");
            Log.d("CarAdapter_BuyButton", "  allowPurchase (adapter flag): " + allowPurchase);
            Log.d("CarAdapter_BuyButton", "  currentUser ID (потенційний покупець): " + currentUserIdForLog);
            Log.d("CarAdapter_BuyButton", "  Car owner ID (продавець - car.getUserId()): " + carOwnerIdForLog);

            boolean isCarPopulated = car != null && car.getUserId() != null;
            boolean isUserNotOwner = false;
            if (isCarPopulated && currentUser != null) {
                isUserNotOwner = !car.getUserId().equals(currentUser.getUid());
            }
            Log.d("CarAdapter_BuyButton", "  Умови: allowPurchase=" + allowPurchase +
                    ", currentUserExists=" + (currentUser != null) +
                    ", carHasOwner=" + isCarPopulated +
                    ", isUserNotOwner=" + isUserNotOwner);

            boolean shouldShowBuyButton = false;
            if (buyCarButton != null) {
                shouldShowBuyButton = allowPurchase && currentUser != null && isCarPopulated && isUserNotOwner;
            } else {
                Log.e("CarAdapter_BuyButton", "  buyCarButton (R.id.button_buy_car) НЕ ЗНАЙДЕНО в XML!");
            }
            Log.d("CarAdapter_BuyButton", "  ФІНАЛЬНЕ РІШЕННЯ - shouldShowBuyButton: " + shouldShowBuyButton);


            carName.setText(car.getName());
            carType.setText("Тип: " + car.getType());
            carYear.setText(String.valueOf(car.getYear()));
            carCountry.setText("Країна: " + car.getCountry());
            carPrice.setText("Ціна: " + currencyFormat.format(car.getPrice()));
            customsDuty.setText("Мито: " + currencyFormat.format(car.getCustomsDuty()));

            if (totalPriceTextView != null) {
                totalPriceTextView.setText(currencyFormat.format(car.getTotalPrice()));
            }

            if (favoriteCarIds != null && car.getId() != null && favoriteCarIds.contains(car.getId())) {
                heartButton.setImageResource(R.drawable.heart_button);
            } else {
                heartButton.setImageResource(R.drawable.ic_favorite);
            }

            if (deleteCarButton != null) {
                if (allowDeletion && currentUser != null && car.getUserId() != null && car.getUserId().equals(currentUser.getUid())) {
                    deleteCarButton.setVisibility(View.VISIBLE);
                } else {
                    deleteCarButton.setVisibility(View.GONE);
                }
            }

            if (buyCarButton != null) {
                if (shouldShowBuyButton) {
                    buyCarButton.setVisibility(View.VISIBLE);
                } else {
                    buyCarButton.setVisibility(View.GONE);
                }
            }
            Log.d("CarAdapter_BuyButton", "------------------------------------------");
        }
    }
}