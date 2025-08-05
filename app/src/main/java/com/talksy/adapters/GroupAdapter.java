package com.talksy.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.talksy.R;
import com.talksy.models.Group;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

	private final List<Group> groupList;
	private final Context context;
	private final OnGroupActionListener listener;
	private final String currentUserId;  // ✅ Needed to check creator

	public GroupAdapter(List<Group> groupList, Context context,
	                    OnGroupActionListener listener, String currentUserId) {
		this.groupList = groupList;
		this.context = context;
		this.listener = listener;
		this.currentUserId = currentUserId;
	}

	@NonNull
	@Override
	public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(context).inflate(R.layout.item_group, parent, false);
		return new GroupViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
		Group group = groupList.get(position);

		// ✅ Bind group name and members
		holder.groupName.setText(group.getName());
		holder.groupSubtitle.setText(group.getMembersCountText());

		// ✅ Click to open group chat
		holder.itemView.setOnClickListener(v -> {
			if (listener != null) listener.onGroupClick(group);
		});

		// ✅ Long press to edit (only for creator)
		holder.itemView.setOnLongClickListener(v -> {
			if (group.isCreator(currentUserId)) {
				if (listener != null) listener.onGroupEdit(group);
				return true;
			} else {
				Toast.makeText(context, "Only the group creator can edit.", Toast.LENGTH_SHORT).show();
				return true;
			}
		});

		// ✅ Show delete button only for group creator
		if (group.isCreator(currentUserId)) {
			holder.btnDeleteGroup.setVisibility(View.VISIBLE);
			holder.btnDeleteGroup.setOnClickListener(v -> {
				if (listener != null) {
					listener.onGroupDelete(group); // Notify fragment to handle delete
				}
			});
		} else {
			holder.btnDeleteGroup.setVisibility(View.GONE);
		}
	}

	@Override
	public int getItemCount() {
		return groupList.size();
	}

	/**
	 * ✅ Callback interface for group actions
	 */
	public interface OnGroupActionListener {
		void onGroupClick(Group group);

		void onGroupEdit(Group group);

		void onGroupDelete(Group group); // ✅ Added delete callback
	}

	/**
	 * ✅ ViewHolder
	 */
	static class GroupViewHolder extends RecyclerView.ViewHolder {
		TextView groupName, groupSubtitle;
		ImageButton btnDeleteGroup;

		GroupViewHolder(@NonNull View itemView) {
			super(itemView);
			groupName = itemView.findViewById(R.id.groupTitleText);
			groupSubtitle = itemView.findViewById(R.id.groupSubtitleText);
			btnDeleteGroup = itemView.findViewById(R.id.btnDeleteGroup); // ✅ Ensure this exists in XML
		}
	}
}
