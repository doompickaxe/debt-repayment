package com.example.debtrepayment.service;

import com.example.debtrepayment.model.DebtRepaymentInput;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
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
    }

    @Test
    public void calculatesQ() {
        var input = BigDecimal.ZERO;
        var result = service.calculateQ(input);
        assertThat(result, equalTo(BigDecimal.ONE));

        input = BigDecimal.ONE;
        result = service.calculateQ(input);
        assertThat(result.setScale(3, RoundingMode.HALF_UP), equalTo(new BigDecimal("1.083")));

        input = BigDecimal.TEN;
        result = service.calculateQ(input);
        assertThat(result.setScale(3, RoundingMode.HALF_UP), equalTo(new BigDecimal("1.833")));

        input = new BigDecimal("0.0212");
        result = service.calculateQ(input);
        assertThat(result.setScale(5, RoundingMode.HALF_UP), equalTo(new BigDecimal("1.00177")));

        input = BigDecimal.valueOf(1).movePointLeft(2);
        result = service.calculateQ(input);
        assertThat(result.setScale(5, RoundingMode.HALF_UP), equalTo(new BigDecimal("1.00083")));
    }

    @Test
    public void calculatesRepaymentForMonth() {
        var input = new DebtRepaymentInput(
            new BigDecimal("100000"),
            new BigDecimal("0.0212"),
            new BigDecimal("0.02"),
            10
        );

        var month = 1;
        var result = service.calculateRepaymentForMonth(input, month);
        assertThat(result, equalTo(new BigDecimal("166.67")));

        month = 2;
        result = service.calculateRepaymentForMonth(input, month);
        assertThat(result.setScale(2, RoundingMode.HALF_EVEN), equalTo(new BigDecimal("166.96")));
    }
}