package br.com.creditengine.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Exchange rate request")
public record ExchangeRateRequest(
        @Schema(
                description = "From currency code",
                example = "USD"
        )
        @NotBlank(message = "From currency code is required")
        @Size(min = 3, max = 3)
        String fromCurrency,
        @Schema(
                description = "To currency code",
                example = "BRL"
        )
        @NotBlank(message = "To currency code is required")
        @Size(min = 3, max = 3)
        String toCurrency,
        @Schema(
                description = "Exchange rate",
                example = "5.0"
        )
        @NotNull(message = "Exchange rate is required")
        BigDecimal rate
) {
}
