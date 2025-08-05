package com.talksy.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.talksy.R;
import com.talksy.adapters.NotesAdapter;
import com.talksy.models.Note;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NotesFragment extends Fragment {

	private final List<Note> notes = new ArrayList<>();
	private NotesAdapter adapter;
	private FirebaseFirestore db;
	private String userId;

	private RecyclerView recyclerView;
	private TextView txtEmpty;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_notes, container, false);

		// Bind Views
		recyclerView = root.findViewById(R.id.recyclerNotes);
		txtEmpty = root.findViewById(R.id.txtEmpty);
		Button btnAddNote = root.findViewById(R.id.btnAddNote);

		// Setup RecyclerView
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		adapter = new NotesAdapter(notes, new NotesAdapter.OnNoteActionListener() {
			@Override
			public void onEdit(Note note) {
				showNoteDialog(note);
			}

			@Override
			public void onDelete(Note note) {
				deleteNote(note);
			}
		});
		recyclerView.setAdapter(adapter);

		// Firebase
		db = FirebaseFirestore.getInstance();
		userId = FirebaseAuth.getInstance().getUid();

		if (userId == null) {
			Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
			return root;
		}

		// Add note button
		btnAddNote.setOnClickListener(v -> showNoteDialog(null));

		// Load Notes
		loadNotes();

		return root;
	}

	/**
	 * ================= LOAD NOTES =================
	 */
	private void loadNotes() {
		db.collection("users")
				.document(userId)
				.collection("notes")
				.orderBy("timestamp", Query.Direction.DESCENDING)
				.addSnapshotListener((snap, e) -> {
					if (e != null) {
						Toast.makeText(getContext(), "Error loading notes", Toast.LENGTH_SHORT).show();
						return;
					}

					notes.clear();
					if (snap != null && !snap.isEmpty()) {
						for (DocumentSnapshot doc : snap.getDocuments()) {
							Note note = doc.toObject(Note.class);
							if (note != null) notes.add(note);
						}
						recyclerView.setVisibility(View.VISIBLE);
						txtEmpty.setVisibility(View.GONE);
					} else {
						recyclerView.setVisibility(View.GONE);
						txtEmpty.setVisibility(View.VISIBLE);
					}
					adapter.notifyDataSetChanged();
				});
	}

	/**
	 * ================= ADD / EDIT NOTE =================
	 */
	private void showNoteDialog(Note noteToEdit) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle(noteToEdit == null ? "Add Note" : "Edit Note");

		final EditText input = new EditText(getContext());
		input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
		input.setHint("Enter your note here...");
		input.setMinLines(3);
		if (noteToEdit != null) input.setText(noteToEdit.getText());

		builder.setView(input);

		builder.setPositiveButton("Save", (dialog, which) -> {
			String text = input.getText().toString().trim();
			if (!text.isEmpty()) {
				if (noteToEdit == null) addNote(text);
				else updateNote(noteToEdit, text);
			} else {
				Toast.makeText(getContext(), "Note cannot be empty", Toast.LENGTH_SHORT).show();
			}
		});

		builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
		builder.show();
	}

	/**
	 * ================= FIREBASE OPERATIONS =================
	 */
	private void addNote(String text) {
		String id = UUID.randomUUID().toString();
		Note note = new Note(id, text, System.currentTimeMillis());

		db.collection("users").document(userId)
				.collection("notes").document(id)
				.set(note)
				.addOnSuccessListener(unused -> Toast.makeText(getContext(), "Note added", Toast.LENGTH_SHORT).show())
				.addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to save", Toast.LENGTH_SHORT).show());
	}

	private void updateNote(Note note, String newText) {
		db.collection("users").document(userId)
				.collection("notes").document(note.getId())
				.update("text", newText, "timestamp", System.currentTimeMillis())
				.addOnSuccessListener(unused -> Toast.makeText(getContext(), "Note updated", Toast.LENGTH_SHORT).show())
				.addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update", Toast.LENGTH_SHORT).show());
	}

	private void deleteNote(Note note) {
		db.collection("users").document(userId)
				.collection("notes").document(note.getId())
				.delete()
				.addOnSuccessListener(unused -> Toast.makeText(getContext(), "Note deleted", Toast.LENGTH_SHORT).show())
				.addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to delete", Toast.LENGTH_SHORT).show());
	}
}
