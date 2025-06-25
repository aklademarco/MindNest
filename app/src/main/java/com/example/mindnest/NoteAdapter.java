package com.example.mindnest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private List<Map<String, Object>> noteList;

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
        String title = (String) note.get("title");
        holder.title.setText(title);

        Object timestampObj = note.get("timestamp");
        if (timestampObj instanceof Long) {
            long timestamp = (Long) timestampObj;
            String formattedDate = formatTimestamp(timestamp);
            holder.date.setText(formattedDate);
        } else {
            holder.date.setText(""); // fallback
        }

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

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView title, date;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.noteTitle);
            date = itemView.findViewById(R.id.noteDate);
        }
    }

    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
