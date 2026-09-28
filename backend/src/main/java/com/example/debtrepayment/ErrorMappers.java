package com.example.debtrepayment;

import com.example.debtrepayment.api.model.ErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import java.util.stream.Collectors;

/**
 * Maps request errors to the {@code ErrorResponse} defined in the OpenAPI spec.
 */
public class ErrorMappers {

    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String INVALID_REQUEST_BODY = "INVALID_REQUEST_BODY";

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> mapConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ErrorMappers::describe)
                .sorted()
                .collect(Collectors.joining("; "));
        return badRequest(VALIDATION_ERROR, message);
    }

    // Wrongly typed values, e.g. a string for a number. Overrides Quarkus' built-in mapper.
    @ServerExceptionMapper
    public RestResponse<ErrorResponse> mapMismatchedInput(MismatchedInputException e) {
        return invalidBody(e);
    }

    // Quarkus wraps JSON syntax errors in a WebApplicationException; everything else keeps its original response.
    @ServerExceptionMapper
    public Response mapWebApplication(WebApplicationException e) {
        if (e.getCause() instanceof JsonProcessingException json) {
            return invalidBody(json).toResponse();
        }
        return e.getResponse();
    }

    private static RestResponse<ErrorResponse> invalidBody(JsonProcessingException e) {
        return badRequest(INVALID_REQUEST_BODY, "Request body is not valid JSON: " + e.getOriginalMessage());
    }

    private static RestResponse<ErrorResponse> badRequest(String errorCode, String message) {
        return RestResponse.status(Response.Status.BAD_REQUEST, new ErrorResponse().errorCode(errorCode).message(message));
    }

    // Property paths look like "calculateDebtRepayment.debtRepaymentRequest.loanAmount"; keep only the field name.
    private static String describe(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath().toString();
        String field = path.substring(path.lastIndexOf('.') + 1);
        return field + ": " + violation.getMessage();
    }
}
