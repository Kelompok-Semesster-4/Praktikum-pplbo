package com.library.app.model;

import com.library.app.model.enums.RequestStatus;

import java.time.LocalDateTime;

public class ProcurementRequest {
    private Long id;
    private Long memberId;
    private String requesterName;
    private String title;
    private String author;
    private String note;
    private RequestStatus status;
    private String responseNote;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;

    public ProcurementRequest() {
    }

    public Long getId() {
        return id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getNote() {
        return note;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public String getResponseNote() {
        return responseNote;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public void setResponseNote(String responseNote) {
        this.responseNote = responseNote;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }
}