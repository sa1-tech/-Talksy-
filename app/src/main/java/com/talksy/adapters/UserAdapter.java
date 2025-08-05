package com.talksy.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.talksy.R;
import com.talksy.models.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

	private final Context context;
	private final List<User> users;
	private final OnUserActionListener listener;
	private final String currentUserId;
	private final String creatorId;

	/**
	 * @param context       Activity/Fragment context
	 * @param users         List of users
	 * @param listener      Callback listener
	 * @param currentUserId ID of the logged-in user
	 * @param creatorId     ID of the group creator (only they can delete)
	 */
	public UserAdapter(Context context, List<User> users, OnUserActionListener listener,
	                   String currentUserId, String creatorId) {
		this.context = context;
		this.users = users;
		this.listener = listener;
		this.currentUserId = currentUserId;
		this.creatorId = creatorId;
	}

	@NonNull
	@Override
	public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.item_user, parent, false);
		return new UserViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
		User user = users.get(position);

		// ✅ Set user name
		holder.txtName.setText(user.getName());

		// ✅ Load profile image with Glide
		String imgUrl = user.getProfileImage();
		if (imgUrl != null && !imgUrl.isEmpty()) {
			Glide.with(context)
					.load(imgUrl)
					.placeholder(R.drawable.place)
					.into(holder.imgProfile);
		} else {
			holder.imgProfile.setImageResource(R.drawable.img);
		}

		// ✅ Handle clicks for opening chat
		holder.itemView.setOnClickListener(v -> {
			if (listener != null) listener.onUserClick(user);
		});

		// ✅ Show delete button only for group creator
		if (currentUserId.equals(creatorId)) {
			holder.btnDelete.setVisibility(View.VISIBLE);
			holder.btnDelete.setOnClickListener(v -> {
				if (listener != null) listener.onUserDelete(user);
			});
		} else {
			holder.btnDelete.setVisibility(View.GONE);
		}
	}

	@Override
	public int getItemCount() {
		return users.size();
	}

	// ✅ Callback interface for user actions
	public interface OnUserActionListener {
		void onUserClick(User user);    // Open chat

		void onUserDelete(User user);   // Remove from group
	}

	// ✅ ViewHolder class
	static class UserViewHolder extends RecyclerView.ViewHolder {
		ImageView imgProfile;
		TextView txtName;
		ImageButton btnDelete;

		UserViewHolder(@NonNull View itemView) {
			super(itemView);
			imgProfile = itemView.findViewById(R.id.itemUserImage);
			txtName = itemView.findViewById(R.id.itemUserName);
			btnDelete = itemView.findViewById(R.id.btnDeleteUser);
		}
	}
}
