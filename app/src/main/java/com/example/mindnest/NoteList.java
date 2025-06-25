package com.example.mindnest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

        recyclerView = findViewById(R.id.noteRecyclerView); // ✅ updated ID

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        noteList = new ArrayList<>();
        adapter = new NoteAdapter(noteList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        loadNotes();

        FloatingActionButton addNoteButton = findViewById(R.id.fabAddNote); // ✅ updated ID
        addNoteButton.setOnClickListener(v -> {
            startActivity(new Intent(NoteList.this, AddNote.class));
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.note_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_profile) {
            startActivity(new Intent(NoteList.this, ProfileActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void loadNotes() {
        TextView emptyMessage = findViewById(R.id.emptyMessage); // Optional, depends on layout

        db.collection("notes")
                .whereEqualTo("userId", mAuth.getCurrentUser().getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Failed to load notes", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    noteList.clear();
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        for (DocumentSnapshot doc : querySnapshot) {
                            Map<String, Object> note = doc.getData();
                            if (note != null) {
                                note.put("id", doc.getId());
                                noteList.add(note);
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}
