package br.com.creditengine.dtos.response;

import br.com.creditengine.entities.Currency;

public record CurrencyResponse(
        Long id,
        String name,
        String code
) {
    public static CurrencyResponse from(Currency currency) {
        return new CurrencyResponse(
                currency.getId(),
                currency.getName(),
                currency.getCode()
        );
    }
}
