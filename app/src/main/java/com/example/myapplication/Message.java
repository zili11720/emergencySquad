package com.example.myapplication;

public class Message {
    private String sender;
    private String timestamp;
    private String content;

    public Message(String sender, String timestamp, String content) {
        this.sender = sender;
        this.timestamp = timestamp;
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getContent() {
        return content;
    }
}
