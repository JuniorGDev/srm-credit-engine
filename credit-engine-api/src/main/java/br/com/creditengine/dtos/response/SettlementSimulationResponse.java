package br.com.creditengine.dtos.response;

import br.com.creditengine.calculators.SettlementCalculation;
import br.com.creditengine.entities.Currency;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Settlement simulation response")
public record SettlementSimulationResponse(

        @Schema(
                description = "Currency of the receivable",
                example = "USD"
        )
        String receivableCurrency,

        @Schema(
                description = "Currency used for settlement payment",
                example = "BRL"
        )
        String paymentCurrency,

        @Schema(
                description = "Present value calculated after applying the pricing formula",
                example = "4582.70"
        )
        BigDecimal presentValue,

        @Schema(
                description = "Discount amount applied to the receivable",
                example = "417.30"
        )
        BigDecimal discountValue,

        @Schema(
                description = "Estimated amount to be paid after currency conversion, when applicable",
                example = "23702.64"
        )
        BigDecimal paymentAmount

) {

    public static SettlementSimulationResponse from(
            Currency receivableCurrency,
            Currency paymentCurrency,
            SettlementCalculation calculation,
            BigDecimal paymentAmount
    ) {

        return new SettlementSimulationResponse(
                receivableCurrency.getCode(),
                paymentCurrency.getCode(),
                calculation.presentValue(),
                calculation.discountValue(),
                paymentAmount
        );
    }

}
