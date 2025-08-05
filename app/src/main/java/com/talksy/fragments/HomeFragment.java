package com.talksy.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.talksy.R;
import com.talksy.activities.ChatActivity;
import com.talksy.adapters.UserAdapter;
import com.talksy.models.User;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements UserAdapter.OnUserActionListener {

	private final List<User> users = new ArrayList<>();
	private RecyclerView userRecycler;
	private UserAdapter adapter;
	private FirebaseFirestore db;
	private String currentUid;

	// ✅ For group delete functionality
	private String groupCreatorId = ""; // Assign dynamically if needed

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_home, container, false);

		// Initialize RecyclerView
		userRecycler = root.findViewById(R.id.recyclerUsers);
		userRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

		// Initialize Firebase
		db = FirebaseFirestore.getInstance();
		currentUid = FirebaseAuth.getInstance().getUid();

		// TODO: Set groupCreatorId dynamically if this fragment is for a group
		// e.g., groupCreatorId = getArguments().getString("creatorId", currentUid);

		// Initialize adapter with delete support
		adapter = new UserAdapter(requireContext(), users, this, currentUid, groupCreatorId);
		userRecycler.setAdapter(adapter);

		// Load users
		loadUsers();

		return root;
	}

	/**
	 * Load all users except the current logged-in user
	 */
	private void loadUsers() {
		db.collection("users")
				.orderBy("name", Query.Direction.ASCENDING)
				.get()
				.addOnSuccessListener(this::onUsersFetched)
				.addOnFailureListener(e -> {
					Toast.makeText(getContext(), "Failed to load users", Toast.LENGTH_SHORT).show();
				});
	}

	/**
	 * Populate RecyclerView with fetched users
	 */
	private void onUsersFetched(QuerySnapshot snap) {
		users.clear();
		if (snap != null && !snap.isEmpty()) {
			for (DocumentSnapshot doc : snap.getDocuments()) {
				User user = doc.toObject(User.class);
				if (user != null && user.getUid() != null && !user.getUid().equals(currentUid)) {
					users.add(user);
				}
			}
		}
		adapter.notifyDataSetChanged();
	}

	/**
	 * ✅ On user click → Open ChatActivity
	 */
	@Override
	public void onUserClick(User user) {
		if (currentUid == null || user == null || user.getUid() == null) return;

		// Create a unique chatId based on both UIDs
		String chatId = currentUid.compareTo(user.getUid()) < 0
				? currentUid + "_" + user.getUid()
				: user.getUid() + "_" + currentUid;

		// Launch ChatActivity with required data
		Intent intent = new Intent(getContext(), ChatActivity.class);
		intent.putExtra("chatId", chatId);
		intent.putExtra("otherUserId", user.getUid()); // ✅ Required for Agora calls
		startActivity(intent);
	}

	/**
	 * ✅ On user delete → Remove from RecyclerView + show toast
	 */
	@Override
	public void onUserDelete(User user) {
		if (!currentUid.equals(groupCreatorId)) {
			Toast.makeText(getContext(), "Only group creator can remove users", Toast.LENGTH_SHORT).show();
			return;
		}

		users.remove(user);
		adapter.notifyDataSetChanged();

		// ✅ Simple in-app notification (not Firebase)
		Toast.makeText(getContext(), user.getName() + " has been removed from the group", Toast.LENGTH_LONG).show();

		// 🔹 Optional: Update Firestore if you want to persist removal
		// db.collection("groups").document(groupId)
		//     .update("members", FieldValue.arrayRemove(user.getUid()));
	}
}
