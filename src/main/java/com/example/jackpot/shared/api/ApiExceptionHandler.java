package com.example.jackpot.shared.api;

import com.example.jackpot.shared.error.BetPublicationException;
import com.example.jackpot.shared.error.ConflictingBetException;
import com.example.jackpot.shared.error.ContributionNotReadyException;
import com.example.jackpot.shared.error.ResourceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail invalidRequestBody(MethodArgumentNotValidException exception) {
        var problem =
                problem(
                        HttpStatus.BAD_REQUEST,
                        "Request validation failed",
                        "One or more request fields are invalid");
        var errors = new LinkedHashMap<String, String>();
        exception
                .getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class,
        ConstraintViolationException.class,
        IllegalArgumentException.class
    })
    ProblemDetail malformedRequest(Exception exception) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "Request validation failed",
                "Request body or parameter is invalid");
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ProblemDetail notFound(ResourceNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "Resource not found", exception);
    }

    @ExceptionHandler({ConflictingBetException.class, ContributionNotReadyException.class})
    ProblemDetail conflict(RuntimeException exception) {
        return problem(HttpStatus.CONFLICT, "Request conflicts with current state", exception);
    }

    @ExceptionHandler(BetPublicationException.class)
    ProblemDetail unavailable(BetPublicationException exception) {
        return problem(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Bet publication is temporarily unavailable",
                exception);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail dataConflict(DataIntegrityViolationException exception) {
        return problem(
                HttpStatus.CONFLICT,
                "Request conflicts with current state",
                "The request violates data integrity");
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail unexpected(Exception exception) {
        return problem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error",
                "An unexpected error occurred");
    }

    private ProblemDetail problem(HttpStatus status, String title, Exception exception) {
        return problem(status, title, exception.getMessage());
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail) {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
