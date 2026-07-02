package br.com.creditengine.controllers;

import br.com.creditengine.dtos.request.SettlementRequest;
import br.com.creditengine.dtos.response.PageResponse;
import br.com.creditengine.dtos.response.SettlementResponse;
import br.com.creditengine.dtos.response.SettlementSimulationResponse;
import br.com.creditengine.dtos.response.SettlementStatementResponse;
import br.com.creditengine.service.SettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(
        name = "Settlement",
        description = "Settlement operations"
)
@RestController
@RequestMapping("/settlements")
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @Operation(
            summary = "Simulate a settlement",
            description = "Simulate a settlement based on the provided request",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Settlement simulated",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SettlementSimulationResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request",
                            content = @Content
                    )
            }
    )
    @PostMapping("/simulate")
    public ResponseEntity<SettlementSimulationResponse> simulate(@Valid @RequestBody SettlementRequest request) {
        return ResponseEntity.ok(settlementService.simulate(request));
    }

    @Operation(
            summary = "Create a settlement",
            description = "Create and save a settlement in the database",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Settlement created",
                            content =  @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SettlementResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request",
                            content = @Content
                    )
            }
    )
    @PostMapping
    public ResponseEntity<SettlementResponse> create(@Valid @RequestBody SettlementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(settlementService.save(request));
    }

    @Operation(
            summary = "List settlements statement",
            description = "List settlements statement by filters",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Settlements statement found",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = SettlementStatementResponse.class))
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request",
                            content = @Content
                    )
            }
    )
    @GetMapping("/statement")
    public ResponseEntity<Page<SettlementStatementResponse>> statement(

            @RequestParam
            @NotNull(message = "Start date is required")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam
            @NotNull(message = "End date is required")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @RequestParam(required = false)
            String sellerName,

            @RequestParam(required = false)
            String currencyCode,

            Pageable pageable
    ) {
        return ResponseEntity.ok(
                settlementService.statement(
                        startDate,
                        endDate,
                        sellerName,
                        currencyCode,
                        pageable
                )
        );
    }
}
