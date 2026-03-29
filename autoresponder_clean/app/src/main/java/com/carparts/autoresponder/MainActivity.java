package com.carparts.autoresponder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvStatus, tvRepliedCount, tvLog;
    private Switch swEnabled;
    private CardView cardPermission;
    private int repliedCount = 0;

    private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String sender  = intent.getStringExtra("sender");
            String message = intent.getStringExtra("message");
            String reply   = intent.getStringExtra("reply");
            repliedCount++;
            updateLog(sender, message, reply);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus       = findViewById(R.id.tvStatus);
        tvRepliedCount = findViewById(R.id.tvRepliedCount);
        tvLog          = findViewById(R.id.tvLog);
        swEnabled      = findViewById(R.id.swEnabled);
        cardPermission = findViewById(R.id.cardPermission);

        // Settings button
        findViewById(R.id.btnSettings).setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));

        // Grant permission button
        findViewById(R.id.btnGrantPermission).setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)));

        // Main toggle
        swEnabled.setOnCheckedChangeListener((btn, isChecked) -> {
            SharedPrefsHelper.setEnabled(this, isChecked);
            updateStatus();
        });

        swEnabled.setChecked(SharedPrefsHelper.isEnabled(this));
        updateStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
        registerReceiver(messageReceiver,
                new IntentFilter("com.carparts.autoresponder.MESSAGE_LOGGED"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        try { unregisterReceiver(messageReceiver); } catch (Exception ignored) {}
    }

    private boolean hasNotificationAccess() {
        String enabledListeners = Settings.Secure.getString(
                getContentResolver(), "enabled_notification_listeners");
        return enabledListeners != null &&
                enabledListeners.contains(getPackageName());
    }

    private void updateStatus() {
        boolean hasPermission = hasNotificationAccess();
        boolean isEnabled     = swEnabled.isChecked();

        cardPermission.setVisibility(hasPermission ? View.GONE : View.VISIBLE);

        if (!hasPermission) {
            tvStatus.setText("⚠️ Permission Required");
            tvStatus.setTextColor(getColor(R.color.warning));
        } else if (isEnabled) {
            tvStatus.setText("🟢 Active — Watching Messenger");
            tvStatus.setTextColor(getColor(R.color.active));
        } else {
            tvStatus.setText("⏸ Paused");
            tvStatus.setTextColor(getColor(R.color.paused));
        }

        tvRepliedCount.setText(String.valueOf(repliedCount));
    }

    private void updateLog(String sender, String message, String reply) {
        runOnUiThread(() -> {
            String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
            String entry = "[" + time + "] 👤 " + sender + "\n"
                    + "💬 " + message + "\n"
                    + "🤖 " + reply + "\n"
                    + "─────────────────\n";

            String current = tvLog.getText().toString();
            tvLog.setText(entry + current);
            tvRepliedCount.setText(String.valueOf(repliedCount));
        });
    }
}
