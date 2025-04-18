package com.example.wordapp.models;

public class Word {
    public String id;
    public String word;
    public String type;
    public String meaning;

    public Word() {} // Bắt buộc cho Firebase

    public Word(String id, String word, String type, String meaning) {
        this.id = id;
        this.word = word;
        this.type = type;
        this.meaning = meaning;
    }
}