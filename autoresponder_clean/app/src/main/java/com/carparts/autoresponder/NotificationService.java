package com.carparts.autoresponder;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationService extends NotificationListenerService {

    private static final String TAG = "CarPartsAutoReply";

    // Facebook Messenger package names
    private static final String MESSENGER_PKG = "com.facebook.orca";
    private static final String FB_PKG = "com.facebook.katana";

    private final ExecutorService executor = Executors.newFixedThreadPool(3);

    // ─── EXHAUST AUTO BUSINESS INFO ─────────────────────────────
    private static final String BUSINESS_NAME    = "Exhaust Auto";
    private static final String BUSINESS_WEBSITE = "https://exhaustauto.com";
    private static final String BUSINESS_ADDRESS = "";
    private static final String BUSINESS_PHONE   = "+1 (437) 838-2886";
    private static final String BUSINESS_EMAIL   = "Sales@exhaustauto.com";
    private static final String SHIPPING_POLICY  = "We ship to both Canada and USA. Free shipping available. Up to 35% off on many items.";

    // Product category links per make — sent when customer asks about a specific make
    private static final String PRODUCT_LINKS =
        "CATALYTIC CONVERTERS by make:\n" +
        "- Toyota: https://exhaustauto.com/collections/universal-fit-catalytic-converter/toyota\n" +
        "- Honda: https://exhaustauto.com/collections/universal-fit-catalytic-converter/honda\n" +
        "- Ford: https://exhaustauto.com/collections/universal-fit-catalytic-converter/ford\n" +
        "- Nissan: https://exhaustauto.com/collections/universal-fit-catalytic-converter/nissan\n" +
        "- Mazda: https://exhaustauto.com/collections/universal-fit-catalytic-converter/mazda\n" +
        "- Subaru: https://exhaustauto.com/collections/universal-fit-catalytic-converter/subaru\n" +
        "- BMW: https://exhaustauto.com/collections/universal-fit-catalytic-converter/bmw\n" +
        "- Chevrolet: https://exhaustauto.com/collections/universal-fit-catalytic-converter/chevrolet\n" +
        "- Hyundai: https://exhaustauto.com/collections/universal-fit-catalytic-converter/hyundai\n" +
        "- Jeep: https://exhaustauto.com/collections/universal-fit-catalytic-converter/jeep\n" +
        "- Kia: https://exhaustauto.com/collections/universal-fit-catalytic-converter/kia\n" +
        "- Lexus: https://exhaustauto.com/collections/universal-fit-catalytic-converter/lexus\n" +
        "- Acura: https://exhaustauto.com/collections/universal-fit-catalytic-converter/acura\n" +
        "- Audi: https://exhaustauto.com/collections/universal-fit-catalytic-converter/audi\n" +
        "- Buick: https://exhaustauto.com/collections/universal-fit-catalytic-converter/buick\n" +
        "- Cadillac: https://exhaustauto.com/collections/universal-fit-catalytic-converter/cadillac\n" +
        "- Infiniti: https://exhaustauto.com/collections/universal-fit-catalytic-converter/infiniti\n" +
        "- VW/Volkswagen: https://exhaustauto.com/collections/universal-fit-catalytic-converter/volkswagen\n\n" +
        "MUFFLERS by make:\n" +
        "- Toyota: https://exhaustauto.com/collections/exhaust-muffler-for-car/toyota\n" +
        "- Honda: https://exhaustauto.com/collections/exhaust-muffler-for-car/honda\n" +
        "- Ford: https://exhaustauto.com/collections/exhaust-muffler-for-car/ford\n" +
        "- Nissan: https://exhaustauto.com/collections/exhaust-muffler-for-car/nissan\n" +
        "- Mazda: https://exhaustauto.com/collections/exhaust-muffler-for-car/mazda\n" +
        "- Subaru: https://exhaustauto.com/collections/exhaust-muffler-for-car/subaru\n" +
        "- BMW: https://exhaustauto.com/collections/exhaust-muffler-for-car/bmw\n" +
        "- Chevrolet: https://exhaustauto.com/collections/exhaust-muffler-for-car/chevrolet\n" +
        "- Hyundai: https://exhaustauto.com/collections/exhaust-muffler-for-car/hyundai\n" +
        "- Jeep: https://exhaustauto.com/collections/exhaust-muffler-for-car/jeep\n" +
        "- Kia: https://exhaustauto.com/collections/exhaust-muffler-for-car/kia\n" +
        "- Lexus: https://exhaustauto.com/collections/exhaust-muffler-for-car/lexus\n" +
        "- Acura: https://exhaustauto.com/collections/exhaust-muffler-for-car/acura\n\n" +
        "EXHAUST FLEX PIPE by make:\n" +
        "- Toyota: https://exhaustauto.com/collections/full-car-exhaust-system-parts/toyota\n" +
        "- Honda: https://exhaustauto.com/collections/full-car-exhaust-system-parts/honda\n" +
        "- Ford: https://exhaustauto.com/collections/full-car-exhaust-system-parts/ford\n" +
        "- Nissan: https://exhaustauto.com/collections/full-car-exhaust-system-parts/nissan\n" +
        "- Mazda: https://exhaustauto.com/collections/full-car-exhaust-system-parts/mazda\n" +
        "- Subaru: https://exhaustauto.com/collections/full-car-exhaust-system-parts/subaru\n" +
        "- BMW: https://exhaustauto.com/collections/full-car-exhaust-system-parts/bmw\n" +
        "- Chevrolet: https://exhaustauto.com/collections/full-car-exhaust-system-parts/chevrolet\n\n" +
        "EXHAUST ACCESSORIES: https://exhaustauto.com/collections/exhaust-pipe-flange-repair-kit\n" +
        "NEW ARRIVALS: https://exhaustauto.com/collections/new-arrivals\n" +
        "HOT DEALS: https://exhaustauto.com/collections/hot-deals\n" +
        "ALL PRODUCTS: https://exhaustauto.com/collections/all\n" +
        "UNIVERSAL CATALYTIC CONVERTER: https://exhaustauto.com/collections/universal-catalytic-converter\n";
    // ────────────────────────────────────────────────────────────

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String pkg = sbn.getPackageName();

        // Only handle Messenger / Facebook notifications
        if (!pkg.equals(MESSENGER_PKG) && !pkg.equals(FB_PKG)) return;

        Notification notification = sbn.getNotification();
        Bundle extras = notification.extras;
        if (extras == null) return;

        String title   = extras.getString(NotificationCompat.EXTRA_TITLE, "");
        String message = extras.getString(NotificationCompat.EXTRA_TEXT, "");

        if (message == null || message.isEmpty()) return;

        Log.d(TAG, "New message from: " + title + " → " + message);

        // Send to AI and get reply
        executor.execute(() -> {
            try {
                String aiReply = getAIReply(title, message);
                if (aiReply != null && !aiReply.isEmpty()) {
                    sendReply(sbn, aiReply);
                    broadcastMessageLogged(title, message, aiReply);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting AI reply: " + e.getMessage());
            }
        });
    }

    private String getAIReply(String senderName, String customerMessage) throws Exception {
        String apiKey = SettingsActivity.getApiKey(getApplicationContext());
        if (apiKey == null || apiKey.isEmpty()) {
            Log.w(TAG, "No API key configured");
            return null;
        }

        String systemPrompt = buildSystemPrompt();

        // Call OpenAI (ChatGPT)
        URL url = new URL("https://api.openai.com/v1/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(15000);

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "gpt-4o-mini");
        requestBody.put("max_tokens", 200);

        JSONArray messages = new JSONArray();
        messages.put(new JSONObject().put("role", "system").put("content", systemPrompt));
        messages.put(new JSONObject().put("role", "user").put("content",
                "Customer name: " + senderName + "\nCustomer message: " + customerMessage));
        requestBody.put("messages", messages);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestBody.toString().getBytes("UTF-8"));
        }

        int responseCode = conn.getResponseCode();
        BufferedReader reader;
        if (responseCode == 200) {
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            String error = readAll(reader);
            Log.e(TAG, "API error " + responseCode + ": " + error);
            return null;
        }

        String responseStr = readAll(reader);
        JSONObject response = new JSONObject(responseStr);
        return response
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim();
    }

    private String buildSystemPrompt() {
        return "You are a friendly, professional customer service assistant for " + BUSINESS_NAME +
                (BUSINESS_CITY.isEmpty() ? "" : ", located in " + BUSINESS_CITY) + ".\n\n" +
                "WEBSITE: https://exhaustauto.com\n\n" +
                "WHAT WE SELL — Exhaust parts for all major makes. Our 3 main product categories:\n" +
                "1. CATALYTIC CONVERTERS — universal fit, for: Toyota, Honda, Ford, Nissan, Mazda, Subaru, BMW, Audi, Chevy, Jeep, Kia, Hyundai, Lexus, Infiniti, VW, Buick, Cadillac, Acura\n" +
                "2. EXHAUST FLEX PIPES — stainless steel, universal fit, all makes above\n" +
                "3. MUFFLERS — all makes above\n\n" +
                "KEY PRODUCT LINKS TO SHARE:\n" +
                "- All products: https://exhaustauto.com/collections/all\n" +
                "- Catalytic Converters: https://exhaustauto.com/collections/universal-fit-catalytic-converter\n" +
                "- Exhaust Flex Pipes: https://exhaustauto.com/collections/full-car-exhaust-system-parts\n" +
                "- Mufflers: https://exhaustauto.com/collections/exhaust-muffler-for-car\n\n" +
                "MAKE-SPECIFIC LINKS (use these when customer mentions their car make):\n" +
                "Toyota cats: https://exhaustauto.com/collections/universal-fit-catalytic-converter/toyota\n" +
                "Honda cats: https://exhaustauto.com/collections/universal-fit-catalytic-converter/honda\n" +
                "Ford cats: https://exhaustauto.com/collections/universal-fit-catalytic-converter/ford\n" +
                "Nissan cats: https://exhaustauto.com/collections/universal-fit-catalytic-converter/nissan\n" +
                "Mazda cats: https://exhaustauto.com/collections/universal-fit-catalytic-converter/mazda\n" +
                "Subaru cats: https://exhaustauto.com/collections/universal-fit-catalytic-converter/subaru\n" +
                "BMW cats: https://exhaustauto.com/collections/universal-fit-catalytic-converter/bmw\n" +
                "Toyota flex: https://exhaustauto.com/collections/full-car-exhaust-system-parts/toyota\n" +
                "Honda flex: https://exhaustauto.com/collections/full-car-exhaust-system-parts/honda\n" +
                "Ford flex: https://exhaustauto.com/collections/full-car-exhaust-system-parts/ford\n" +
                "Toyota muffler: https://exhaustauto.com/collections/exhaust-muffler-for-car/toyota\n" +
                "Honda muffler: https://exhaustauto.com/collections/exhaust-muffler-for-car/Honda\n" +
                "Ford muffler: https://exhaustauto.com/collections/exhaust-muffler-for-car/ford\n\n" +
                "SHIPPING: We ship to USA and Canada. Currently up to 35% off + FREE shipping on orders.\n\n" +
                "RULES:\n" +
                "- Reply in 2-4 short sentences only. Be warm and friendly.\n" +
                "- ALWAYS include the relevant product link when customer asks about a specific part or make.\n" +
                "- If customer mentions their car make (e.g. Toyota), send the make-specific link for that part.\n" +
                "- If customer asks about shipping: confirm free shipping is available, share https://exhaustauto.com/collections/all\n" +
                "- If asked about catalytic converter: share the catalytic converter collection link.\n" +
                "- If asked about flex pipe: share the flex pipe collection link.\n" +
                "- If asked about muffler: share the muffler collection link.\n" +
                "- If unsure what part: share https://exhaustauto.com/collections/all\n" +
                "- NEVER make up prices or stock levels.\n" +
                "- Always ask for vehicle year/make/model if not provided.\n" +
                "- Sign off as: " + BUSINESS_NAME + " Team 🔧";
    }

    private String readAll(BufferedReader reader) throws Exception {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        return sb.toString();
    }

    private void sendReply(StatusBarNotification sbn, String replyText) {
        try {
            Notification.Action[] actions = sbn.getNotification().actions;
            if (actions == null) return;

            for (Notification.Action action : actions) {
                if (action.getRemoteInputs() == null) continue;

                Bundle inputBundle = new Bundle();
                for (android.app.RemoteInput remoteInput : action.getRemoteInputs()) {
                    inputBundle.putCharSequence(remoteInput.getResultKey(), replyText);
                }

                Intent fillIntent = new Intent();
                android.app.RemoteInput.addResultsToIntent(action.getRemoteInputs(), fillIntent, inputBundle);

                try {
                    action.actionIntent.send(getApplicationContext(), 0, fillIntent);
                    Log.d(TAG, "✅ Reply sent: " + replyText);
                    return;
                } catch (Exception e) {
                    Log.e(TAG, "Failed to send reply: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "sendReply error: " + e.getMessage());
        }
    }

    private void broadcastMessageLogged(String sender, String message, String reply) {
        Intent intent = new Intent("com.carparts.autoresponder.MESSAGE_LOGGED");
        intent.putExtra("sender", sender);
        intent.putExtra("message", message);
        intent.putExtra("reply", reply);
        sendBroadcast(intent);
    }
}
