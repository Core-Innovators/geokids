package com.coreinnovators.geokids;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "geokids_notifications";
    private static final String CHANNEL_NAME = "GeoKids Notifications";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "🔑 New FCM token generated: " + token);

        // Save token to Firestore for the logged-in user
        saveTokenToFirestore(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "📩 Message received from: " + remoteMessage.getFrom());

        // Check if message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();

            Map<String, String> data = remoteMessage.getData();

            Log.d(TAG, "📬 Notification - Title: " + title + ", Body: " + body);
            showNotification(title, body, data);
        }

        // Check if message contains a data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "📦 Message data payload: " + remoteMessage.getData());
            handleDataPayload(remoteMessage.getData());
        }
    }

    private void handleDataPayload(Map<String, String> data) {
        String type = data.get("type");
        String childName = data.get("childName");
        String driverName = data.get("driverName");

        Log.d(TAG, "🎯 Notification type: " + type + ", Child: " + childName + ", Driver: " + driverName);

        // You can add custom handling here based on notification type
        if ("pickup".equals(type)) {
            Log.d(TAG, "🚌 Child pickup notification received");
        } else if ("dropoff".equals(type)) {
            Log.d(TAG, "🏫 Child dropoff notification received");
        }
    }

    private void showNotification(String title, String message, Map<String, String> data) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Intent to open ParentActiveDashboard when notification is clicked
        Intent intent = new Intent(this, ParentActiveDashboard.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add data to intent
        if (data != null) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue());
            }
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build notification
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setColor(getResources().getColor(R.color.orange)); // #ED5316

        notificationManager.notify(
                (int) System.currentTimeMillis(),
                notificationBuilder.build()
        );
        
        Log.d(TAG, "✅ Notification displayed: " + title);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for child pickup and dropoff");
            channel.enableVibration(true);
            channel.setShowBadge(true);

            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "📱 Notification channel created: " + CHANNEL_ID);
            }
        }
    }

    /**
     * Save FCM token to Firestore for the current user
     * This is called when a new token is generated
     */
    private void saveTokenToFirestore(String token) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Log.w(TAG, "⚠️ Cannot save FCM token - No user logged in");
            return;
        }

        String userId = currentUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Log.d(TAG, "💾 Saving FCM token to Firestore for user: " + userId);

        // Try to update the parents collection first (for parent users)
        db.collection("parents").document(userId)
                .update("fcmToken", token)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ FCM token saved to parents collection (by doc ID)");
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "Parent doc not found by ID, trying query...");
                    // If document doesn't exist with this ID, query by parentId field
                    saveTokenByParentIdQuery(db, userId, token);
                });
    }

    /**
     * Fallback: Query parents collection by parentId field and update
     */
    private void saveTokenByParentIdQuery(FirebaseFirestore db, String userId, String token) {
        db.collection("parents")
                .whereEqualTo("parentId", userId)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String docId = querySnapshot.getDocuments().get(0).getId();
                        db.collection("parents").document(docId)
                                .update("fcmToken", token)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "✅ FCM token saved to parents collection (by query)");
                                })
                                .addOnFailureListener(err -> {
                                    Log.e(TAG, "❌ Failed to update FCM token: " + err.getMessage());
                                });
                    } else {
                        // No parent document found, might be a driver
                        Log.d(TAG, "No parent found, trying drivers collection...");
                        saveTokenToDrivers(db, userId, token);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error querying parents: " + e.getMessage());
                    // Try drivers collection as fallback
                    saveTokenToDrivers(db, userId, token);
                });
    }

    /**
     * Save FCM token to drivers collection (for driver users)
     */
    private void saveTokenToDrivers(FirebaseFirestore db, String userId, String token) {
        db.collection("drivers").document(userId)
                .update("fcmToken", token)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ FCM token saved to drivers collection");
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "⚠️ Could not save FCM token to any collection: " + e.getMessage());
                });
    }

    /**
     * Static method to manually save FCM token from other activities
     * Call this after user login to ensure token is saved
     */
    public static void saveFCMToken() {
        com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {
                    Log.d(TAG, "🔑 Got FCM token: " + token);
                    
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    FirebaseUser currentUser = auth.getCurrentUser();
                    
                    if (currentUser != null) {
                        String userId = currentUser.getUid();
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        
                        // Update parents collection
                        db.collection("parents").document(userId)
                                .update("fcmToken", token)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "✅ FCM token saved after login");
                                })
                                .addOnFailureListener(e -> {
                                    // Try query by parentId
                                    db.collection("parents")
                                            .whereEqualTo("parentId", userId)
                                            .limit(1)
                                            .get()
                                            .addOnSuccessListener(querySnapshot -> {
                                                if (!querySnapshot.isEmpty()) {
                                                    String docId = querySnapshot.getDocuments().get(0).getId();
                                                    db.collection("parents").document(docId)
                                                            .update("fcmToken", token);
                                                }
                                            });
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Failed to get FCM token: " + e.getMessage());
                });
    }
}