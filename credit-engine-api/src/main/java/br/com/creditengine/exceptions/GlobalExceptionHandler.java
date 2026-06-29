package br.com.creditengine.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CurrencyAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleCurrencyAlreadyExistsException(Exception e) {
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.CONFLICT, "Currency already exists", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleResourceNotFoundException(Exception e) {
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.NOT_FOUND, "Resource not found", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex
    ) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        ProblemDetail problem = createProblemDetail(HttpStatus.BAD_REQUEST, "Validation failed", "Invalid request fields");
        problem.setProperty("errors", errors);

        return ResponseEntity.badRequest().body(problem);
    }

    private ProblemDetail createProblemDetail(
            HttpStatus status,
            String title,
            String detail
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setTitle(title);
        problemDetail.setDetail(detail);
        return problemDetail;
    }
}
