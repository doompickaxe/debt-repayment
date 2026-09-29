package com.example.debtrepayment.service;

import com.example.debtrepayment.model.DebtRepaymentInput;
import com.example.debtrepayment.model.RepaymentPlan;
import com.example.debtrepayment.model.RepaymentPlan.RepaymentPlanItem;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.example.debtrepayment.model.DebtRepaymentInput.MONTHS_PER_YEAR;
import static com.example.debtrepayment.model.DebtRepaymentInput.toCents;

@ApplicationScoped
public class DebtRepaymentService {

    static final MathContext MATH_CONTEXT = MathContext.DECIMAL128;

    /// Return repayment plan calculated based on input
    /// The instalment is fixed over the whole period, but the relation of paid interest and paid debt changes each month.
    /// Each month is calculated from the remaining debt of the previous month, like a bank would book it:
    /// 1. interest = remaining debt * i/12, rounded to cents
    /// 2. repayment = instalment - interest
    /// 3. remaining debt = remaining debt - repayment
    ///
    /// This way every row adds up exactly and the summary equals the sums of the columns.
    /// If the debt is paid off before the end of the period, the last instalment only covers the rest and the plan ends there.
    public RepaymentPlan calculateDebtRepaymentPlan(DebtRepaymentInput input) {
        var instalment = input.monthlyInstalment();
        var remainingDebt = toCents(input.payout());
        var months = input.periodYears() * MONTHS_PER_YEAR.intValue();
        var plan = new ArrayList<RepaymentPlanItem>(months);

        for (var month = 1; month <= months && remainingDebt.signum() > 0; month++) {
            var interest = calculateInterest(remainingDebt, input.interestRate());
            var repayment = instalment.subtract(interest).min(remainingDebt);
            remainingDebt = remainingDebt.subtract(repayment);
            plan.add(new RepaymentPlanItem(month, interest.add(repayment), interest, repayment, remainingDebt));
        }

        return new RepaymentPlan(plan, calculateRepaymentSummary(toCents(input.payout()), plan));
    }

    /// Interest for one month on the given debt, rounded to cents
    BigDecimal calculateInterest(BigDecimal remainingDebt, BigDecimal interestRate) {
        return toCents(remainingDebt.multiply(interestRate).divide(MONTHS_PER_YEAR, MATH_CONTEXT));
    }

    RepaymentPlan.RepaymentSummary calculateRepaymentSummary(BigDecimal payout, List<RepaymentPlanItem> plan) {
        var remainingDebt = plan.isEmpty() ? payout : plan.getLast().remainingDebt();

        return new RepaymentPlan.RepaymentSummary(
            remainingDebt,
            sum(plan, RepaymentPlanItem::interest),
            sum(plan, RepaymentPlanItem::instalment),
            sum(plan, RepaymentPlanItem::repayment)
        );
    }

    private static BigDecimal sum(List<RepaymentPlanItem> plan, Function<RepaymentPlanItem, BigDecimal> column) {
        return toCents(plan.stream().map(column).reduce(BigDecimal.ZERO, BigDecimal::add));
    }
}
