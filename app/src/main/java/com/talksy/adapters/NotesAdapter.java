package com.talksy.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.talksy.R;
import com.talksy.models.Note;

import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {
	private final List<Note> notes;
	private final OnNoteActionListener listener;

	public NotesAdapter(List<Note> notes, OnNoteActionListener listener) {
		this.notes = notes;
		this.listener = listener;
	}

	@NonNull
	@Override
	public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.item_note, parent, false);
		return new NoteViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
		Note note = notes.get(position);
		holder.textView.setText(note.getText());
		holder.btnEdit.setOnClickListener(v -> listener.onEdit(note));
		holder.btnDelete.setOnClickListener(v -> listener.onDelete(note));
	}

	@Override
	public int getItemCount() {
		return notes.size();
	}

	public interface OnNoteActionListener {
		void onEdit(Note note);

		void onDelete(Note note);
	}

	static class NoteViewHolder extends RecyclerView.ViewHolder {
		TextView textView;
		ImageButton btnEdit, btnDelete;

		NoteViewHolder(View itemView) {
			super(itemView);
			textView = itemView.findViewById(R.id.txtNote);
			btnEdit = itemView.findViewById(R.id.btnEdit);
			btnDelete = itemView.findViewById(R.id.btnDelete);
		}
	}
}
