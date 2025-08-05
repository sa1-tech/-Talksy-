package com.talksy.adapters;

import android.content.Context;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.talksy.R;
import com.talksy.models.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

	private final Context context;
	private final String currentUserId;
	private final List<Message> messageList = new ArrayList<>();
	private final OnMessageActionListener actionListener;

	public ChatAdapter(Context context, String currentUserId, OnMessageActionListener listener) {
		this.context = context;
		this.currentUserId = currentUserId;
		this.actionListener = listener;
	}

	/**
	 * Determine if the message is sent (1) or received (0)
	 */
	@Override
	public int getItemViewType(int position) {
		Message message = messageList.get(position);
		return message.getSenderId().equals(currentUserId) ? 1 : 0; // 1 = Sent, 0 = Received
	}

	@NonNull
	@Override
	public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		int layout = (viewType == 1)
				? R.layout.item_chat_message_sent
				: R.layout.item_chat_message_received;
		View view = LayoutInflater.from(context).inflate(layout, parent, false);
		return new MessageViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
		Message message = messageList.get(position);

		// ✅ Handle deleted messages
		if (message.isDeleted()) {
			holder.textMessage.setText("This message was deleted");
			holder.textMessage.setTypeface(holder.textMessage.getTypeface(), android.graphics.Typeface.ITALIC);
		} else if (message.isEdited()) {
			holder.textMessage.setText(message.getText() + " (edited)");
			holder.textMessage.setTypeface(holder.textMessage.getTypeface(), android.graphics.Typeface.ITALIC);
		} else {
			holder.textMessage.setText(message.getText());
			holder.textMessage.setTypeface(holder.textMessage.getTypeface(), android.graphics.Typeface.NORMAL);
		}


		// ✅ Format timestamp
		String formattedTime = new SimpleDateFormat("hh:mm a", Locale.getDefault())
				.format(new Date(message.getTimestamp()));
		holder.textTimestamp.setText(formattedTime);

		// ✅ Gesture detection for double tap (edit) and long press (menu)
		GestureDetector gestureDetector = new GestureDetector(context,
				new GestureDetector.SimpleOnGestureListener() {
					@Override
					public boolean onDoubleTap(MotionEvent e) {
						if (actionListener != null) {
							actionListener.onMessageDoubleClick(message);
						}
						return true;
					}

					@Override
					public void onLongPress(MotionEvent e) {
						if (actionListener != null) {
							actionListener.onMessageLongPress(message);
						}
					}
				});

		// ✅ Attach gesture detector to the whole message item
		holder.itemView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
	}

	@Override
	public int getItemCount() {
		return messageList.size();
	}

	/**
	 * Update message list with new data
	 */
	public void updateList(List<Message> newList) {
		messageList.clear();
		messageList.addAll(newList);
		notifyDataSetChanged();
	}

	/**
	 * Callback interface for double-tap edit & long press delete
	 */
	public interface OnMessageActionListener {
		void onMessageDoubleClick(Message message); // Edit

		void onMessageLongPress(Message message);   // Delete or Menu
	}

	/**
	 * ViewHolder
	 */
	static class MessageViewHolder extends RecyclerView.ViewHolder {
		TextView textMessage, textTimestamp;

		MessageViewHolder(@NonNull View itemView) {
			super(itemView);
			textMessage = itemView.findViewById(R.id.textMessage);
			textTimestamp = itemView.findViewById(R.id.textTimestamp);
		}
	}
}
