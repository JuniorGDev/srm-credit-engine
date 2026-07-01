package br.com.creditengine.dtos.response;

import br.com.creditengine.entities.Currency;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Currency response")
public record CurrencyResponse(

        @Schema(
                description = "ID of the currency",
                example = "1"
        )
        Long id,

        @Schema(
                description = "Name of the currency",
                example = "Real"
        )
        String name,

        @Schema(
                description = "Code of the currency",
                example = "BRL"
        )
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
