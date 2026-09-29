package com.example.debtrepayment.mapper;

import com.example.debtrepayment.api.model.DebtRepaymentRequest;
import com.example.debtrepayment.model.RepaymentPlan;
import com.example.debtrepayment.model.RepaymentPlan.RepaymentPlanItem;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;

class DebtRepaymentMapperTest {

    private final DebtRepaymentMapper mapper = Mappers.getMapper(DebtRepaymentMapper.class);

    private final DebtRepaymentRequest request = new DebtRepaymentRequest()
        .loanAmount(new BigDecimal("100000"))
        .annualInterestRate(new BigDecimal("2.12"))
        .initialRepaymentRate(new BigDecimal("2"))
        .loanPeriodYears(10);

    @Test
    void mapsRequestToInputAndConvertsPercentToFraction() {
        var input = mapper.toInput(request);

        assertThat(input.payout(), equalTo(new BigDecimal("100000")));
        assertThat(input.interestRate(), comparesEqualTo(new BigDecimal("0.0212")));
        assertThat(input.repaymentRate(), comparesEqualTo(new BigDecimal("0.02")));
        assertThat(input.periodYears(), equalTo(10));
    }

    @Test
    void mapsPlanToResponse() {
        var plan = new RepaymentPlan(
            List.of(new RepaymentPlanItem(1, new BigDecimal("343.33"), new BigDecimal("176.67"), new BigDecimal("166.66"), new BigDecimal("99833.34"))),
            new RepaymentPlan.RepaymentSummary(new BigDecimal("77744.14"), new BigDecimal("18943.74"), new BigDecimal("41199.60"), new BigDecimal("22255.86"))
        );

        var response = mapper.toResponse(request, plan);

        assertThat(response.getParameters(), sameInstance(request));

        var entry = response.getSchedule().getFirst();
        assertThat(entry.getMonth(), equalTo(1));
        assertThat(entry.getInstalment(), equalTo(new BigDecimal("343.33")));
        assertThat(entry.getInterest(), equalTo(new BigDecimal("176.67")));
        assertThat(entry.getRepayment(), equalTo(new BigDecimal("166.66")));
        assertThat(entry.getRemainingDebt(), equalTo(new BigDecimal("99833.34")));

        var summary = response.getSummary();
        assertThat(summary.getRemainingDebt(), equalTo(new BigDecimal("77744.14")));
        assertThat(summary.getTotalInterest(), equalTo(new BigDecimal("18943.74")));
        assertThat(summary.getTotalInstalments(), equalTo(new BigDecimal("41199.60")));
        assertThat(summary.getTotalRepayment(), equalTo(new BigDecimal("22255.86")));
    }
}
