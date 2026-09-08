package com.sendlt.api.advice;

import static org.assertj.core.api.Assertions.assertThat;

import com.sendlt.api.dto.ErrorResponse;
import com.sendlt.application.exception.BusinessValidationException;
import com.sendlt.application.exception.ForbiddenOperationException;
import com.sendlt.application.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler(new MockEnvironment());

    @Test
    void notFoundReturnsErrorBody() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/boulders/unknown");
        ResponseEntity<ErrorResponse> response =
                handler.handleNotFound(new ResourceNotFoundException("Boulder", "x"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().error()).isEqualTo("Not Found");
        assertThat(response.getBody().path()).isEqualTo("/api/boulders/unknown");
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    void businessValidationMapsTo400() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/sessions/1/attempts");
        ResponseEntity<ErrorResponse> response = handler.handleBusinessValidation(
                new BusinessValidationException("A FLASH attempt must have triesCount = 1"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).contains("FLASH");
    }

    @Test
    void forbiddenMapsTo403() {
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/api/sessions/1/attempts/1");
        ResponseEntity<ErrorResponse> response = handler.handleForbidden(
                new ForbiddenOperationException("You can only modify your own attempts"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().status()).isEqualTo(403);
    }

    @Test
    void unexpectedErrorHidesDetailsInProdProfile() {
        Environment prod = new MockEnvironment().withProperty("spring.profiles.active", "prod");
        GlobalExceptionHandler prodHandler = new GlobalExceptionHandler(prod);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");

        ResponseEntity<ErrorResponse> response =
                prodHandler.handleUnexpected(new RuntimeException("secret db password"), request);

        assertThat(response.getBody().message()).isEqualTo("An unexpected error occurred");
        assertThat(response.getBody().message()).doesNotContain("password");
    }
}
