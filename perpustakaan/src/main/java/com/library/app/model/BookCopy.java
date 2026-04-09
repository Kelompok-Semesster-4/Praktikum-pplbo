package com.library.app.model;

import com.library.app.model.enums.CopyStatus;

public class BookCopy {
    private Long id;
    private Long bookId;
    private String copyCode;
    private CopyStatus status;

    public BookCopy() {
    }

    public BookCopy(Long id, Long bookId, String copyCode, CopyStatus status) {
        this.id = id;
        this.bookId = bookId;
        this.copyCode = copyCode;
        this.status = status;
    }

    public void markBorrowed() {
        this.status = CopyStatus.BORROWED;
    }

    public void markAvailable() {
        this.status = CopyStatus.AVAILABLE;
    }

    public Long getId() {
        return id;
    }

    public Long getBookId() {
        return bookId;
    }

    public String getCopyCode() {
        return copyCode;
    }

    public CopyStatus getStatus() {
        return status;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public void setCopyCode(String copyCode) {
        this.copyCode = copyCode;
    }

    public void setStatus(CopyStatus status) {
        this.status = status;
    }
}
