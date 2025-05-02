package com.example.myapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {

    private Context context;
    private List<Car> carList;
    private List<Car> favoriteCars;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

    public CarAdapter(Context context, List<Car> carList, List<Car> favoriteCars) {
        this.context = context;
        this.carList = carList;
        this.favoriteCars = favoriteCars;
    }

    @Override
    public CarViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_car, parent, false);
        return new CarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CarViewHolder holder, int position) {
        Car car = carList.get(position);
        holder.carName.setText(car.getName());
        holder.carType.setText("Тип: " + car.getType());
        holder.carYear.setText("Рік: " + String.valueOf(car.getYear()));
        holder.carCountry.setText("Країна: " + car.getCountry());
        holder.carPrice.setText("Ціна: " + currencyFormat.format(car.getPrice()));
        holder.customsDuty.setText("Мито: " + currencyFormat.format(car.getCustomsDuty()));

        if (favoriteCars.contains(car)) {
            holder.heartButton.setImageResource(R.drawable.heart_button);
        } else {
            holder.heartButton.setImageResource(R.drawable.ic_favorite);
        }

        holder.heartButton.setOnClickListener(v -> {
            if (!favoriteCars.contains(car)) {
                favoriteCars.add(car);
                holder.heartButton.setImageResource(R.drawable.heart_button);
            } else {
                favoriteCars.remove(car);
                holder.heartButton.setImageResource(R.drawable.ic_favorite);
            }

            SharedPreferencesHelper.saveFavorites(context, favoriteCars);
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    public static class CarViewHolder extends RecyclerView.ViewHolder {
        ImageView carImage;
        TextView carName;
        TextView carType;
        TextView carYear;
        TextView carCountry;
        TextView carPrice;
        TextView customsDuty;
        ImageButton heartButton;

        public CarViewHolder(View itemView) {
            super(itemView);
            carImage = itemView.findViewById(R.id.car_image);
            carName = itemView.findViewById(R.id.car_name);
            carType = itemView.findViewById(R.id.car_type);
            carYear = itemView.findViewById(R.id.car_year);
            carCountry = itemView.findViewById(R.id.car_country);
            carPrice = itemView.findViewById(R.id.car_price);
            customsDuty = itemView.findViewById(R.id.customs_duty);
            heartButton = itemView.findViewById(R.id.heart_button);
        }
    }
}