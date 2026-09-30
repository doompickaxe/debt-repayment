package com.example.debtrepayment.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record DebtRepaymentInput(BigDecimal payout,
                                 BigDecimal interestRate,
                                 BigDecimal repaymentRate,
                                 int periodYears) {

    public static final BigDecimal MONTHS_PER_YEAR = BigDecimal.valueOf(12);

    /// Fixed monthly instalment: payout * (i + r) / 12, rounded to cents
    public BigDecimal monthlyInstalment() {
        return payout.multiply(interestRate.add(repaymentRate)).divide(MONTHS_PER_YEAR, 2, RoundingMode.HALF_EVEN);
    }

    /// Money is booked in whole cents, rounded commercially
    public static BigDecimal toCents(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_EVEN);
    }
}
