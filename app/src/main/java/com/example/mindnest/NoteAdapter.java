package com.example.mindnest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Map;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private final List<Map<String, Object>> notes;

    public NoteAdapter(List<Map<String, Object>> notes) {
        this.notes = notes;
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
        Map<String, Object> note = notes.get(position);

        holder.title.setText((String) note.get("title"));
        holder.body.setText((String) note.get("body"));

        String imageUrl = (String) note.get("imageUrl");
        if (imageUrl != null && !imageUrl.isEmpty()) {
            holder.image.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .into(holder.image);
        } else {
            holder.image.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView title, body;
        ImageView image;

        NoteViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.noteTitle);
            body = itemView.findViewById(R.id.noteBody);
            image = itemView.findViewById(R.id.noteImage);
        }
    }
}
