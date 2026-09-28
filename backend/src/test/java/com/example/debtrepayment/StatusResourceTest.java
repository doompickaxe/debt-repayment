package com.example.debtrepayment;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class StatusResourceTest {

    @Test
    void returnsStatus() {
        given()
                .when().get("/api/status")
                .then()
                .statusCode(200)
                .body("status", is("UP"))
                .body("version", notNullValue())
                .body("timestamp", notNullValue());
    }

    @Test
    void servesOpenApiSpec() {
        given()
                .when().get("/q/openapi")
                .then()
                .statusCode(200);
    }
}
