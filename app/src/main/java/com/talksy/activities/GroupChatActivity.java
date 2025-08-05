package com.talksy.activities;

import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.talksy.R;
import com.talksy.adapters.ChatAdapter;
import com.talksy.models.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupChatActivity extends AppCompatActivity {

	private final List<Message> messageList = new ArrayList<>();
	private RecyclerView chatList;
	private EditText inputMessage;
	private ImageButton btnSend;
	private TextView groupTitleText;

	private FirebaseFirestore db;
	private ListenerRegistration messageListener;
	private ChatAdapter adapter;
	private String userId, chatId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_chat);

		// Bind UI
		chatList = findViewById(R.id.chatRecycler);
		inputMessage = findViewById(R.id.inputMessage);
		btnSend = findViewById(R.id.btnSend);
		groupTitleText = findViewById(R.id.groupTitleText);

		// Init Firebase
		db = FirebaseFirestore.getInstance();
		userId = FirebaseAuth.getInstance().getUid();
		chatId = getIntent().getStringExtra("chatId");

		if (TextUtils.isEmpty(chatId) || userId == null) {
			Toast.makeText(this, "Invalid chat session", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		// Setup RecyclerView with gesture listeners
		adapter = new ChatAdapter(this, userId, new ChatAdapter.OnMessageActionListener() {
			@Override
			public void onMessageDoubleClick(Message message) {
				// ✅ Optional quick edit
				if (message.getSenderId().equals(userId)) {
					editMessage(message);
				} else {
					Toast.makeText(GroupChatActivity.this, "You can only edit your messages", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onMessageLongPress(Message message) {
				// ✅ Only allow sender to manage their messages
				if (message.getSenderId().equals(userId)) {
					showMessageOptionsDialog(message); // ✅ Unified dialog
				} else {
					Toast.makeText(GroupChatActivity.this, "You can only manage your messages", Toast.LENGTH_SHORT).show();
				}
			}
		});

		chatList.setLayoutManager(new LinearLayoutManager(this));
		chatList.setAdapter(adapter);

		// Send button
		btnSend.setOnClickListener(v -> sendMessage());

		// Load Group Title
		loadGroupTitle();

		// Listen for messages
		listenForMessages();
	}

	/**
	 * Load group title from Firestore
	 */
	private void loadGroupTitle() {
		db.collection("groups").document(chatId).get()
				.addOnSuccessListener(doc -> {
					if (doc.exists()) {
						groupTitleText.setText(doc.getString("name"));
					}
				});
	}

	/**
	 * Listen for new messages in real-time
	 */
	private void listenForMessages() {
		messageListener = db.collection("chats").document(chatId)
				.collection("messages")
				.orderBy("timestamp", Query.Direction.ASCENDING)
				.addSnapshotListener((snap, e) -> {
					if (e != null || snap == null) return;

					for (DocumentChange dc : snap.getDocumentChanges()) {
						switch (dc.getType()) {
							case ADDED:
								Message m = dc.getDocument().toObject(Message.class);
								m.setId(dc.getDocument().getId());
								messageList.add(m);
								break;
							case MODIFIED:
								Message updatedMsg = dc.getDocument().toObject(Message.class);
								updatedMsg.setId(dc.getDocument().getId());
								for (int i = 0; i < messageList.size(); i++) {
									if (messageList.get(i).getId().equals(updatedMsg.getId())) {
										messageList.set(i, updatedMsg);
										break;
									}
								}
								break;
							case REMOVED:
								String removedId = dc.getDocument().getId();
								messageList.removeIf(msg -> msg.getId().equals(removedId));
								break;
						}
					}
					adapter.updateList(messageList);
					chatList.scrollToPosition(messageList.size() - 1);
				});
	}

	/**
	 * Send message and update group timestamp
	 */
	private void sendMessage() {
		String text = inputMessage.getText().toString().trim();
		if (TextUtils.isEmpty(text)) return;

		Map<String, Object> msg = new HashMap<>();
		msg.put("type", "text");
		msg.put("text", text);
		msg.put("senderId", userId);
		msg.put("timestamp", System.currentTimeMillis());

		db.collection("chats").document(chatId)
				.collection("messages")
				.add(msg)
				.addOnSuccessListener(doc ->
						db.collection("groups").document(chatId)
								.update("timestamp", System.currentTimeMillis())
				);

		inputMessage.setText("");
	}

	/**
	 * ✅ Unified Message Options Dialog: Edit / Delete
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
	 * ✅ Edit message
	 */
	private void editMessage(Message message) {
		EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		input.setText(message.getText());

		new AlertDialog.Builder(this)
				.setTitle("Edit Message")
				.setView(input)
				.setPositiveButton("Save", (dialog, which) -> {
					String newText = input.getText().toString().trim();
					if (!TextUtils.isEmpty(newText)) {
						db.collection("chats").document(chatId)
								.collection("messages")
								.document(message.getId())
								.update("text", newText, "type", "edited")
								.addOnSuccessListener(unused ->
										Toast.makeText(this, "Message updated", Toast.LENGTH_SHORT).show())
								.addOnFailureListener(e ->
										Toast.makeText(this, "Failed to update", Toast.LENGTH_SHORT).show());
					}
				})
				.setNegativeButton("Cancel", null)
				.show();
	}

	/**
	 * ✅ Soft delete message
	 */
	private void softDeleteMessage(Message message) {
		db.collection("chats").document(chatId)
				.collection("messages")
				.document(message.getId())
				.update("text", "This message was deleted", "type", "deleted")
				.addOnSuccessListener(unused ->
						Toast.makeText(this, "Message deleted", Toast.LENGTH_SHORT).show())
				.addOnFailureListener(e ->
						Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show());
	}

	@Override
	protected void onDestroy() {
		if (messageListener != null) messageListener.remove();
		super.onDestroy();
	}
}
