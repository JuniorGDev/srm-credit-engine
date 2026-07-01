package br.com.creditengine.exceptions.handlers;

import br.com.creditengine.exceptions.CurrencyAlreadyExistsException;
import br.com.creditengine.exceptions.ExchangeRateAlreadyException;
import br.com.creditengine.exceptions.InvalidExchangeRateException;
import br.com.creditengine.exceptions.InvalidSettlementException;
import br.com.creditengine.exceptions.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

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

    @ExceptionHandler(ExchangeRateAlreadyException.class)
    public ResponseEntity<ProblemDetail> handleExchangeRateAlreadyException(Exception e) {
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.CONFLICT, "Exchange rate already exists", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }

    @ExceptionHandler(InvalidExchangeRateException.class)
    public ResponseEntity<ProblemDetail> handleInvalidExchangeRateException(Exception e) {
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.BAD_REQUEST, "Invalid exchange rate", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(InvalidSettlementException.class)
    public ResponseEntity<ProblemDetail> handleInvalidSettlementException(Exception e) {
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.BAD_REQUEST, "Invalid settlement", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }
}
