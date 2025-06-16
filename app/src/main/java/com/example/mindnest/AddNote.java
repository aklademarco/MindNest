package com.example.mindnest;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddNote extends AppCompatActivity {

    EditText titleEditText, bodyEditText;
    Button saveNoteBtn;
    FirebaseFirestore db;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        titleEditText = findViewById(R.id.titleEditText);
        bodyEditText = findViewById(R.id.bodyEditText);
        saveNoteBtn = findViewById(R.id.saveNoteBtn);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        saveNoteBtn.setOnClickListener(v -> saveNote());
    }

    private void saveNote() {
        String title = titleEditText.getText().toString().trim();
        String body = bodyEditText.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(body)) {
            Toast.makeText(this, "Please fill both fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> note = new HashMap<>();
        note.put("title", title);
        note.put("body", body);
        note.put("userId", mAuth.getCurrentUser().getUid());
        note.put("timestamp", System.currentTimeMillis());

        db.collection("notes")
                .add(note)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Note saved!", Toast.LENGTH_SHORT).show();
                    finish(); // close this activity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save note: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
