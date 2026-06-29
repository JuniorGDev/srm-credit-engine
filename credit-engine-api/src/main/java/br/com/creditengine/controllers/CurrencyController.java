package br.com.creditengine.controllers;

import br.com.creditengine.dtos.request.CurrencyRequest;
import br.com.creditengine.dtos.response.CurrencyResponse;
import br.com.creditengine.service.CurrencyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/currencies")
@Validated
public class CurrencyController {

    private final CurrencyService currencyService;

    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @GetMapping
    public ResponseEntity<List<CurrencyResponse>> findAll() {
        return ResponseEntity.ok(currencyService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CurrencyResponse> findById(@Positive @PathVariable Long id) {
        return ResponseEntity.ok(currencyService.findById(id));
    }

    @PostMapping
    public ResponseEntity<CurrencyResponse> save(@Valid @RequestBody CurrencyRequest currencyRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(currencyService.save(currencyRequest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CurrencyResponse> update(@Positive @PathVariable Long id, @Valid @RequestBody CurrencyRequest currencyRequest) {
        return ResponseEntity.ok(currencyService.update(id, currencyRequest));
    }
}
