package com.example.debtrepayment.controller;

import com.example.debtrepayment.api.DebtRepaymentApi;
import com.example.debtrepayment.api.model.DebtRepaymentRequest;
import com.example.debtrepayment.api.model.DebtRepaymentResponse;
import com.example.debtrepayment.service.DebtRepaymentService;

public class DebtRepaymentResource implements DebtRepaymentApi {

    private final DebtRepaymentService debtRepaymentService;

    public DebtRepaymentResource(DebtRepaymentService debtRepaymentService) {
        this.debtRepaymentService = debtRepaymentService;
    }

    @Override
    public DebtRepaymentResponse calculateDebtRepayment(DebtRepaymentRequest request) {
        // TODO: calculate the monthly repayment schedule and its summary
        return new DebtRepaymentResponse().parameters(request);
    }
}
