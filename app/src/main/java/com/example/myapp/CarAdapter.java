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
import java.util.List;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {

    private Context context;
    private List<Car> carList;
    private List<Car> favoriteList;

    public CarAdapter(Context context, List<Car> carList, List<Car> favoriteList) {
        this.context = context;
        this.carList = carList;
        this.favoriteList = favoriteList;
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_car, parent, false);
        return new CarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        Car car = carList.get(position);
        holder.carName.setText(car.getName());

        // Ставимо правильну іконку залежно від того, чи в улюблених
        if (favoriteList.contains(car)) {
            holder.heartButton.setImageResource(R.drawable.heart_button);
        } else {
            holder.heartButton.setImageResource(R.drawable.ic_favorite);
        }

        holder.heartButton.setOnClickListener(v -> {
            if (favoriteList.contains(car)) {
                favoriteList.remove(car);
                holder.heartButton.setImageResource(R.drawable.ic_favorite);
            } else {
                favoriteList.add(car);
                holder.heartButton.setImageResource(R.drawable.heart_button);
            }
        });
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    static class CarViewHolder extends RecyclerView.ViewHolder {
        ImageView carImage;
        TextView carName;
        ImageButton heartButton;

        public CarViewHolder(@NonNull View itemView) {
            super(itemView);
            carImage = itemView.findViewById(R.id.car_image);
            carName = itemView.findViewById(R.id.car_name);
            heartButton = itemView.findViewById(R.id.heart_button);
        }
    }
}
