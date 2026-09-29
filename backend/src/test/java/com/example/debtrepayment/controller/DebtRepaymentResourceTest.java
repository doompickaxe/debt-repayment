package com.example.debtrepayment.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class DebtRepaymentResourceTest {

    @Test
    void acceptsValidRequest() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                  {"loanAmount": 100000, "annualInterestRate": 2.12, "loanPeriodYears": 10, "initialRepaymentRate": 2}
                  """)
            .when().post("/api/debt-repayment")
            .then()
            .statusCode(200)
            .body("parameters.loanAmount", is(100000))
            .body("parameters.loanPeriodYears", is(10))
            .body("parameters.annualInterestRate", is(2.12f))
            .body("parameters.initialRepaymentRate", is(2))
            .body("summary.remainingDebt", is(77744.14f))
            .body("summary.totalInterest", is(18943.74f))
            .body("summary.totalRepayment", is(22255.86f))
            .body("summary.totalInstalments", is(41199.60f))
            .body("schedule[0].month", is(1))
            .body("schedule[0].remainingDebt", is(99833.34f))
            .body("schedule[0].interest", is(176.67f))
            .body("schedule[0].repayment", is(166.66f))
            .body("schedule[0].instalment", is(343.33f))
            .body("schedule[119].month", is(120))
            .body("schedule[119].remainingDebt", is(77744.14f))
            .body("schedule[119].interest", is(137.71f))
            .body("schedule[119].repayment", is(205.62f))
            .body("schedule[119].instalment", is(343.33f));
    }

    @Test
    void rejectsInvalidRequest() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                  {"loanAmount": -1, "annualInterestRate": 2.12, "loanPeriodYears": 0}
                  """)
            .when().post("/api/debt-repayment")
            .then()
            .statusCode(400)
            .body("errorCode", is("VALIDATION_ERROR"))
            .body(
                "message", allOf(
                    containsString("loanAmount"),
                    containsString("loanPeriodYears"),
                    containsString("initialRepaymentRate")
                )
            );
    }

    @Test
    void rejectsMissingBody() {
        given()
            .contentType(ContentType.JSON)
            .when().post("/api/debt-repayment")
            .then()
            .statusCode(400)
            .body("errorCode", is("VALIDATION_ERROR"));
    }

    @Test
    void rejectsWronglyTypedValue() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                  {"loanAmount": "lots", "annualInterestRate": 2.12, "loanPeriodYears": 10, "initialRepaymentRate": 2}
                  """)
            .when().post("/api/debt-repayment")
            .then()
            .statusCode(400)
            .body("errorCode", is("INVALID_REQUEST_BODY"));
    }

    @Test
    void rejectsMalformedBody() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"loanAmount\": \"lots\"")
            .when().post("/api/debt-repayment")
            .then()
            .statusCode(400)
            .body("errorCode", is("INVALID_REQUEST_BODY"))
            .body("message", notNullValue());
    }

    @Test
    void servesOpenApiSpec() {
        given()
            .when().get("/q/openapi")
            .then()
            .statusCode(200);
    }
}
