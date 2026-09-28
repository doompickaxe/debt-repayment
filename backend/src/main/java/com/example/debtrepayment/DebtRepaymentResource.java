package com.example.debtrepayment;

import com.example.debtrepayment.api.DebtRepaymentApi;
import com.example.debtrepayment.api.model.DebtRepaymentRequest;
import com.example.debtrepayment.api.model.DebtRepaymentResponse;

public class DebtRepaymentResource implements DebtRepaymentApi {

    @Override
    public DebtRepaymentResponse calculateDebtRepayment(DebtRepaymentRequest request) {
        // TODO: calculate the monthly repayment schedule and its summary
        return new DebtRepaymentResponse().parameters(request);
    }
}
