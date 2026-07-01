package br.com.creditengine.calculators;

import java.math.BigDecimal;

public record SettlementCalculation(
        BigDecimal presentValue,
        BigDecimal discountValue,
        BigDecimal spread
) {
}
