package br.com.creditengine.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Exchange rate update request")
public record ExchangeRateUpdateRequest(
        @Schema(
                description = "Exchange rate",
                example = "5.0"
        )
        @NotNull(message = "Exchange rate is required")
        BigDecimal rate
) {
}
