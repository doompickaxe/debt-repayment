package com.example.debtrepayment.service;

import com.example.debtrepayment.model.DebtRepaymentInput;
import com.example.debtrepayment.model.RepaymentPlan;
import com.example.debtrepayment.model.RepaymentPlan.RepaymentPlanItem;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.stream.IntStream;

@ApplicationScoped
public class DebtRepaymentService {

    static final MathContext MATH_CONTEXT = MathContext.DECIMAL128;

    /// Return repayment plan calculated based on input
    /// The instalment is fixed over the whole period, but the relation of paid interest and paid debt changes each month.
    /// Key point is the formula `Tm = T1 * q^(m-1)` where `q = 1 + i/12`. (T1 = first repayment amount, Tm = repayment for given month, i = interest, m = month)
    public RepaymentPlan calculateDebtRepaymentPlan(DebtRepaymentInput input) {
        var plan = IntStream.rangeClosed(1, input.periodYears() * 12)
            .mapToObj(month -> createRepaymentPlanItem(input, month))
            .toList();

        return new RepaymentPlan(plan, calculateRepaymentSummary(input));
    }

    /// q = 1 + i/12
    BigDecimal calculateQ(BigDecimal interest) {
        return BigDecimal.ONE.add(interest.divide(BigDecimal.valueOf(12), MATH_CONTEXT));
    }

    /// qm = (q^m-1)/(q-1)
    BigDecimal calculateQMultiplier(BigDecimal q, int month) {
        return q.pow(month, MATH_CONTEXT).subtract(BigDecimal.ONE).divide(q.subtract(BigDecimal.ONE), MATH_CONTEXT);
    }

    /// Tm = T1 * q^(m-1)
    BigDecimal calculateRepaymentForMonth(DebtRepaymentInput input, int month) {
        var q = calculateQ(input.interestRate());
        return input.firstRepaymentAmount().multiply(q.pow(month - 1, MATH_CONTEXT));
    }

    /// R = K - T1 * (q^m-1)/(q-1)
    BigDecimal calculateRemainingDebt(DebtRepaymentInput input, BigDecimal q, int month) {
        var multiplier = calculateQMultiplier(q, month);
        var rightHandSubtractor = input.firstRepaymentAmount().multiply(multiplier, MATH_CONTEXT);
        return input.payout().subtract(rightHandSubtractor);
    }

    RepaymentPlanItem createRepaymentPlanItem(DebtRepaymentInput input, int month) {
        var repayment = calculateRepaymentForMonth(input, month);

        return new RepaymentPlanItem(
            month,
            input.monthlyInstalment().setScale(2, RoundingMode.HALF_UP),
            input.monthlyInstalment().subtract(repayment).setScale(2, RoundingMode.HALF_UP),
            repayment.setScale(2, RoundingMode.HALF_UP),
            calculateRemainingDebt(input, calculateQ(input.interestRate()), month).setScale(2, RoundingMode.HALF_UP)
        );
    }

    RepaymentPlan.RepaymentSummary calculateRepaymentSummary(DebtRepaymentInput input) {
        var q = calculateQ(input.interestRate());
        var months = input.periodYears() * 12;
        var qm = calculateQMultiplier(q, months);
        var allInstalments = input.monthlyInstalment().setScale(2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(input.periodYears() * 12), MATH_CONTEXT);
        var allRepayments = input.firstRepaymentAmount().multiply(qm, MATH_CONTEXT);
        var remainingDebt = input.payout().subtract(allRepayments);
        var totalPaidInterest = input.monthlyInstalment().multiply(BigDecimal.valueOf(months), MATH_CONTEXT).subtract(allRepayments);

        return new RepaymentPlan.RepaymentSummary(
            remainingDebt.setScale(2, RoundingMode.HALF_UP),
            totalPaidInterest.setScale(2, RoundingMode.HALF_UP),
            allInstalments.setScale(2, RoundingMode.HALF_UP),
            allRepayments.setScale(2, RoundingMode.HALF_UP)
        );
    }
}
