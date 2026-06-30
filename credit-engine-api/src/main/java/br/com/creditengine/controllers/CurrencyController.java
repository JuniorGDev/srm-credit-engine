package br.com.creditengine.controllers;

import br.com.creditengine.dtos.request.CurrencyRequest;
import br.com.creditengine.dtos.response.CurrencyResponse;
import br.com.creditengine.service.CurrencyService;
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
        name = "Currency",
        description = "Endpoints for managing currencies"
)
@RestController
@RequestMapping("/currencies")
@Validated
public class CurrencyController {

    private final CurrencyService currencyService;

    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @Operation(
            summary = "Find all currencies",
            description = "Find all currencies in the database",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Currencies found",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = CurrencyResponse.class))
                            )
                    )
            }
    )
    @GetMapping
    public ResponseEntity<List<CurrencyResponse>> findAll() {
        return ResponseEntity.ok(currencyService.findAll());
    }

    @Operation(
            summary = "Find currency by id",
            description = "Find currency by id in the database",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Currency found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CurrencyResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Currency not found with id: ",
                            content = @Content
                    )
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<CurrencyResponse> findById(@Positive @PathVariable Long id) {
        return ResponseEntity.ok(currencyService.findById(id));
    }

    @Operation(
            summary = "Create currency",
            description = "Create currency in the database",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Currency created",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CurrencyResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Currency already exists",
                            content = @Content
                    )
            }
    )
    @PostMapping
    public ResponseEntity<CurrencyResponse> save(@Valid @RequestBody CurrencyRequest currencyRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(currencyService.save(currencyRequest));
    }

    @Operation(
            summary = "Update currency",
            description = "Update currency in the database",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Currency updated",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CurrencyResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Currency not found with id: ",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request",
                            content =  @Content
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Currency already exists",
                            content = @Content
                    )
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<CurrencyResponse> update(@Positive @PathVariable Long id, @Valid @RequestBody CurrencyRequest currencyRequest) {
        return ResponseEntity.ok(currencyService.update(id, currencyRequest));
    }
}
