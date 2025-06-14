package com.example.stepler;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        if ("unauthorized_access".equals(remoteMessage.getData().get("type"))) {
            NotificationCompat.Builder b = new NotificationCompat.Builder(this, "security_alerts")
                    .setSmallIcon(R.drawable.ic_warning)
                    .setContentTitle("Тревога!")
                    .setContentText("Несанкционированный доступ к автомобилю!")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);
            NotificationManagerCompat.from(this).notify(1001, b.build());
        }
    }
}
