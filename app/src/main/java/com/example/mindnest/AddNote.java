package com.example.mindnest;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddNote extends AppCompatActivity {

    EditText titleEditText, bodyEditText;
    Button saveNoteBtn;
    ImageView noteImageView;
    FirebaseFirestore db;
    FirebaseAuth mAuth;

    FloatingActionButton fabMain, fabPhoto, fabFile, fabMusic;
    boolean isFabOpen = false;

    private static final int PICK_IMAGE = 1;
    private static final int PICK_FILE = 2;
    private static final int PICK_MUSIC = 3;

    private Uri selectedImageUri;
    private Uri selectedFileUri;
    private Uri selectedMusicUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        titleEditText = findViewById(R.id.titleEditText);
        bodyEditText = findViewById(R.id.bodyEditText);
        saveNoteBtn = findViewById(R.id.saveNoteBtn);
        noteImageView = findViewById(R.id.noteImageView);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        fabMain = findViewById(R.id.fabMain);
        fabPhoto = findViewById(R.id.fabPhoto);
        fabFile = findViewById(R.id.fabFile);
        fabMusic = findViewById(R.id.fabMusic);

        fabMain.setOnClickListener(v -> toggleFabMenu());
        fabPhoto.setOnClickListener(v -> pickImage());
        fabFile.setOnClickListener(v -> pickFile());
        fabMusic.setOnClickListener(v -> pickMusic());
        saveNoteBtn.setOnClickListener(v -> saveNote());
    }

    private void toggleFabMenu() {
        int visibility = isFabOpen ? View.GONE : View.VISIBLE;
        fabPhoto.setVisibility(visibility);
        fabFile.setVisibility(visibility);
        fabMusic.setVisibility(visibility);
        isFabOpen = !isFabOpen;
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }

    private void pickFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_FILE);
    }

    private void pickMusic() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(intent, PICK_MUSIC);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            Uri selectedUri = data.getData();
            if (selectedUri == null) return;

            switch (requestCode) {
                case PICK_IMAGE:
                    selectedImageUri = selectedUri;
                    noteImageView.setImageURI(selectedImageUri);
                    noteImageView.setVisibility(View.VISIBLE);
                    break;

                case PICK_FILE:
                    selectedFileUri = selectedUri;
                    Toast.makeText(this, "File: " + getFileName(selectedUri), Toast.LENGTH_SHORT).show();
                    break;

                case PICK_MUSIC:
                    selectedMusicUri = selectedUri;
                    Toast.makeText(this, "Music: " + getFileName(selectedUri), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
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

        uploadAttachments(note);
    }

    private void uploadAttachments(Map<String, Object> note) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        List<Task<?>> uploadTasks = new ArrayList<>();

        if (selectedImageUri != null) {
            StorageReference imageRef = storageRef.child("notes/images/" + getFileName(selectedImageUri));
            Task<Uri> uploadTask = imageRef.putFile(selectedImageUri)
                    .continueWithTask(task -> imageRef.getDownloadUrl())
                    .addOnSuccessListener(uri -> note.put("imageUrl", uri.toString()));
            uploadTasks.add(uploadTask);
        }

        if (selectedFileUri != null) {
            StorageReference fileRef = storageRef.child("notes/files/" + getFileName(selectedFileUri));
            Task<Uri> uploadTask = fileRef.putFile(selectedFileUri)
                    .continueWithTask(task -> fileRef.getDownloadUrl())
                    .addOnSuccessListener(uri -> note.put("fileUrl", uri.toString()));
            uploadTasks.add(uploadTask);
        }

        if (selectedMusicUri != null) {
            StorageReference musicRef = storageRef.child("notes/music/" + getFileName(selectedMusicUri));
            Task<Uri> uploadTask = musicRef.putFile(selectedMusicUri)
                    .continueWithTask(task -> musicRef.getDownloadUrl())
                    .addOnSuccessListener(uri -> note.put("musicUrl", uri.toString()));
            uploadTasks.add(uploadTask);
        }

        Tasks.whenAllSuccess(uploadTasks)
                .addOnSuccessListener(results -> {
                    db.collection("notes")
                            .add(note)
                            .addOnSuccessListener(docRef -> {
                                Toast.makeText(this, "Note saved!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, NoteList.class));
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error saving note: " + e.getMessage(), Toast.LENGTH_LONG).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }

        if (result == null) {
            result = uri.getLastPathSegment();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }

        return result != null ? result : "unknown_file";
    }

}
