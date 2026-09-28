package com.example.debtrepayment.model;

import java.math.BigDecimal;
import java.util.List;

public record RepaymentPlan(List<RepaymentPlanItem> plan, RepaymentSummary summary) {

    public record RepaymentPlanItem(int month, BigDecimal instalment, BigDecimal interest, BigDecimal repayment, BigDecimal remainingDebt) {
    }

    public record RepaymentSummary(BigDecimal remainingDebt,
                                   BigDecimal totalInterest,
                                   BigDecimal totalInstalments,
                                   BigDecimal totalRepayments) {
    }
}
