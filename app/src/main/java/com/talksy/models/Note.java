package com.talksy.models;

public class Note {
	private String id;
	private String text;
	private long timestamp;

	public Note() {
	} // Firestore needs empty constructor

	public Note(String id, String text, long timestamp) {
		this.id = id;
		this.text = text;
		this.timestamp = timestamp;
	}

	public String getId() {
		return id;
	}

	public String getText() {
		return text;
	}

	public long getTimestamp() {
		return timestamp;
	}
}
