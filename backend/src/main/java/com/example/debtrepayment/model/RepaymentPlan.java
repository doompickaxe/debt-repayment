package com.example.debtrepayment.model;

import java.math.BigDecimal;
import java.util.List;

public record RepaymentPlan(List<RepaymentPlanItem> plan, RepaymentSummary summary) {
}

record RepaymentPlanItem(int month, BigDecimal instalment, BigDecimal interest, BigDecimal debt) {
}

record RepaymentSummary(BigDecimal totalInterest, BigDecimal totalDebt) {
}