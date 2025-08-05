package com.talksy.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.talksy.R;

public class MyFirebaseService extends FirebaseMessagingService {

	private static final String CHANNEL_ID = "default_channel";

	@Override
	public void onMessageReceived(@NonNull RemoteMessage message) {
		super.onMessageReceived(message);

		// Create notification channel for Android O+
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel(
					CHANNEL_ID,
					"Default Notifications",
					NotificationManager.IMPORTANCE_HIGH
			);
			NotificationManager manager = getSystemService(NotificationManager.class);
			manager.createNotificationChannel(channel);
		}

		// ✅ Handle Notification payload
		if (message.getNotification() != null) {
			showNotification(
					message.getNotification().getTitle(),
					message.getNotification().getBody()
			);
		}

		// ✅ Handle Data payload
		if (!message.getData().isEmpty()) {
			String chatId = message.getData().get("chatId");
			String msgText = message.getData().get("message");
			if (msgText != null) {
				showNotification("New Chat Message", msgText);
			}
		}
	}

	private void showNotification(String title, String body) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
				.setSmallIcon(R.drawable.bell)  // Use your app's icon
				.setContentTitle(title)
				.setContentText(body)
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setAutoCancel(true);

		NotificationManagerCompat.from(this)
				.notify((int) System.currentTimeMillis(), builder.build());
	}

	@Override
	public void onNewToken(@NonNull String token) {
		super.onNewToken(token);
		// Log token for testing
		android.util.Log.d("FCM_TOKEN", "New token: " + token);
		// Optionally send token to your server here
	}
}
