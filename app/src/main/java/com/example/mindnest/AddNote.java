package com.example.mindnest;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
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
    ImageView noteImageView;
    FirebaseFirestore db;
    FirebaseAuth mAuth;

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
//        noteImageView = findViewById(R.id.noteImageView); // If used later

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Optional toolbar functionality can be added here if needed
        findViewById(R.id.backIcon).setOnClickListener(v -> onBackPressed());
        findViewById(R.id.undoIcon).setOnClickListener(v -> Toast.makeText(this, "Undo", Toast.LENGTH_SHORT).show());
        findViewById(R.id.redoIcon).setOnClickListener(v -> Toast.makeText(this, "Redo", Toast.LENGTH_SHORT).show());
        findViewById(R.id.shareIcon).setOnClickListener(v -> Toast.makeText(this, "Share", Toast.LENGTH_SHORT).show());
        findViewById(R.id.menuIcon).setOnClickListener(v -> {
            androidx.appcompat.widget.PopupMenu popupMenu = new androidx.appcompat.widget.PopupMenu(this, v);
            popupMenu.getMenuInflater().inflate(R.menu.note_more_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();

                if (id == R.id.action_lock) {
                    Toast.makeText(this, "Lock clicked", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.action_pin) {
                    Toast.makeText(this, "Pin clicked", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.action_scan) {
                    Toast.makeText(this, "Scan clicked", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.action_find) {
                    Toast.makeText(this, "Find in Note clicked", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.action_recent) {
                    Toast.makeText(this, "Recent Notes clicked", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.action_lines_grids) {
                    Toast.makeText(this, "Lines and Grids clicked", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.action_delete) {
                    Toast.makeText(this, "Delete clicked", Toast.LENGTH_SHORT).show();
                    return true;
                }

                return false;
            });


            popupMenu.show();
        });


        findViewById(R.id.formatIcon).setOnClickListener(v -> Toast.makeText(this, "Format", Toast.LENGTH_SHORT).show());
        findViewById(R.id.bulletIcon).setOnClickListener(v -> Toast.makeText(this, "Bullet", Toast.LENGTH_SHORT).show());
        findViewById(R.id.checklistIcon).setOnClickListener(v -> Toast.makeText(this, "Checklist", Toast.LENGTH_SHORT).show());
        findViewById(R.id.tableIcon).setOnClickListener(v -> Toast.makeText(this, "Table", Toast.LENGTH_SHORT).show());
        findViewById(R.id.attachIcon).setOnClickListener(v -> Toast.makeText(this, "Attach", Toast.LENGTH_SHORT).show());
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
