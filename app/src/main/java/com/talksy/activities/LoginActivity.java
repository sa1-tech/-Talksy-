package com.talksy.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.talksy.R;

public class LoginActivity extends AppCompatActivity {

	private EditText emailInput, passwordInput;
	private Button loginBtn;
	private TextView registerLink;

	private FirebaseAuth mAuth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		mAuth = FirebaseAuth.getInstance();

		emailInput = findViewById(R.id.inputEmail);
		passwordInput = findViewById(R.id.inputPassword);
		loginBtn = findViewById(R.id.btnLogin);
		registerLink = findViewById(R.id.linkRegister);

		loginBtn.setOnClickListener(v -> loginUser());
		registerLink.setOnClickListener(v -> {
			startActivity(new Intent(this, RegisterActivity.class));
			finish();
		});
	}

	private void loginUser() {
		String email = emailInput.getText().toString().trim();
		String pass = passwordInput.getText().toString().trim();

		if (email.isEmpty() || pass.isEmpty()) {
			Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
			return;
		}

		mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
			if (task.isSuccessful()) {
				startActivity(new Intent(this, MainActivity.class));
				finish();
			} else {
				Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
			}
		});
	}
}
