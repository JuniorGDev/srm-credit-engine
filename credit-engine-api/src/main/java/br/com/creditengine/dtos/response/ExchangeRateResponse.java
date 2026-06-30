package br.com.creditengine.dtos.response;


import br.com.creditengine.entities.ExchangeRate;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Exchange rate response")
public record ExchangeRateResponse(
        @Schema(
                description = "ID of the exchange rate",
                example = "1"
        )
        Long id,
        @Schema(
                description = "Code of the currency from",
                example = "BRL"
        )
        String fromCurrency,
        @Schema(
                description = "Code of the currency to",
                example = "USD"
        )
        String toCurrency,
        @Schema(
                description = "Rate of the exchange",
                example = "5.00"
        )
        BigDecimal rate,
        @Schema(
                description = "Date and time of the last update",
                example = "2023-01-01T00:00:00"
        )
        LocalDateTime updatedAt
) {
    public static ExchangeRateResponse from(ExchangeRate exchangeRate) {
        return  new ExchangeRateResponse(
                exchangeRate.getId(),
                exchangeRate.getFromCurrency().getCode(),
                exchangeRate.getToCurrency().getCode(),
                exchangeRate.getRate(),
                exchangeRate.getUpdatedAt()
        );
    }
}
