package br.com.creditengine.dtos.response;


import br.com.creditengine.entities.ExchangeRate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ExchangeRateResponse(
        Long id,
        String fromCurrency,
        String toCurrency,
        BigDecimal rate,
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
