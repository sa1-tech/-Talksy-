package com.talksy.models;

import com.google.firebase.firestore.Exclude;

public class Message {
	private String id;
	private String text;
	private String senderId;
	private long timestamp;
	private String type; // "text", "edited", "deleted"

	public Message() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public String getSenderId() {
		return senderId;
	}

	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	// ✅ Utility methods (ignored by Firestore)
	@Exclude
	public boolean isDeleted() {
		return "deleted".equals(type);
	}

	@Exclude
	public boolean isEdited() {
		return "edited".equals(type);
	}

	@Exclude
	public boolean isText() {
		return "text".equals(type);
	}

	public void setText(String text) {
		this.text = text;
	}
}
