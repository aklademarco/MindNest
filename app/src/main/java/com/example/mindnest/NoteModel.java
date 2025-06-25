package com.example.mindnest;

public class NoteModel {
    private String id;
    private String title;
    private String body;
    private String imageUrl;
    private String fileUrl;
    private String musicUrl;
    private String userId;
    private long timestamp;

    // Required empty constructor for Firestore
    public NoteModel() {}

    public NoteModel(String id, String title, String body, String imageUrl, String fileUrl, String musicUrl, String userId, long timestamp) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.imageUrl = imageUrl;
        this.fileUrl = fileUrl;
        this.musicUrl = musicUrl;
        this.userId = userId;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getMusicUrl() { return musicUrl; }
    public void setMusicUrl(String musicUrl) { this.musicUrl = musicUrl; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
