package com.example.smartlibraryapp;

public class Book {
    public String title;
    public String author;
    public String status;
    public String qr;
    public String imageUrl;
    public Long issueTimestamp;
    public Long timestamp;
    public int quantity;
    public int issuedCount;

    public Book() {}

    public Book(String title, String author, String status, String qr) {
        this.title = title;
        this.author = author;
        this.status = status;
        this.qr = qr;
        this.quantity = 1;
        this.issuedCount = 0; // Default hamesha 0 rahega
    }

    public Book(String title, String author, String status, String qr, Long issueTimestamp) {
        this(title, author, status, qr);
        this.issueTimestamp = issueTimestamp;
    }

    public Book(String title, String author, String status, String qr, String imageUrl, Long timestamp) {
        this(title, author, status, qr);
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
    }
}
