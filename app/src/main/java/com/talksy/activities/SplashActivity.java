package com.talksy.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.talksy.R;

public class SplashActivity extends AppCompatActivity {

	private static final int SPLASH_DELAY = 1000; // 2 seconds

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		new Handler(Looper.getMainLooper()).postDelayed(() -> {
			if (FirebaseAuth.getInstance().getCurrentUser() != null) {
				// User already logged in
				startActivity(new Intent(SplashActivity.this, MainActivity.class));
			} else {
				startActivity(new Intent(SplashActivity.this, LoginActivity.class));
			}
			finish();
		}, SPLASH_DELAY);
	}
}
