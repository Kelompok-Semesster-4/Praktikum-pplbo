package com.library.app.model;

import java.math.BigDecimal;

public class ReportSummary {
   private int totalLoans;
   private int returnedLoans;
   private int overdueLoans;
   private BigDecimal totalFineAmount = BigDecimal.ZERO;

   public int getTotalLoans() {
      return totalLoans;
   }

   public void setTotalLoans(int totalLoans) {
      this.totalLoans = totalLoans;
   }

   public int getReturnedLoans() {
      return returnedLoans;
   }

   public void setReturnedLoans(int returnedLoans) {
      this.returnedLoans = returnedLoans;
   }

   public int getOverdueLoans() {
      return overdueLoans;
   }

   public void setOverdueLoans(int overdueLoans) {
      this.overdueLoans = overdueLoans;
   }

   public BigDecimal getTotalFineAmount() {
      return totalFineAmount;
   }

   public void setTotalFineAmount(BigDecimal totalFineAmount) {
      this.totalFineAmount = totalFineAmount == null ? BigDecimal.ZERO : totalFineAmount;
   }
}
