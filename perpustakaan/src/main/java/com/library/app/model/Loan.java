package com.library.app.model;

import com.library.app.model.enums.LoanStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Loan {
    private Long id;
    private Long memberId;
    private Long copyId;
    private String memberCode;
    private String memberName;
    private String copyCode;
    private String bookTitle;
    private LocalDate loanDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private BigDecimal fineAmount = BigDecimal.ZERO;
    private LoanStatus status;

    public Loan() {
    }

    public boolean isOverdue(LocalDate today) {
        return dueDate != null && today.isAfter(dueDate);
    }

    public long calculateLateDays(LocalDate today) {
        if (!isOverdue(today)) {
            return 0;
        }
        return ChronoUnit.DAYS.between(dueDate, today);
    }

    public BigDecimal calculateFine(LocalDate today, BigDecimal dailyFine) {
        return dailyFine.multiply(BigDecimal.valueOf(calculateLateDays(today)));
    }

    public void returnBook(LocalDate today, BigDecimal dailyFine) {
        this.returnDate = today;
        this.fineAmount = calculateFine(today, dailyFine);
        this.status = LoanStatus.RETURNED;
    }

    public Long getId() {
        return id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public Long getCopyId() {
        return copyId;
    }

    public String getMemberCode() {
        return memberCode;
    }

    public String getMemberName() {
        return memberName;
    }

    public String getCopyCode() {
        return copyCode;
    }

    public String getBookTitle() {
        return bookTitle;
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

    public void setId(Long id) {
        this.id = id;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public void setCopyId(Long copyId) {
        this.copyId = copyId;
    }

    public void setMemberCode(String memberCode) {
        this.memberCode = memberCode;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public void setCopyCode(String copyCode) {
        this.copyCode = copyCode;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
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
}
