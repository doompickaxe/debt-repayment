package com.example.debtrepayment.mapper;

import com.example.debtrepayment.api.model.DebtRepaymentRequest;
import com.example.debtrepayment.api.model.DebtRepaymentResponse;
import com.example.debtrepayment.api.model.RepaymentScheduleEntry;
import com.example.debtrepayment.api.model.RepaymentSummary;
import com.example.debtrepayment.model.DebtRepaymentInput;
import com.example.debtrepayment.model.RepaymentPlan;
import com.example.debtrepayment.model.RepaymentPlan.RepaymentPlanItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.math.BigDecimal;

/// Maps between the generated API models and the internal models.
/// The API expresses rates in percent (`2.12`), the internal model as fractions (`0.0212`).
@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI)
public interface DebtRepaymentMapper {

    @Mapping(target = "payout", source = "loanAmount")
    @Mapping(target = "interestRate", source = "annualInterestRate", qualifiedByName = "percentToFraction")
    @Mapping(target = "repaymentRate", source = "initialRepaymentRate", qualifiedByName = "percentToFraction")
    @Mapping(target = "periodYears", source = "loanPeriodYears")
    DebtRepaymentInput toInput(DebtRepaymentRequest request);

    @Mapping(target = "parameters", source = "request")
    @Mapping(target = "schedule", source = "plan.plan")
    @Mapping(target = "summary", source = "plan.summary")
    // Generated fluent helper, not a property
    @Mapping(target = "removeScheduleItem", ignore = true)
    DebtRepaymentResponse toResponse(DebtRepaymentRequest request, RepaymentPlan plan);

    RepaymentScheduleEntry toScheduleEntry(RepaymentPlanItem item);

    @Mapping(target = "totalRepayment", source = "totalRepayments")
    RepaymentSummary toSummary(RepaymentPlan.RepaymentSummary summary);

    @Named("percentToFraction")
    default BigDecimal percentToFraction(BigDecimal percent) {
        return percent == null ? null : percent.movePointLeft(2);
    }
}
