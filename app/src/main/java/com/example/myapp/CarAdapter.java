package com.example.myapp;
import android.annotation.SuppressLint;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {

    private Context context;
    private List<Car> carList;
    private List<Car> favoriteList = new ArrayList<>(); // Список вподобаних

    // Конструктор адаптера
    public CarAdapter(Context context, List<Car> carList) {
        this.context = context;
        this.carList = carList;
    }

    @Override
    public CarViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Створення елемента для кожної картки
        View view = LayoutInflater.from(context).inflate(R.layout.item_car, parent, false);
        return new CarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CarViewHolder holder, int position) {
        // Прив'язка даних з моделі до відповідного елементу
        Car car = carList.get(position);
        holder.nameTextView.setText(car.getName());

        // Налаштовуємо поведінку кнопки сердечка
        holder.heartButton.setOnClickListener(v -> {
            if (!favoriteList.contains(car)) {
                favoriteList.add(car);
                holder.heartButton.setImageResource(R.drawable.heart_button); // Змінити іконку на заповнене сердечко
            } else {
                favoriteList.remove(car);
                holder.heartButton.setImageResource(R.drawable.heart_button); // Повернути порожнє сердечко
            }
            notifyItemChanged(position); // Оновити елемент
        });
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    // Метод для отримання списку вподобаних машин
    public List<Car> getFavoriteList() {
        return favoriteList;
    }

    public static class CarViewHolder extends RecyclerView.ViewHolder {

        TextView nameTextView;
        ImageView carImageView;
        ImageButton heartButton;

        @SuppressLint("WrongViewCast")
        public CarViewHolder(View itemView) {
            super(itemView);
            // Ініціалізація елементів UI
            nameTextView = itemView.findViewById(R.id.car_name);
            carImageView = itemView.findViewById(R.id.car_image);
            heartButton = itemView.findViewById(R.id.heart_button);
        }
    }
}
