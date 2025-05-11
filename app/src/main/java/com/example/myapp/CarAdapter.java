package com.example.myapp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
    private List<String> favoriteCarIds; // Зберігаємо ID улюблених автомобілів
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

    public CarAdapter(Context context, List<Car> carList, List<String> favoriteCarIds) {
        this.context = context;
        this.carList = (carList != null) ? carList : new ArrayList<>();
        this.favoriteCarIds = (favoriteCarIds != null) ? favoriteCarIds : new ArrayList<>();
    }

    @Override
    public CarViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_car, parent, false);
        return new CarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CarViewHolder holder, int position) {
        Car car = carList.get(position);
        Log.d("CarAdapter", "onBindViewHolder called for position: " + position + ", Car: " + car);
        holder.bind(car);
    }

    @Override
    public int getItemCount() {
        return (carList != null) ? carList.size() : 0;
    }

    public void updateFavoriteCarIds(List<String> favoriteCarIds) {
        this.favoriteCarIds = favoriteCarIds;
        notifyDataSetChanged();
    }

    public class CarViewHolder extends RecyclerView.ViewHolder {
        ImageView carImage;
        TextView carName;
        TextView carType;
        TextView carYear;
        TextView carCountry;
        TextView carPrice;
        TextView customsDuty;
        TextView totalPrice;
        ImageButton heartButton;
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
            totalPrice = itemView.findViewById(R.id.input_total_price);
            heartButton = itemView.findViewById(R.id.heart_button);

            heartButton.setOnClickListener(v -> {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    Log.d("CarAdapter", "Heart button clicked. currentCar: " + currentCar);
                    if (currentCar != null) {
                        String carId = currentCar.getId();
                        Log.d("CarAdapter", "Clicked car ID: " + carId);
                        if (carId != null) {
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
                            Log.e("CarAdapter", "Clicked car ID is null!");
                        }
                    } else {
                        Log.e("CarAdapter", "currentCar is null when heart button clicked!");
                    }
                } else {
                    Toast.makeText(context, "Будь ласка, увійдіть, щоб додавати автомобілі до улюбленого", Toast.LENGTH_SHORT).show();
                    context.startActivity(new Intent(context, AccountActivity.class));
                }
            });
        }

        public void bind(Car car) {
            Log.d("CarAdapter", "bind called with Car: " + car);
            currentCar = car;
            if (currentCar != null) {
                Log.d("CarAdapter", "Binding car with ID: " + currentCar.getId() + ", Name: " + currentCar.getName());
            } else {
                Log.e("CarAdapter", "Attempting to bind a null Car object!");
            }
            carName.setText(car.getName());
            carType.setText("Тип: " + car.getType());
            carYear.setText("Рік: " + car.getYear());
            carCountry.setText("Країна: " + car.getCountry());
            carPrice.setText("Ціна: " + currencyFormat.format(car.getPrice()));
            customsDuty.setText("Мито: " + currencyFormat.format(car.getCustomsDuty()));

            double totalPriceValue = car.getPrice() + car.getCustomsDuty();
            totalPrice.setText("Загальна ціна: " + currencyFormat.format(totalPriceValue));

            if (favoriteCarIds.contains(car.getId())) {
                heartButton.setImageResource(R.drawable.heart_button);
            } else {
                heartButton.setImageResource(R.drawable.ic_favorite);
            }
        }
    }
}