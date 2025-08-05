package com.talksy.models;

import java.util.List;

public class Group {
	private String id;                 // Firestore document ID
	private String name;               // Group name
	private List<String> participants; // List of participant UIDs
	private long timestamp;            // Last update timestamp
	private String creatorId;          // ✅ Group creator's UID

	// ✅ Empty constructor (required for Firestore)
	public Group() {
	}

	// ✅ Full Constructor
	public Group(String id, String name, List<String> participants, long timestamp, String creatorId) {
		this.id = id;
		this.name = name;
		this.participants = participants;
		this.timestamp = timestamp;
		this.creatorId = creatorId;
	}

	// ✅ Getters & Setters
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getParticipants() {
		return participants;
	}

	public void setParticipants(List<String> participants) {
		this.participants = participants;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(String creatorId) {
		this.creatorId = creatorId;
	}

	/**
	 * ✅ Returns the number of participants in the group
	 */
	public int getMembersCount() {
		return participants != null ? participants.size() : 0;
	}

	/**
	 * ✅ Returns a readable member count string
	 */
	public String getMembersCountText() {
		int count = getMembersCount();
		return count + (count == 1 ? " Member" : " Members");
	}

	/**
	 * ✅ Check if a user is the creator
	 */
	public boolean isCreator(String userId) {
		return creatorId != null && creatorId.equals(userId);
	}
}
