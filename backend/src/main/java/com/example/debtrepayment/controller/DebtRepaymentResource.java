package com.example.debtrepayment.controller;

import com.example.debtrepayment.api.DebtRepaymentApi;
import com.example.debtrepayment.api.model.DebtRepaymentRequest;
import com.example.debtrepayment.api.model.DebtRepaymentResponse;
import com.example.debtrepayment.mapper.DebtRepaymentMapper;
import com.example.debtrepayment.service.DebtRepaymentService;

public class DebtRepaymentResource implements DebtRepaymentApi {

    private final DebtRepaymentService debtRepaymentService;
    private final DebtRepaymentMapper debtRepaymentMapper;

    public DebtRepaymentResource(DebtRepaymentService debtRepaymentService, DebtRepaymentMapper debtRepaymentMapper) {
        this.debtRepaymentService = debtRepaymentService;
        this.debtRepaymentMapper = debtRepaymentMapper;
    }

    @Override
    public DebtRepaymentResponse calculateDebtRepayment(DebtRepaymentRequest request) {
        var plan = debtRepaymentService.calculateDebtRepaymentPlan(debtRepaymentMapper.toInput(request));
        return debtRepaymentMapper.toResponse(request, plan);
    }
}
