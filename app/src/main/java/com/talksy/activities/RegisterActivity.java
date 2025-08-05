package com.talksy.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.talksy.R;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

	private EditText nameInput, emailInput, passwordInput;
	private Button registerBtn;
	private TextView loginLink;

	private FirebaseAuth mAuth;
	private FirebaseFirestore firestore;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		mAuth = FirebaseAuth.getInstance();
		firestore = FirebaseFirestore.getInstance();

		nameInput = findViewById(R.id.inputName);
		emailInput = findViewById(R.id.inputEmail);
		passwordInput = findViewById(R.id.inputPassword);
		registerBtn = findViewById(R.id.btnRegister);
		loginLink = findViewById(R.id.linkLogin);

		registerBtn.setOnClickListener(v -> registerUser());
		loginLink.setOnClickListener(v -> {
			startActivity(new Intent(this, LoginActivity.class));
			finish();
		});
	}

	private void registerUser() {
		String name = nameInput.getText().toString().trim();
		String email = emailInput.getText().toString().trim();
		String pass = passwordInput.getText().toString().trim();

		if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
			Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
			return;
		}

		mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
			if (task.isSuccessful()) {
				String uid = task.getResult().getUser().getUid();
				Map<String, Object> user = new HashMap<>();
				user.put("uid", uid);
				user.put("name", name);
				user.put("email", email);
				user.put("profileImage", ""); // empty for now

				firestore.collection("users").document(uid).set(user);

				startActivity(new Intent(this, MainActivity.class));
				finish();
			} else {
				Toast.makeText(this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
			}
		});
	}
}
