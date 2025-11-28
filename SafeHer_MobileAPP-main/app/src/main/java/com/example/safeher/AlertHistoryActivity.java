package com.example.safeher;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AlertHistoryActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "SafeHerPrefs";
    private static final String ALERT_HISTORY_KEY = "AlertHistory";

    private RecyclerView recyclerViewAlerts;
    private AlertHistoryAdapter alertHistoryAdapter;
    private List<Alert> alertList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_history);

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        recyclerViewAlerts = findViewById(R.id.recyclerViewAlerts);
        loadAlerts();
        alertHistoryAdapter = new AlertHistoryAdapter(alertList);
        recyclerViewAlerts.setAdapter(alertHistoryAdapter);
    }

    private void loadAlerts() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(ALERT_HISTORY_KEY, null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<Alert>>() {}.getType();
            alertList = new Gson().fromJson(json, type);
            Collections.reverse(alertList); // Show most recent alerts first
        }
    }
}
