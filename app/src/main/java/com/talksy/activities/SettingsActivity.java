package com.talksy.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.talksy.R;

public class SettingsActivity extends AppCompatActivity {

	private Switch switchDarkMode, switchPrivacyMode;
	private Button btnLogout;
	private SharedPreferences prefs;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		// Bind UI
		switchDarkMode = findViewById(R.id.switchDarkMode);
		switchPrivacyMode = findViewById(R.id.switchPrivacy);
		btnLogout = findViewById(R.id.btnLogout);

		prefs = getSharedPreferences("app_settings", MODE_PRIVATE);

		// Load saved preferences
		boolean darkModeEnabled = prefs.getBoolean("dark_mode", false);
		boolean privacyEnabled = prefs.getBoolean("privacy_mode", false);

		// Apply switch states
		switchDarkMode.setChecked(darkModeEnabled);
		switchPrivacyMode.setChecked(privacyEnabled);

		// Apply initial states
		applyDarkMode(darkModeEnabled);
		applyPrivacyMode(privacyEnabled);

		/** ========== DARK MODE LISTENER ========== */
		switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
			prefs.edit().putBoolean("dark_mode", isChecked).apply();
			applyDarkMode(isChecked);
			recreate(); // Instant theme update
		});

		/** ========== PRIVACY MODE LISTENER ========== */
		switchPrivacyMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
			prefs.edit().putBoolean("privacy_mode", isChecked).apply();
			applyPrivacyMode(isChecked);
			Toast.makeText(this, "Privacy mode " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
		});

		/** ========== LOGOUT BUTTON ========== */
		btnLogout.setOnClickListener(v -> {
			FirebaseAuth.getInstance().signOut();
			Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
			startActivity(new Intent(this, LoginActivity.class));
			finishAffinity();
		});
	}

	/**
	 * ================== DARK MODE ==================
	 */
	private void applyDarkMode(boolean enabled) {
		AppCompatDelegate.setDefaultNightMode(
				enabled ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
		);
	}

	/**
	 * ================== PRIVACY MODE ==================
	 */
	private void applyPrivacyMode(boolean enabled) {
		if (enabled) {
			getWindow().setFlags(
					android.view.WindowManager.LayoutParams.FLAG_SECURE,
					android.view.WindowManager.LayoutParams.FLAG_SECURE
			);
		} else {
			getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE);
		}
	}
}
