package com.talksy.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.talksy.R;
import com.talksy.activities.GroupChatActivity;
import com.talksy.adapters.GroupAdapter;
import com.talksy.models.Group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupsFragment extends Fragment {

	private final List<Group> groupList = new ArrayList<>();
	private RecyclerView recyclerView;
	private GroupAdapter adapter;
	private FirebaseFirestore db;
	private String currentUserId;
	private ListenerRegistration groupListener;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_groups, container, false);

		recyclerView = root.findViewById(R.id.recyclerGroups);
		FloatingActionButton fabCreate = root.findViewById(R.id.fabCreateGroup);

		db = FirebaseFirestore.getInstance();
		currentUserId = FirebaseAuth.getInstance().getUid();

		// ✅ Setup adapter with delete support
		adapter = new GroupAdapter(groupList, requireContext(), new GroupAdapter.OnGroupActionListener() {
			@Override
			public void onGroupClick(Group group) {
				openGroupChat(group);
			}

			@Override
			public void onGroupEdit(Group group) {
				if (!group.isCreator(currentUserId)) {
					Toast.makeText(getContext(), "Only the group creator can edit this group.", Toast.LENGTH_SHORT).show();
					return;
				}
				showGroupDialog(group);
			}

			@Override
			public void onGroupDelete(Group group) {
				confirmDeleteGroup(group);
			}
		}, currentUserId);

		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		recyclerView.setAdapter(adapter);

		if (currentUserId != null) {
			loadGroups();
		} else {
			Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
		}

		fabCreate.setOnClickListener(v -> {
			if (currentUserId != null) {
				showGroupDialog(null);
			} else {
				Toast.makeText(getContext(), "Login required", Toast.LENGTH_SHORT).show();
			}
		});

		return root;
	}

	/**
	 * ========== REAL-TIME GROUP LOADING ==========
	 */
	private void loadGroups() {
		if (groupListener != null) groupListener.remove();

		groupListener = db.collection("groups")
				.whereArrayContains("participants", currentUserId)
				.addSnapshotListener((snapshots, e) -> {
					if (e != null) {
						Toast.makeText(getContext(), "Failed to load groups", Toast.LENGTH_SHORT).show();
						return;
					}

					groupList.clear();
					if (snapshots != null) {
						for (DocumentSnapshot doc : snapshots.getDocuments()) {
							Group group = doc.toObject(Group.class);
							if (group != null) {
								group.setId(doc.getId());
								groupList.add(group);
							}
						}
						// ✅ Sort by timestamp (latest first)
						groupList.sort((g1, g2) -> Long.compare(g2.getTimestamp(), g1.getTimestamp()));
					}
					adapter.notifyDataSetChanged();
				});
	}

	/**
	 * ========== CREATE OR EDIT GROUP ==========
	 */
	private void showGroupDialog(Group groupToEdit) {
		Context context = requireContext();
		View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_create_group, null);

		EditText inputGroupName = dialogView.findViewById(R.id.inputGroupName);
		TextView selectedUsersText = dialogView.findViewById(R.id.selectedUsersText);
		Button btnSelectUsers = dialogView.findViewById(R.id.btnSelectUsers);

		if (groupToEdit != null) inputGroupName.setText(groupToEdit.getName());

		List<String> allUserNames = new ArrayList<>();
		List<String> allUserIds = new ArrayList<>();
		List<String> selectedUserIds = new ArrayList<>(groupToEdit != null ? groupToEdit.getParticipants() : new ArrayList<>());

		db.collection("users").get().addOnSuccessListener(snapshot -> {
			for (DocumentSnapshot doc : snapshot.getDocuments()) {
				String uid = doc.getId();
				String name = doc.getString("name");
				allUserIds.add(uid);
				allUserNames.add(name != null ? name : "User");
			}

			btnSelectUsers.setOnClickListener(v -> {
				boolean[] checkedItems = new boolean[allUserIds.size()];
				for (int i = 0; i < allUserIds.size(); i++) {
					checkedItems[i] = selectedUserIds.contains(allUserIds.get(i));
				}

				new AlertDialog.Builder(context)
						.setTitle("Select Members")
						.setMultiChoiceItems(allUserNames.toArray(new String[0]), checkedItems, (dialog, index, isChecked) -> {
							String uid = allUserIds.get(index);
							if (isChecked) {
								if (!selectedUserIds.contains(uid)) selectedUserIds.add(uid);
							} else {
								selectedUserIds.remove(uid);
							}
						})
						.setPositiveButton("Done", (dialog, which) ->
								selectedUsersText.setText("Selected: " + selectedUserIds.size()))
						.setNegativeButton("Cancel", null)
						.show();
			});

			new AlertDialog.Builder(context)
					.setTitle(groupToEdit == null ? "Create Group" : "Edit Group")
					.setView(dialogView)
					.setPositiveButton(groupToEdit == null ? "Create" : "Update", (dialog, which) -> {
						String groupName = inputGroupName.getText().toString().trim();
						if (TextUtils.isEmpty(groupName)) {
							Toast.makeText(context, "Group name required", Toast.LENGTH_SHORT).show();
							return;
						}
						if (!selectedUserIds.contains(currentUserId)) {
							selectedUserIds.add(currentUserId); // always include self
						}
						if (groupToEdit == null) {
							createGroup(groupName, selectedUserIds);
						} else {
							updateGroup(groupToEdit.getId(), groupName, selectedUserIds);
						}
					})
					.setNegativeButton("Cancel", null)
					.show();
		});
	}

	/**
	 * ========== CREATE GROUP ==========
	 */
	private void createGroup(String name, List<String> participants) {
		Map<String, Object> groupMap = new HashMap<>();
		groupMap.put("name", name);
		groupMap.put("participants", participants);
		groupMap.put("creatorId", currentUserId); // ✅ Save the creator
		groupMap.put("timestamp", System.currentTimeMillis());

		db.collection("groups")
				.add(groupMap)
				.addOnSuccessListener(doc ->
						Toast.makeText(getContext(), "Group created!", Toast.LENGTH_SHORT).show()
				)
				.addOnFailureListener(e ->
						Toast.makeText(getContext(), "Error creating group", Toast.LENGTH_SHORT).show()
				);
	}

	/**
	 * ========== UPDATE GROUP ==========
	 */
	private void updateGroup(String groupId, String newName, List<String> participants) {
		Map<String, Object> updates = new HashMap<>();
		updates.put("name", newName);
		updates.put("participants", participants);
		updates.put("timestamp", System.currentTimeMillis());

		db.collection("groups").document(groupId)
				.update(updates)
				.addOnSuccessListener(unused ->
						Toast.makeText(getContext(), "Group updated", Toast.LENGTH_SHORT).show()
				)
				.addOnFailureListener(e ->
						Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show()
				);
	}

	/**
	 * ========== DELETE GROUP ==========
	 */
	private void confirmDeleteGroup(Group group) {
		if (!group.isCreator(currentUserId)) {
			Toast.makeText(getContext(), "Only the creator can delete this group.", Toast.LENGTH_SHORT).show();
			return;
		}

		new AlertDialog.Builder(requireContext())
				.setTitle("Delete Group")
				.setMessage("Are you sure you want to delete \"" + group.getName() + "\"?")
				.setPositiveButton("Delete", (dialog, which) -> {
					db.collection("groups").document(group.getId())
							.delete()
							.addOnSuccessListener(unused -> {
								Toast.makeText(getContext(), group.getName() + " has been deleted", Toast.LENGTH_LONG).show();
							})
							.addOnFailureListener(e ->
									Toast.makeText(getContext(), "Failed to delete group", Toast.LENGTH_SHORT).show()
							);
				})
				.setNegativeButton("Cancel", null)
				.show();
	}

	/**
	 * ========== OPEN GROUP CHAT ==========
	 */
	private void openGroupChat(Group group) {
		Intent intent = new Intent(getContext(), GroupChatActivity.class);
		intent.putExtra("chatId", group.getId());
		startActivity(intent);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (groupListener != null) groupListener.remove();
	}
}
