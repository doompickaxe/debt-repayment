package com.example.debtrepayment.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record DebtRepaymentInput(BigDecimal payout,
                                 BigDecimal interestRate,
                                 BigDecimal repaymentRate,
                                 int periodYears) {

    public BigDecimal monthlyInstalment() {
        return payout.multiply(interestRate.add(repaymentRate)).divide(new BigDecimal("12"), RoundingMode.HALF_EVEN);
    }

    public BigDecimal firstRepaymentAmount() {
        return payout.multiply(repaymentRate).divide(new BigDecimal("12"), RoundingMode.HALF_EVEN);
    }
}
