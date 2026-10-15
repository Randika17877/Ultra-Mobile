package lk.randika.ultramobile.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.HashMap;
import java.util.Map;
import lk.randika.ultramobile.R;
import lk.randika.ultramobile.activity.MainActivity;
import lk.randika.ultramobile.receiver.OrderStatusReceiver;

public class UltraMobileMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> data = remoteMessage.getData();
        String orderId  = data.get("orderId");
        String status   = data.get("status");

        // 1. Fire local broadcast so any open OrdersFragment/OrderDetailFragment refreshes
        if (orderId != null) {
            Intent broadcastIntent = new Intent(OrderStatusReceiver.ACTION_ORDER_UPDATED);
            broadcastIntent.putExtra(OrderStatusReceiver.EXTRA_ORDER_ID, orderId);
            broadcastIntent.putExtra(OrderStatusReceiver.EXTRA_NEW_STATUS, status);
            LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
        }

        // 2. Show system notification
        if (remoteMessage.getNotification() != null) {
            sendNotification(
                    remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody(),
                    orderId);
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        updateTokenInFirestore(token);
    }

    private void updateTokenInFirestore(String token) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId != null) {
            Map<String, Object> tokenMap = new HashMap<>();
            tokenMap.put("fcmToken", token);
            FirebaseFirestore.getInstance().collection("users").document(userId)
                    .update(tokenMap);
        }
    }

    private void sendNotification(String title, String messageBody, String orderId) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // Embed orderId so MainActivity can deep-link into OrderDetailFragment
        if (orderId != null) {
            intent.putExtra("navigate_to", "order_detail");
            intent.putExtra("orderId", orderId);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        String channelId = "order_updates";
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.list_24px) // Ensure this exists
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Order Status Updates",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }
}
