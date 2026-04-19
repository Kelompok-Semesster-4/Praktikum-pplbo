package com.library.app.model;

import com.library.app.model.enums.LoanStatus;
import java.time.LocalDate;
import java.math.BigDecimal;

public class Loan {
    private long id;
    private long memberId;
    private long copyId;
    private LocalDate loanDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private BigDecimal fineAmount = BigDecimal.ZERO;
    private LoanStatus status = LoanStatus.ACTIVE;
    
    public Loan() {}

    public Loan(long id, long memberId, long copyId, LocalDate loanDate, LocalDate dueDate, LocalDate returnDate,
            BigDecimal fineAmount, LoanStatus status) {
        this.id = id;
        this.memberId = memberId;
        this.copyId = copyId;
        this.loanDate = loanDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.fineAmount = fineAmount;
        this.status = status;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setMemberId(long memberId) {
        this.memberId = memberId;
    }

    public void setCopyId(long copyId) {
        this.copyId = copyId;
    }

    public void setLoanDate(LocalDate loanDate) {
        this.loanDate = loanDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public void setFineAmount(BigDecimal fineAmount) {
        this.fineAmount = fineAmount;
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public long getMemberId() {
        return memberId;
    }

    public long getCopyId() {
        return copyId;
    }

    public LocalDate getLoanDate() {
        return loanDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public BigDecimal getFineAmount() {
        return fineAmount;
    }

    public LoanStatus getStatus() {
        return status;
    }
}