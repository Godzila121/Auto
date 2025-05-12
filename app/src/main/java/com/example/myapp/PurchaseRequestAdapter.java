package com.example.myapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PurchaseRequestAdapter extends RecyclerView.Adapter<PurchaseRequestAdapter.PurchaseRequestViewHolder> {

    private Context context;
    private List<PurchaseRequest> requestList;

    public PurchaseRequestAdapter(Context context, List<PurchaseRequest> requestList) {
        this.context = context;
        this.requestList = requestList;
    }

    @NonNull
    @Override
    public PurchaseRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_purchase_request, parent, false);
        return new PurchaseRequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PurchaseRequestViewHolder holder, int position) {
        PurchaseRequest request = requestList.get(position);
        if (request != null) {
            holder.bind(request);
        }
    }

    @Override
    public int getItemCount() {
        return requestList != null ? requestList.size() : 0;
    }

    public void updateRequests(List<PurchaseRequest> newRequests) {
        this.requestList.clear();
        if (newRequests != null) {
            this.requestList.addAll(newRequests);
        }
        notifyDataSetChanged();
    }

    class PurchaseRequestViewHolder extends RecyclerView.ViewHolder {
        TextView textCarName, textBuyerEmail, textRequestStatus;
        Button buttonAccept, buttonDecline;
        LinearLayout layoutRequestActions; // Для показу/приховування кнопок

        PurchaseRequest currentRequest;

        public PurchaseRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            textCarName = itemView.findViewById(R.id.text_car_name_request);
            textBuyerEmail = itemView.findViewById(R.id.text_buyer_email_request);
            textRequestStatus = itemView.findViewById(R.id.text_request_status);
            buttonAccept = itemView.findViewById(R.id.button_accept_request);
            buttonDecline = itemView.findViewById(R.id.button_decline_request);
            layoutRequestActions = itemView.findViewById(R.id.layout_request_actions);

            buttonAccept.setOnClickListener(v -> {
                if (currentRequest == null || currentRequest.getRequestId() == null || currentRequest.getCarId() == null) return;

                new AlertDialog.Builder(context)
                        .setTitle("Підтвердження продажу")
                        .setMessage("Ви впевнені, що хочете продати автомобіль \"" + currentRequest.getCarName() + "\" користувачу " + currentRequest.getBuyerEmail() + "? Автомобіль буде видалено зі списку.")
                        .setPositiveButton("Так, продати", (dialog, which) -> {
                            FirebaseHelper.updatePurchaseRequestStatus(context, currentRequest.getRequestId(), "accepted", new FirebaseHelper.OnRequestUpdatedListener() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(context, "Продаж автомобіля \"" + currentRequest.getCarName() + "\" підтверджено!", Toast.LENGTH_SHORT).show();
                                    // Видаляємо автомобіль з Realtime Database
                                    FirebaseHelper.deleteCarFromRealtimeDatabase(context, currentRequest.getCarId());
                                    // Список запитів та список автомобілів користувача оновляться через ValueEventListeners в AccountActivity
                                }
                                @Override
                                public void onFailure(Exception e) {
                                    Toast.makeText(context, "Помилка підтвердження продажу: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        })
                        .setNegativeButton("Ні", null)
                        .show();
            });

            buttonDecline.setOnClickListener(v -> {
                if (currentRequest == null || currentRequest.getRequestId() == null) return;
                new AlertDialog.Builder(context)
                        .setTitle("Підтвердження відхилення")
                        .setMessage("Ви впевнені, що хочете відхилити запит на купівлю автомобіля \"" + currentRequest.getCarName() + "\"?")
                        .setPositiveButton("Так, відхилити", (dialog, which) -> {
                            FirebaseHelper.updatePurchaseRequestStatus(context, currentRequest.getRequestId(), "declined", new FirebaseHelper.OnRequestUpdatedListener() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(context, "Запит на купівлю \"" + currentRequest.getCarName() + "\" відхилено.", Toast.LENGTH_SHORT).show();
                                    // Список запитів оновиться через ValueEventListener в AccountActivity
                                }
                                @Override
                                public void onFailure(Exception e) {
                                    Toast.makeText(context, "Помилка відхилення запиту: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        })
                        .setNegativeButton("Ні", null)
                        .show();
            });
        }

        void bind(PurchaseRequest request) {
            this.currentRequest = request;
            textCarName.setText(request.getCarName() != null ? request.getCarName() : "Н/Д");
            textBuyerEmail.setText("Покупець: " + (request.getBuyerEmail() != null ? request.getBuyerEmail() : "Н/Д"));

            String statusDisplay;
            switch (request.getStatus()) {
                case "pending":
                    statusDisplay = "Статус: Очікується ваша відповідь";
                    layoutRequestActions.setVisibility(View.VISIBLE); // Показуємо кнопки для pending запитів
                    break;
                case "accepted":
                    statusDisplay = "Статус: Прийнято (Автомобіль продано)";
                    layoutRequestActions.setVisibility(View.GONE); // Ховаємо кнопки для вже оброблених
                    break;
                case "declined":
                    statusDisplay = "Статус: Відхилено";
                    layoutRequestActions.setVisibility(View.GONE); // Ховаємо кнопки
                    break;
                default:
                    statusDisplay = "Статус: " + request.getStatus();
                    layoutRequestActions.setVisibility(View.GONE);
                    break;
            }
            textRequestStatus.setText(statusDisplay);
        }
    }
}