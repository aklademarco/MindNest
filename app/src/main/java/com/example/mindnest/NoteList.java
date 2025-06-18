package com.example.mindnest;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NoteList extends AppCompatActivity {

    RecyclerView recyclerView;
    NoteAdapter adapter;
    List<Map<String, Object>> noteList;

    FirebaseFirestore db;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);

        recyclerView = findViewById(R.id.notesRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        noteList = new ArrayList<>();
        adapter = new NoteAdapter(noteList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        loadNotes();
    }

    private void loadNotes() {
        db.collection("notes")
                .whereEqualTo("userId", mAuth.getCurrentUser().getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Failed to load notes", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    noteList.clear();
                    for (DocumentSnapshot doc : snapshots) {
                        Map<String, Object> note = doc.getData();
                        if (note != null) {
                            note.put("id", doc.getId());
                            noteList.add(note);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
