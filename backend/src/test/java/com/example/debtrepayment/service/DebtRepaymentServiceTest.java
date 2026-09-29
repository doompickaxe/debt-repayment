package com.example.debtrepayment.service;

import com.example.debtrepayment.model.DebtRepaymentInput;
import com.example.debtrepayment.model.RepaymentPlan.RepaymentPlanItem;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;

class DebtRepaymentServiceTest {

    private final DebtRepaymentService service = new DebtRepaymentService();

    @Test
    public void calculatesDebtRepaymentPlan() {
        var input = new DebtRepaymentInput(
            new BigDecimal("100000"),
            new BigDecimal("0.0212"),
            new BigDecimal("0.02"),
            10
        );

        var result = service.calculateDebtRepaymentPlan(input);

        assertThat(result, notNullValue());
        assertThat(result.plan(), notNullValue());
        assertThat(result.summary(), notNullValue());

        var plan = result.plan();
        assertThat(plan.size(), equalTo(120));
        var firstMonth = plan.getFirst();
        assertThat(firstMonth.month(), equalTo(1));
        assertThat(firstMonth.instalment(), equalTo(new BigDecimal("343.33")));
        assertThat(firstMonth.repayment(), equalTo(new BigDecimal("166.66")));
        assertThat(firstMonth.interest(), equalTo(new BigDecimal("176.67")));
        assertThat(firstMonth.remainingDebt(), equalTo(new BigDecimal("99833.34")));

        var lastMonth = plan.getLast();
        assertThat(lastMonth.month(), equalTo(120));
        assertThat(lastMonth.instalment(), equalTo(new BigDecimal("343.33")));
        assertThat(lastMonth.interest(), equalTo(new BigDecimal("137.71")));
        assertThat(lastMonth.repayment(), equalTo(new BigDecimal("205.62")));
        assertThat(lastMonth.remainingDebt(), equalTo(new BigDecimal("77744.14")));

        var summary = result.summary();
        assertThat(summary.remainingDebt(), equalTo(new BigDecimal("77744.14")));
        assertThat(summary.totalInterest(), equalTo(new BigDecimal("18943.74")));
        assertThat(summary.totalRepayments(), equalTo(new BigDecimal("22255.86")));
        assertThat(summary.totalInstalments(), equalTo(new BigDecimal("41199.60")));
    }

    @Test
    public void everyRowAddsUp() {
        var input = new DebtRepaymentInput(
            new BigDecimal("250000"),
            new BigDecimal("0.0375"),
            new BigDecimal("0.015"),
            30
        );

        var plan = service.calculateDebtRepaymentPlan(input).plan();

        var previousDebt = new BigDecimal("250000.00");
        for (var item : plan) {
            assertThat(item.interest().add(item.repayment()), equalTo(item.instalment()));
            assertThat(previousDebt.subtract(item.repayment()), equalTo(item.remainingDebt()));
            previousDebt = item.remainingDebt();
        }
    }

    @Test
    public void summaryEqualsColumnSums() {
        var input = new DebtRepaymentInput(
            new BigDecimal("250000"),
            new BigDecimal("0.0375"),
            new BigDecimal("0.015"),
            30
        );

        var result = service.calculateDebtRepaymentPlan(input);

        var plan = result.plan();
        var summary = result.summary();
        assertThat(summary.totalInterest(), equalTo(plan.stream().map(RepaymentPlanItem::interest).reduce(BigDecimal.ZERO, BigDecimal::add)));
        assertThat(summary.totalRepayments(), equalTo(plan.stream().map(RepaymentPlanItem::repayment).reduce(BigDecimal.ZERO, BigDecimal::add)));
        assertThat(summary.totalInstalments(), equalTo(plan.stream().map(RepaymentPlanItem::instalment).reduce(BigDecimal.ZERO, BigDecimal::add)));
        assertThat(summary.remainingDebt(), equalTo(plan.getLast().remainingDebt()));
        assertThat(summary.totalRepayments().add(summary.remainingDebt()), equalTo(new BigDecimal("250000.00")));
    }

    @Test
    public void stopsWhenDebtIsPaidOff() {
        // 50 % redemption pays off the loan in the second year, long before the period ends
        var input = new DebtRepaymentInput(
            new BigDecimal("10000"),
            new BigDecimal("0.05"),
            new BigDecimal("0.5"),
            10
        );

        var result = service.calculateDebtRepaymentPlan(input);

        var plan = result.plan();
        assertThat(plan.size(), lessThan(24));
        assertThat(plan.getLast().remainingDebt(), comparesEqualTo(BigDecimal.ZERO));
        assertThat(plan.getLast().instalment(), lessThan(input.monthlyInstalment()));
        assertThat(result.summary().remainingDebt(), comparesEqualTo(BigDecimal.ZERO));
        assertThat(result.summary().totalRepayments(), equalTo(new BigDecimal("10000.00")));
    }

    @Test
    public void calculatesInterestRoundedToCents() {
        assertThat(service.calculateInterest(new BigDecimal("100000.00"), new BigDecimal("0.0212")), equalTo(new BigDecimal("176.67")));
        assertThat(service.calculateInterest(new BigDecimal("99833.34"), new BigDecimal("0.0212")), equalTo(new BigDecimal("176.37")));
        assertThat(service.calculateInterest(new BigDecimal("100000.00"), BigDecimal.ZERO), equalTo(new BigDecimal("0.00")));
    }

    @Test
    public void calculatesMonthlyInstalmentRoundedToCents() {
        var input = new DebtRepaymentInput(new BigDecimal("100000"), new BigDecimal("0.0212"), new BigDecimal("0.02"), 10);
        assertThat(input.monthlyInstalment(), equalTo(new BigDecimal("343.33")));
    }
}
