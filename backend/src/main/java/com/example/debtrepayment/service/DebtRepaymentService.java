package com.example.debtrepayment.service;

import com.example.debtrepayment.model.DebtRepaymentInput;
import com.example.debtrepayment.model.RepaymentPlan;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.IntStream;

@ApplicationScoped
public class DebtRepaymentService {

    /// Return repayment plan calculated based on input
    /// The instalment is fixed over the whole period, but the relation of paid interest and paid debt changes each month.
    /// Key point is the formula `Tm = T1 * q^(m-1)` where `q = 1 + i/12`. (T1 = first repayment amount, Tm = repayment for given month, i = interest, m = month)
    public RepaymentPlan calculateDebtRepaymentPlan(DebtRepaymentInput input) {
        var plan = IntStream.range(1, input.periodYears() * 12)
            .mapToObj(month -> calculateRepaymentForMonth(input, month));

        return new RepaymentPlan(null, null);
    }

    /// q = 1 + i/12
    BigDecimal calculateQ(BigDecimal interest) {
        return BigDecimal.ONE.add(interest.divide(BigDecimal.valueOf(12), RoundingMode.HALF_EVEN));
    }

    /// Tm = T1 * q^(m-1)
    BigDecimal calculateRepaymentForMonth(DebtRepaymentInput input, int month) {
        var q = calculateQ(input.interestRate());
        return input.firstRepaymentAmount().multiply(q.pow(month - 1));
    }
}
