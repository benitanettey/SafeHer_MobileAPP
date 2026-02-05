package com.example.safeher;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AlertHistoryAdapter extends RecyclerView.Adapter<AlertHistoryAdapter.AlertViewHolder> {

    private List<Alert> alertList;

    public AlertHistoryAdapter(List<Alert> alertList) {
        this.alertList = alertList;
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_alert, parent, false);
        return new AlertViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        Alert alert = alertList.get(position);
        holder.alertMessage.setText(alert.getMessage());
        
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy • h:mm a", Locale.getDefault());
        String timestamp = sdf.format(new Date(alert.getTimestamp()));
        holder.alertTimestamp.setText("Sent: " + timestamp);
    }

    @Override
    public int getItemCount() {
        return alertList.size();
    }

    public static class AlertViewHolder extends RecyclerView.ViewHolder {
        TextView alertMessage, alertTimestamp;

        public AlertViewHolder(@NonNull View itemView) {
            super(itemView);
            alertMessage = itemView.findViewById(R.id.alertMessage);
            alertTimestamp = itemView.findViewById(R.id.alertTimestamp);
        }
    }
}
