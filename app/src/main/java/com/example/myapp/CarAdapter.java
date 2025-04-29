package com.example.myapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {

    private Context context;
    private List<Car> carList;
    private List<Car> favoriteCars;

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
        TextView carName;
        ImageButton heartButton;

        public CarViewHolder(View itemView) {
            super(itemView);
            carName = itemView.findViewById(R.id.car_name);
            heartButton = itemView.findViewById(R.id.heart_button);
        }
    }
}