package br.com.creditengine.controllers;

import br.com.creditengine.dtos.request.ExchangeRateRequest;
import br.com.creditengine.dtos.request.ExchangeRateUpdateRequest;
import br.com.creditengine.dtos.response.CurrencyResponse;
import br.com.creditengine.dtos.response.ExchangeRateResponse;
import br.com.creditengine.service.ExchangeRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Exchange Rate",
        description = "Exchange Rate Controller"
)
@RestController
@RequestMapping("/exchange-rates")
@Validated
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    public ExchangeRateController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @Operation(
            summary = "Find all exchange rates",
            description = "Find all exchange rates",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Exchange rates found",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = CurrencyResponse.class))
                            )
                    )
            }
    )
    @GetMapping
    public ResponseEntity<List<ExchangeRateResponse>> findAll() {
        return ResponseEntity.ok(exchangeRateService.findAll());
    }

    @Operation(
            summary = "Find exchange rate by id",
            description = "Find exchange rate by id",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Exchange rate found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ExchangeRateResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Exchange rate not found"
                    )
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<ExchangeRateResponse> findById(@Positive @PathVariable Long id) {
        return ResponseEntity.ok(exchangeRateService.findById(id));
    }

    @Operation(
            summary = "Create exchange rate",
            description = "Create exchange rate",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Exchange rate created",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ExchangeRateResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Exchange rate already exists",
                            content = @Content
                    )
            }
    )
    @PostMapping
    public ResponseEntity<ExchangeRateResponse> save(@Valid @RequestBody ExchangeRateRequest exchangeRateRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(exchangeRateService.save(exchangeRateRequest));
    }

    @Operation(
            summary = "Update exchange rate",
            description = "Update exchange rate",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Exchange rate updated",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ExchangeRateResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Exchange rate not found",
                            content = @Content
                    )
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<ExchangeRateResponse> update(@Positive @PathVariable Long id, @Valid @RequestBody ExchangeRateUpdateRequest exchangeRateUpdateRequest) {
        return ResponseEntity.ok(exchangeRateService.update(id, exchangeRateUpdateRequest));
    }
}
