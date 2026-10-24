package com.rd.mynotes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    Context context;
    private List<String> notesList;
    private OnItemClickListener listener; // 1. Define the listener interface instance

    // Constructor to pass in your notes dataset
    // 2. Interface to handle click events in the Activity
    public interface OnItemClickListener {
        void onItemClick(String noteText, int position);
    }

    // 3. Updated Constructor to accept the listener
    public NotesAdapter(List<String> notesList, OnItemClickListener listener) {
        this.notesList = notesList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the custom row layout we just created
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_item, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        // Bind your note data to the TextView inside the row
        String currentNote = notesList.get(position);
        holder.textViewNoteText.setText(currentNote);

        // 4. Set a click listener on the entire item container view
        holder.itemView.setOnClickListener(v -> {
            if (listener != null && position != RecyclerView.NO_POSITION) {
                // Pass the note text and its index back to the activity
                listener.onItemClick(currentNote, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notesList != null ? notesList.size() : 0;
    }

    // ViewHolder holds direct references to all inner views for a row
    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView textViewNoteText;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNoteText = itemView.findViewById(R.id.textViewNoteText);
        }
    }
}
