package br.com.creditengine.calculators;

import br.com.creditengine.exceptions.InvalidSettlementException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class PresentValueCalculator {

    private static final BigDecimal BASE_RATE =
            new BigDecimal("0.01");

    public SettlementCalculation calculate(
            BigDecimal faceValue,
            LocalDate dueDate,
            BigDecimal spread
    ) {
        var termInMonths = calculateTermInMonths(dueDate);
        var effectiveRate = calculateEffectiveRate(spread, termInMonths);
        var presentValue = calculatePresentValue(faceValue, effectiveRate);
        var discountValue = calculateDiscountValue(faceValue, presentValue);
        return new SettlementCalculation(
                presentValue,
                discountValue,
                spread
        );
    }

    private BigDecimal calculateEffectiveRate(BigDecimal spread, BigDecimal termInMonths) {
        var effectiveRate = BASE_RATE.add(spread);
        double factor = Math.pow(
                BigDecimal.ONE.add(effectiveRate).doubleValue(),
                termInMonths.doubleValue()
        );
        return BigDecimal.valueOf(factor);
    }

    private BigDecimal calculateTermInMonths(LocalDate dueDate) {

        final BigDecimal DAYS_IN_MONTH = BigDecimal.valueOf(30);

        long days = ChronoUnit.DAYS.between(LocalDate.now(), dueDate);

        if (days < 0) {
            throw new InvalidSettlementException("Receivable is overdue");
        }

        return BigDecimal.valueOf(days)
                .divide(DAYS_IN_MONTH, 8, RoundingMode.HALF_UP);
    }

    private BigDecimal calculatePresentValue(BigDecimal faceValue, BigDecimal effectiveRate) {
        return faceValue.divide(effectiveRate, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateDiscountValue(BigDecimal faceValue, BigDecimal presentValue) {
        return faceValue.subtract(presentValue);
    }
}
