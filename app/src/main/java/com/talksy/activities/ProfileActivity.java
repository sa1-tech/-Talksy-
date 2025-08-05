package com.talksy.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.talksy.R;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

	private EditText inputName;
	private Button btnSave;
	private ImageView imgProfile;
	private FirebaseFirestore db;
	private FirebaseStorage storage;
	private String uid;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);

		inputName = findViewById(R.id.inputProfileName);
		btnSave = findViewById(R.id.btnProfileSave);
		imgProfile = findViewById(R.id.imgProfile);

		db = FirebaseFirestore.getInstance();
		storage = FirebaseStorage.getInstance();
		uid = FirebaseAuth.getInstance().getUid();

		if (uid == null) {
			Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		loadUserProfile();

		// Now clicking the profile image just shows a toast
		imgProfile.setOnClickListener(v ->
				Toast.makeText(this, "This is default", Toast.LENGTH_SHORT).show()
		);

		// Save profile without changing image
		btnSave.setOnClickListener(v -> saveProfile(null));
	}

	private void loadUserProfile() {
		db.collection("users").document(uid)
				.get()
				.addOnSuccessListener(this::populateUserData)
				.addOnFailureListener(e ->
						Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show());
	}

	private void populateUserData(DocumentSnapshot snapshot) {
		if (snapshot.exists()) {
			String name = snapshot.getString("name");
			String profilePic = snapshot.getString("profilePic");

			if (!TextUtils.isEmpty(name)) {
				inputName.setText(name);
			}

			if (!TextUtils.isEmpty(profilePic)) {
				Glide.with(this).load(profilePic)
						.placeholder(R.drawable.img)
						.into(imgProfile);
			}
		}
	}

	private void saveProfile(@Nullable String imageUrl) {
		String newName = inputName.getText().toString().trim();
		if (TextUtils.isEmpty(newName)) {
			inputName.setError("Name cannot be empty");
			return;
		}

		Map<String, Object> updates = new HashMap<>();
		updates.put("name", newName);

		// Only update name; profilePic is unchanged
		db.collection("users").document(uid)
				.update(updates)
				.addOnSuccessListener(unused ->
						Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show())
				.addOnFailureListener(e ->
						Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show());
	}
}
