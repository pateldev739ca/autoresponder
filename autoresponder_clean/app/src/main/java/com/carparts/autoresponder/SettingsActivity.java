package com.carparts.autoresponder;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private EditText etApiKey, etBusinessName, etCity, etNote;
    private static final String PREFS = "car_parts_prefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        etApiKey       = findViewById(R.id.etApiKey);
        etBusinessName = findViewById(R.id.etBusinessName);
        etCity         = findViewById(R.id.etCity);
        etNote         = findViewById(R.id.etNote);

        // Load saved values
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        etApiKey.setText(prefs.getString("api_key", ""));
        etBusinessName.setText(prefs.getString("biz_name", "Dev's Auto Parts"));
        etCity.setText(prefs.getString("biz_city", ""));
        etNote.setText(prefs.getString("biz_note", ""));

        findViewById(R.id.btnSave).setOnClickListener(v -> {
            String apiKey = etApiKey.getText().toString().trim();
            String bizName = etBusinessName.getText().toString().trim();
            String city   = etCity.getText().toString().trim();
            String note   = etNote.getText().toString().trim();

            if (apiKey.isEmpty()) {
                Toast.makeText(this, "Please enter your API key", Toast.LENGTH_SHORT).show();
                return;
            }

            prefs.edit()
                 .putString("api_key",  apiKey)
                 .putString("biz_name", bizName)
                 .putString("biz_city", city)
                 .putString("biz_note", note)
                 .apply();

            Toast.makeText(this, "✅ Settings saved!", Toast.LENGTH_SHORT).show();
            finish();
        });

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    // Static helpers so NotificationService can read settings
    public static String getApiKey(Context ctx) {
        return ctx.getSharedPreferences(PREFS, MODE_PRIVATE).getString("api_key", "");
    }

    public static String getBizName(Context ctx) {
        return ctx.getSharedPreferences(PREFS, MODE_PRIVATE).getString("biz_name", "Dev's Auto Parts");
    }

    public static String getBizCity(Context ctx) {
        return ctx.getSharedPreferences(PREFS, MODE_PRIVATE).getString("biz_city", "");
    }
}
