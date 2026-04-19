package com.library.app.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class OverdueLoanReportItem {
   private String borrowerName;
   private String memberCode;
   private String bookTitle;
   private LocalDate dueDate;
   private BigDecimal fineAmount = BigDecimal.ZERO;
   private String status;

   public String getBorrowerName() {
      return borrowerName;
   }

   public void setBorrowerName(String borrowerName) {
      this.borrowerName = borrowerName;
   }

   public String getMemberCode() {
      return memberCode;
   }

   public void setMemberCode(String memberCode) {
      this.memberCode = memberCode;
   }

   public String getBookTitle() {
      return bookTitle;
   }

   public void setBookTitle(String bookTitle) {
      this.bookTitle = bookTitle;
   }

   public LocalDate getDueDate() {
      return dueDate;
   }

   public void setDueDate(LocalDate dueDate) {
      this.dueDate = dueDate;
   }

   public BigDecimal getFineAmount() {
      return fineAmount;
   }

   public void setFineAmount(BigDecimal fineAmount) {
      this.fineAmount = fineAmount == null ? BigDecimal.ZERO : fineAmount;
   }

   public String getStatus() {
      return status;
   }

   public void setStatus(String status) {
      this.status = status;
   }
}
