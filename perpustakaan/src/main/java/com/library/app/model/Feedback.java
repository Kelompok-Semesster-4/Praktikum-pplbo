package com.library.app.model;

import java.time.LocalDateTime;

public class Feedback {
    private Long id;
    private Long memberId;
    private String senderName;
    private String message;
    private LocalDateTime createdAt;

    public Feedback() {
    }

    public Feedback(Long id, Long memberId, String senderName, String message, LocalDateTime createdAt) {
        this.id = id;
        this.memberId = memberId;
        this.senderName = senderName;
        this.message = message;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}