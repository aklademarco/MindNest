package com.example.mindnest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    List<Map<String, Object>> noteList;

    public NoteAdapter(List<Map<String, Object>> noteList) {
        this.noteList = noteList;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_item, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Map<String, Object> note = noteList.get(position);
        holder.title.setText((String) note.get("title"));
        holder.body.setText((String) note.get("body"));

        // 🔴 Long-click to delete
        holder.itemView.setOnLongClickListener(v -> {
            String noteId = (String) note.get("id");
            FirebaseFirestore.getInstance().collection("notes")
                    .document(noteId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        noteList.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(v.getContext(), "Note deleted", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(v.getContext(), "Delete failed", Toast.LENGTH_SHORT).show();
                    });
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView title, body;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.noteTitle);
            body = itemView.findViewById(R.id.noteBody);
        }
    }
}
