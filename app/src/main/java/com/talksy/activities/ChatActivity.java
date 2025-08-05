package com.talksy.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.talksy.R;
import com.talksy.adapters.ChatAdapter;
import com.talksy.models.Message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

	private RecyclerView chatList;
	private ChatAdapter adapter;
	private EditText inputMessage;
	private ImageButton btnSend;

	private FirebaseFirestore db;
	private String chatId, userId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);

		initViews();
		initFirebase();
		validateChatSession();
		setupRecyclerView();
		listenForMessages();

		btnSend.setOnClickListener(v -> onSendClicked());
	}

	/**
	 * Initialize UI components
	 */
	private void initViews() {
		chatList = findViewById(R.id.chatRecycler);
		inputMessage = findViewById(R.id.inputMessage);
		btnSend = findViewById(R.id.btnSend);
	}

	/**
	 * Initialize Firebase references
	 */
	private void initFirebase() {
		db = FirebaseFirestore.getInstance();
		userId = FirebaseAuth.getInstance().getUid();
		chatId = getIntent().getStringExtra("chatId");
	}

	/**
	 * Validate chat session
	 */
	private void validateChatSession() {
		if (TextUtils.isEmpty(chatId) || userId == null) {
			Toast.makeText(this, "Invalid chat session", Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	/**
	 * Setup RecyclerView and Adapter
	 */
	private void setupRecyclerView() {
		adapter = new ChatAdapter(this, userId, new ChatAdapter.OnMessageActionListener() {
			@Override
			public void onMessageLongPress(Message message) {
				if (!message.getSenderId().equals(userId)) {
					Toast.makeText(ChatActivity.this, "You can only manage your messages", Toast.LENGTH_SHORT).show();
					return;
				}
				showMessageOptionsDialog(message); // ✅ Show delete/update dialog
			}

			@Override
			public void onMessageDoubleClick(Message message) {
				// Optional: Could trigger edit directly on double click
				if (message.getSenderId().equals(userId)) {
					editMessage(message);
				}
			}
		});

		chatList.setLayoutManager(new LinearLayoutManager(this));
		chatList.setHasFixedSize(true);
		chatList.setAdapter(adapter);
	}

	/**
	 * Send button logic
	 */
	private void onSendClicked() {
		String msg = inputMessage.getText().toString().trim();
		if (TextUtils.isEmpty(msg)) return;
		sendMessage(msg);
		inputMessage.setText("");
	}

	/**
	 * Listen for real-time messages
	 */
	private void listenForMessages() {
		db.collection("chats").document(chatId)
				.collection("messages")
				.orderBy("timestamp")
				.addSnapshotListener((snap, e) -> {
					if (snap != null && !snap.isEmpty()) {
						List<Message> messages = snap.toObjects(Message.class);
						for (int i = 0; i < snap.getDocuments().size(); i++) {
							messages.get(i).setId(snap.getDocuments().get(i).getId());
						}
						adapter.updateList(messages);
						chatList.smoothScrollToPosition(messages.size() - 1);
					}
				});
	}

	/**
	 * Send a new text message
	 */
	private void sendMessage(String text) {
		Map<String, Object> map = new HashMap<>();
		map.put("type", "text");
		map.put("text", text);
		map.put("senderId", userId);
		map.put("timestamp", System.currentTimeMillis());

		db.collection("chats").document(chatId)
				.collection("messages")
				.add(map);
	}

	/**
	 * ✅ Show a dialog with Delete or Edit options
	 */
	private void showMessageOptionsDialog(Message message) {
		new AlertDialog.Builder(this)
				.setTitle("Message Options")
				.setItems(new CharSequence[]{"Edit", "Delete"}, (dialog, which) -> {
					if (which == 0) {
						editMessage(message);
					} else {
						softDeleteMessage(message);
					}
				})
				.setNegativeButton("Cancel", null)
				.show();
	}

	/**
	 * ✅ Soft Delete: Mark message as deleted
	 */
	private void softDeleteMessage(Message message) {
		db.collection("chats").document(chatId)
				.collection("messages").document(message.getId())
				.update("text", "This message was deleted", "type", "deleted")
				.addOnSuccessListener(aVoid ->
						Toast.makeText(this, "Message deleted", Toast.LENGTH_SHORT).show()
				)
				.addOnFailureListener(e ->
						Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show()
				);
	}

	/**
	 * ✅ Edit message with input dialog
	 */
	private void editMessage(Message message) {
		EditText editInput = new EditText(this);
		editInput.setText(message.getText());

		new AlertDialog.Builder(this)
				.setTitle("Edit Message")
				.setView(editInput)
				.setPositiveButton("Update", (dialog, which) -> {
					String updatedText = editInput.getText().toString().trim();
					if (!TextUtils.isEmpty(updatedText)) {
						db.collection("chats").document(chatId)
								.collection("messages").document(message.getId())
								.update("text", updatedText, "type", "edited")
								.addOnSuccessListener(aVoid ->
										Toast.makeText(this, "Message updated", Toast.LENGTH_SHORT).show()
								)
								.addOnFailureListener(e ->
										Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
								);
					}
				})
				.setNegativeButton("Cancel", null)
				.show();
	}
}
