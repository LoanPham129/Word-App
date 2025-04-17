package com.example.wordapp.models;

public class Folders {
    private String id;
    private String name;
    private String owner;

    // Constructor rỗng bắt buộc cho Firebase
    public Folders() {
    }
    // Constructor đầy đủ
    public Folders(String id, String name, String owner) {
        this.id = id;
        this.name = name;
        this.owner = owner;
    }
    // Getter và Setter
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getOwner() {
        return owner;
    }
    public void setOwner(String owner) {
        this.owner = owner;
    }
}
