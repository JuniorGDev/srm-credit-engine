package br.com.creditengine.calculators;

import br.com.creditengine.exceptions.InvalidSettlementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.data.Offset.offset;

class PresentValueCalculatorTest {

    private PresentValueCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new PresentValueCalculator();
    }

    @Test
    void calculate_ShouldReturnCorrectPresentValue_WhenValidInputs() {
        BigDecimal faceValue = new BigDecimal("10000.00");
        LocalDate dueDate = LocalDate.now().plusDays(30);
        BigDecimal spread = new BigDecimal("0.015");

        SettlementCalculation result = calculator.calculate(faceValue, dueDate, spread);

        assertThat(result.presentValue()).isNotNull();
        assertThat(result.discountValue()).isNotNull();
        assertThat(result.spread()).isEqualTo(spread);
        assertThat(result.presentValue()).isLessThan(faceValue);
        assertThat(result.discountValue()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    void calculate_ShouldReturnCorrectPresentValue_WithDuplicataMercantilSpread() {
        BigDecimal faceValue = new BigDecimal("10000.00");
        LocalDate dueDate = LocalDate.now().plusDays(30);
        BigDecimal spread = new BigDecimal("0.015");

        SettlementCalculation result = calculator.calculate(faceValue, dueDate, spread);

        assertThat(result.spread()).isEqualTo(new BigDecimal("0.015"));
        assertThat(result.presentValue()).isNotNull();
        assertThat(result.discountValue()).isNotNull();
    }

    @Test
    void calculate_ShouldReturnCorrectPresentValue_WithChequePreDatadoSpread() {
        BigDecimal faceValue = new BigDecimal("10000.00");
        LocalDate dueDate = LocalDate.now().plusDays(30);
        BigDecimal spread = new BigDecimal("0.025");

        SettlementCalculation result = calculator.calculate(faceValue, dueDate, spread);

        assertThat(result.spread()).isEqualTo(new BigDecimal("0.025"));
        assertThat(result.presentValue()).isNotNull();
        assertThat(result.discountValue()).isNotNull();
    }

    @Test
    void calculate_ShouldReturnHigherDiscount_WithHigherSpread() {
        BigDecimal faceValue = new BigDecimal("10000.00");
        LocalDate dueDate = LocalDate.now().plusDays(30);
        BigDecimal lowSpread = new BigDecimal("0.015");
        BigDecimal highSpread = new BigDecimal("0.025");

        SettlementCalculation lowSpreadResult = calculator.calculate(faceValue, dueDate, lowSpread);
        SettlementCalculation highSpreadResult = calculator.calculate(faceValue, dueDate, highSpread);

        assertThat(highSpreadResult.discountValue()).isGreaterThan(lowSpreadResult.discountValue());
        assertThat(highSpreadResult.presentValue()).isLessThan(lowSpreadResult.presentValue());
    }

    @Test
    void calculate_ShouldReturnHigherDiscount_WithLongerTerm() {
        BigDecimal faceValue = new BigDecimal("10000.00");
        BigDecimal spread = new BigDecimal("0.015");
        LocalDate shortTermDate = LocalDate.now().plusDays(30);
        LocalDate longTermDate = LocalDate.now().plusDays(90);

        SettlementCalculation shortTermResult = calculator.calculate(faceValue, shortTermDate, spread);
        SettlementCalculation longTermResult = calculator.calculate(faceValue, longTermDate, spread);

        assertThat(longTermResult.discountValue()).isGreaterThan(shortTermResult.discountValue());
        assertThat(longTermResult.presentValue()).isLessThan(shortTermResult.presentValue());
    }

    @Test
    void calculate_ShouldHandleSameDayDueDate() {
        BigDecimal faceValue = new BigDecimal("10000.00");
        LocalDate dueDate = LocalDate.now();
        BigDecimal spread = new BigDecimal("0.015");

        SettlementCalculation result = calculator.calculate(faceValue, dueDate, spread);

        assertThat(result.presentValue()).isNotNull();
        assertThat(result.discountValue()).isNotNull();
        assertThat(result.presentValue()).isCloseTo(faceValue, offset(new BigDecimal("0.01")));
        assertThat(result.discountValue()).isCloseTo(BigDecimal.ZERO, offset(new BigDecimal("0.01")));
    }

    @Test
    void calculate_ShouldHandleFutureDueDate_30Days() {
        BigDecimal faceValue = new BigDecimal("10000.00");
        LocalDate dueDate = LocalDate.now().plusDays(30);
        BigDecimal spread = new BigDecimal("0.015");

        SettlementCalculation result = calculator.calculate(faceValue, dueDate, spread);

        assertThat(result.presentValue()).isNotNull();
        assertThat(result.discountValue()).isNotNull();
        assertThat(result.presentValue()).isLessThan(faceValue);
    }

    @Test
    void calculate_ShouldHandleFutureDueDate_60Days() {
        BigDecimal faceValue = new BigDecimal("10000.00");
        LocalDate dueDate = LocalDate.now().plusDays(60);
        BigDecimal spread = new BigDecimal("0.015");

        SettlementCalculation result = calculator.calculate(faceValue, dueDate, spread);

        assertThat(result.presentValue()).isNotNull();
        assertThat(result.discountValue()).isNotNull();
        assertThat(result.presentValue()).isLessThan(faceValue);
    }

    @Test
    void calculate_ShouldHandleFutureDueDate_90Days() {
        BigDecimal faceValue = new BigDecimal("10000.00");
        LocalDate dueDate = LocalDate.now().plusDays(90);
        BigDecimal spread = new BigDecimal("0.015");

        SettlementCalculation result = calculator.calculate(faceValue, dueDate, spread);

        assertThat(result.presentValue()).isNotNull();
        assertThat(result.discountValue()).isNotNull();
        assertThat(result.presentValue()).isLessThan(faceValue);
    }

    @Test
    void calculate_ShouldHandleFutureDueDate_180Days() {
        BigDecimal faceValue = new BigDecimal("10000.00");
        LocalDate dueDate = LocalDate.now().plusDays(180);
        BigDecimal spread = new BigDecimal("0.015");

        SettlementCalculation result = calculator.calculate(faceValue, dueDate, spread);

        assertThat(result.presentValue()).isNotNull();
        assertThat(result.discountValue()).isNotNull();
        assertThat(result.presentValue()).isLessThan(faceValue);
    }

    @Test
    void calculate_ShouldHandleFutureDueDate_360Days() {
        BigDecimal faceValue = new BigDecimal("10000.00");
        LocalDate dueDate = LocalDate.now().plusDays(360);
        BigDecimal spread = new BigDecimal("0.015");

        SettlementCalculation result = calculator.calculate(faceValue, dueDate, spread);

        assertThat(result.presentValue()).isNotNull();
        assertThat(result.discountValue()).isNotNull();
        assertThat(result.presentValue()).isLessThan(faceValue);
    }

    @Test
    void calculate_ShouldHandleLargeMonetaryValue() {
        BigDecimal faceValue = new BigDecimal("1000000.00");
        LocalDate dueDate = LocalDate.now().plusDays(30);
        BigDecimal spread = new BigDecimal("0.015");

        SettlementCalculation result = calculator.calculate(faceValue, dueDate, spread);

        assertThat(result.presentValue()).isNotNull();
        assertThat(result.discountValue()).isNotNull();
        assertThat(result.presentValue()).isLessThan(faceValue);
        assertThat(result.discountValue()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    void calculate_ShouldHandleVeryLargeMonetaryValue() {
        BigDecimal faceValue = new BigDecimal("100000000.00");
        LocalDate dueDate = LocalDate.now().plusDays(30);
        BigDecimal spread = new BigDecimal("0.015");

        SettlementCalculation result = calculator.calculate(faceValue, dueDate, spread);

        assertThat(result.presentValue()).isNotNull();
        assertThat(result.discountValue()).isNotNull();
        assertThat(result.presentValue()).isLessThan(faceValue);
        assertThat(result.discountValue()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    void calculate_ShouldHandleDecimalPrecision_TwoDecimalPlaces() {
        BigDecimal faceValue = new BigDecimal("10000.55");
        LocalDate dueDate = LocalDate.now().plusDays(30);
        BigDecimal spread = new BigDecimal("0.015");

        SettlementCalculation result = calculator.calculate(faceValue, dueDate, spread);

        assertThat(result.presentValue()).isNotNull();
        assertThat(result.presentValue().scale()).isLessThanOrEqualTo(2);
    }

    @Test
    void calculate_ShouldHandleDecimalPrecision_FourDecimalPlaces() {
        BigDecimal faceValue = new BigDecimal("10000.5555");
        LocalDate dueDate = LocalDate.now().plusDays(30);
        BigDecimal spread = new BigDecimal("0.015");

        SettlementCalculation result = calculator.calculate(faceValue, dueDate, spread);

        assertThat(result.presentValue()).isNotNull();
        assertThat(result.presentValue().scale()).isLessThanOrEqualTo(2);
    }

    @Test
    void calculate_ShouldHandleDecimalPrecision_SpreadWithDecimals() {
        BigDecimal faceValue = new BigDecimal("10000.00");
        LocalDate dueDate = LocalDate.now().plusDays(30);
        BigDecimal spread = new BigDecimal("0.0155");

        SettlementCalculation result = calculator.calculate(faceValue, dueDate, spread);

        assertThat(result.presentValue()).isNotNull();
        assertThat(result.spread()).isEqualTo(new BigDecimal("0.0155"));
    }

    @Test
    void calculate_ShouldRoundPresentValue_HalfUp() {
        BigDecimal faceValue = new BigDecimal("10000.00");
        LocalDate dueDate = LocalDate.now().plusDays(30);
        BigDecimal spread = new BigDecimal("0.015");

        SettlementCalculation result = calculator.calculate(faceValue, dueDate, spread);

        assertThat(result.presentValue()).isNotNull();
        assertThat(result.presentValue().scale()).isLessThanOrEqualTo(2);
    }

    @Test
    void calculate_ShouldRoundDiscountValue_Correctly() {
        BigDecimal faceValue = new BigDecimal("10000.00");
        LocalDate dueDate = LocalDate.now().plusDays(30);
        BigDecimal spread = new BigDecimal("0.015");

        SettlementCalculation result = calculator.calculate(faceValue, dueDate, spread);

        assertThat(result.discountValue()).isNotNull();
        assertThat(result.discountValue()).isEqualTo(faceValue.subtract(result.presentValue()));
    }

    @Test
    void calculate_ShouldHandleZeroFaceValue() {
        BigDecimal faceValue = BigDecimal.ZERO;
        LocalDate dueDate = LocalDate.now().plusDays(30);
        BigDecimal spread = new BigDecimal("0.015");

        SettlementCalculation result = calculator.calculate(faceValue, dueDate, spread);

        assertThat(result.presentValue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.discountValue()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void calculate_ShouldHandleZeroSpread() {
        BigDecimal faceValue = new BigDecimal("10000.00");
        LocalDate dueDate = LocalDate.now().plusDays(30);
        BigDecimal spread = BigDecimal.ZERO;

        SettlementCalculation result = calculator.calculate(faceValue, dueDate, spread);

        assertThat(result.presentValue()).isNotNull();
        assertThat(result.spread()).isEqualTo(BigDecimal.ZERO);
        assertThat(result.presentValue()).isLessThan(faceValue);
    }

    @Test
    void calculate_ShouldHandleSmallFaceValue() {
        BigDecimal faceValue = new BigDecimal("1.00");
        LocalDate dueDate = LocalDate.now().plusDays(30);
        BigDecimal spread = new BigDecimal("0.015");

        SettlementCalculation result = calculator.calculate(faceValue, dueDate, spread);

        assertThat(result.presentValue()).isNotNull();
        assertThat(result.presentValue()).isLessThan(faceValue);
    }

    @Test
    void calculate_ShouldHandleVerySmallFaceValue() {
        BigDecimal faceValue = new BigDecimal("0.01");
        LocalDate dueDate = LocalDate.now().plusDays(30);
        BigDecimal spread = new BigDecimal("0.015");

        SettlementCalculation result = calculator.calculate(faceValue, dueDate, spread);

        assertThat(result.presentValue()).isNotNull();
        assertThat(result.presentValue()).isLessThanOrEqualTo(faceValue);
    }

    @Test
    void calculate_ShouldThrowException_WhenDueDateIsInPast() {
        BigDecimal faceValue = new BigDecimal("10000.00");
        LocalDate dueDate = LocalDate.now().minusDays(1);
        BigDecimal spread = new BigDecimal("0.015");

        assertThatThrownBy(() -> calculator.calculate(faceValue, dueDate, spread))
                .isInstanceOf(InvalidSettlementException.class)
                .hasMessageContaining("Receivable is overdue");
    }

    @Test
    void calculate_ShouldThrowException_WhenDueDateIsYesterday() {
        BigDecimal faceValue = new BigDecimal("10000.00");
        LocalDate dueDate = LocalDate.now().minusDays(1);
        BigDecimal spread = new BigDecimal("0.015");

        assertThatThrownBy(() -> calculator.calculate(faceValue, dueDate, spread))
                .isInstanceOf(InvalidSettlementException.class)
                .hasMessageContaining("Receivable is overdue");
    }

    @Test
    void calculate_ShouldThrowException_WhenDueDateIsLastMonth() {
        BigDecimal faceValue = new BigDecimal("10000.00");
        LocalDate dueDate = LocalDate.now().minusDays(30);
        BigDecimal spread = new BigDecimal("0.015");

        assertThatThrownBy(() -> calculator.calculate(faceValue, dueDate, spread))
                .isInstanceOf(InvalidSettlementException.class)
                .hasMessageContaining("Receivable is overdue");
    }

    @Test
    void calculate_ShouldHandleBoundaryCondition_OneDayFuture() {
        BigDecimal faceValue = new BigDecimal("10000.00");
        LocalDate dueDate = LocalDate.now().plusDays(1);
        BigDecimal spread = new BigDecimal("0.015");

        SettlementCalculation result = calculator.calculate(faceValue, dueDate, spread);

        assertThat(result.presentValue()).isNotNull();
        assertThat(result.discountValue()).isNotNull();
    }

    @Test
    void calculate_ShouldHandleBoundaryCondition_ExactlyOneMonth() {
        BigDecimal faceValue = new BigDecimal("10000.00");
        LocalDate dueDate = LocalDate.now().plusDays(30);
        BigDecimal spread = new BigDecimal("0.015");

        SettlementCalculation result = calculator.calculate(faceValue, dueDate, spread);

        assertThat(result.presentValue()).isNotNull();
        assertThat(result.discountValue()).isNotNull();
    }

    @Test
    void calculate_ShouldHandleBoundaryCondition_ExactlyTwoMonths() {
        BigDecimal faceValue = new BigDecimal("10000.00");
        LocalDate dueDate = LocalDate.now().plusDays(60);
        BigDecimal spread = new BigDecimal("0.015");

        SettlementCalculation result = calculator.calculate(faceValue, dueDate, spread);

        assertThat(result.presentValue()).isNotNull();
        assertThat(result.discountValue()).isNotNull();
    }

    @Test
    void calculate_ShouldHandleBoundaryCondition_ExactlySixMonths() {
        BigDecimal faceValue = new BigDecimal("10000.00");
        LocalDate dueDate = LocalDate.now().plusDays(180);
        BigDecimal spread = new BigDecimal("0.015");

        SettlementCalculation result = calculator.calculate(faceValue, dueDate, spread);

        assertThat(result.presentValue()).isNotNull();
        assertThat(result.discountValue()).isNotNull();
    }

    @Test
    void calculate_ShouldHandleBoundaryCondition_ExactlyOneYear() {
        BigDecimal faceValue = new BigDecimal("10000.00");
        LocalDate dueDate = LocalDate.now().plusDays(360);
        BigDecimal spread = new BigDecimal("0.015");

        SettlementCalculation result = calculator.calculate(faceValue, dueDate, spread);

        assertThat(result.presentValue()).isNotNull();
        assertThat(result.discountValue()).isNotNull();
    }

    @Test
    void calculate_ShouldHandleBoundaryCondition_29Days() {
        BigDecimal faceValue = new BigDecimal("10000.00");
        LocalDate dueDate = LocalDate.now().plusDays(29);
        BigDecimal spread = new BigDecimal("0.015");

        SettlementCalculation result = calculator.calculate(faceValue, dueDate, spread);

        assertThat(result.presentValue()).isNotNull();
        assertThat(result.discountValue()).isNotNull();
    }

    @Test
    void calculate_ShouldHandleBoundaryCondition_31Days() {
        BigDecimal faceValue = new BigDecimal("10000.00");
        LocalDate dueDate = LocalDate.now().plusDays(31);
        BigDecimal spread = new BigDecimal("0.015");

        SettlementCalculation result = calculator.calculate(faceValue, dueDate, spread);

        assertThat(result.presentValue()).isNotNull();
        assertThat(result.discountValue()).isNotNull();
    }

    @Test
    void calculate_ShouldHandleBoundaryCondition_59Days() {
        BigDecimal faceValue = new BigDecimal("10000.00");
        LocalDate dueDate = LocalDate.now().plusDays(59);
        BigDecimal spread = new BigDecimal("0.015");

        SettlementCalculation result = calculator.calculate(faceValue, dueDate, spread);

        assertThat(result.presentValue()).isNotNull();
        assertThat(result.discountValue()).isNotNull();
    }

    @Test
    void calculate_ShouldHandleBoundaryCondition_61Days() {
        BigDecimal faceValue = new BigDecimal("10000.00");
        LocalDate dueDate = LocalDate.now().plusDays(61);
        BigDecimal spread = new BigDecimal("0.015");

        SettlementCalculation result = calculator.calculate(faceValue, dueDate, spread);

        assertThat(result.presentValue()).isNotNull();
        assertThat(result.discountValue()).isNotNull();
    }

    @Test
    void calculate_ShouldHandleHighSpread() {
        BigDecimal faceValue = new BigDecimal("10000.00");
        LocalDate dueDate = LocalDate.now().plusDays(30);
        BigDecimal spread = new BigDecimal("0.10");

        SettlementCalculation result = calculator.calculate(faceValue, dueDate, spread);

        assertThat(result.presentValue()).isNotNull();
        assertThat(result.discountValue()).isNotNull();
        assertThat(result.spread()).isEqualTo(new BigDecimal("0.10"));
    }

    @Test
    void calculate_ShouldHandleVeryHighSpread() {
        BigDecimal faceValue = new BigDecimal("10000.00");
        LocalDate dueDate = LocalDate.now().plusDays(30);
        BigDecimal spread = new BigDecimal("0.50");

        SettlementCalculation result = calculator.calculate(faceValue, dueDate, spread);

        assertThat(result.presentValue()).isNotNull();
        assertThat(result.discountValue()).isNotNull();
        assertThat(result.spread()).isEqualTo(new BigDecimal("0.50"));
    }

    @Test
    void calculate_ShouldHandleNegativeSpread() {
        BigDecimal faceValue = new BigDecimal("10000.00");
        LocalDate dueDate = LocalDate.now().plusDays(30);
        BigDecimal spread = new BigDecimal("-0.01");

        SettlementCalculation result = calculator.calculate(faceValue, dueDate, spread);

        assertThat(result.presentValue()).isNotNull();
        assertThat(result.spread()).isEqualTo(new BigDecimal("-0.01"));
    }

    @Test
    void calculate_ShouldReturnConsistentResults_WithSameInputs() {
        BigDecimal faceValue = new BigDecimal("10000.00");
        LocalDate dueDate = LocalDate.now().plusDays(30);
        BigDecimal spread = new BigDecimal("0.015");

        SettlementCalculation result1 = calculator.calculate(faceValue, dueDate, spread);
        SettlementCalculation result2 = calculator.calculate(faceValue, dueDate, spread);

        assertThat(result1.presentValue()).isEqualTo(result2.presentValue());
        assertThat(result1.discountValue()).isEqualTo(result2.discountValue());
        assertThat(result1.spread()).isEqualTo(result2.spread());
    }

    @Test
    void calculate_ShouldHandleFaceValueWithScaleZero() {
        BigDecimal faceValue = new BigDecimal("10000");
        LocalDate dueDate = LocalDate.now().plusDays(30);
        BigDecimal spread = new BigDecimal("0.015");

        SettlementCalculation result = calculator.calculate(faceValue, dueDate, spread);

        assertThat(result.presentValue()).isNotNull();
        assertThat(result.discountValue()).isNotNull();
    }

    @Test
    void calculate_ShouldHandleSpreadWithHighPrecision() {
        BigDecimal faceValue = new BigDecimal("10000.00");
        LocalDate dueDate = LocalDate.now().plusDays(30);
        BigDecimal spread = new BigDecimal("0.015123456789");

        SettlementCalculation result = calculator.calculate(faceValue, dueDate, spread);

        assertThat(result.presentValue()).isNotNull();
        assertThat(result.spread()).isEqualTo(new BigDecimal("0.015123456789"));
    }

    @Test
    void calculate_ShouldHandleLongTerm_5Years() {
        BigDecimal faceValue = new BigDecimal("10000.00");
        LocalDate dueDate = LocalDate.now().plusDays(1825);
        BigDecimal spread = new BigDecimal("0.015");

        SettlementCalculation result = calculator.calculate(faceValue, dueDate, spread);

        assertThat(result.presentValue()).isNotNull();
        assertThat(result.discountValue()).isNotNull();
        assertThat(result.presentValue()).isLessThan(faceValue);
    }

    @Test
    void calculate_ShouldHandleLongTerm_10Years() {
        BigDecimal faceValue = new BigDecimal("10000.00");
        LocalDate dueDate = LocalDate.now().plusDays(3650);
        BigDecimal spread = new BigDecimal("0.015");

        SettlementCalculation result = calculator.calculate(faceValue, dueDate, spread);

        assertThat(result.presentValue()).isNotNull();
        assertThat(result.discountValue()).isNotNull();
        assertThat(result.presentValue()).isLessThan(faceValue);
    }

    @Test
    void calculate_ShouldHandleVerySmallSpread() {
        BigDecimal faceValue = new BigDecimal("10000.00");
        LocalDate dueDate = LocalDate.now().plusDays(30);
        BigDecimal spread = new BigDecimal("0.001");

        SettlementCalculation result = calculator.calculate(faceValue, dueDate, spread);

        assertThat(result.presentValue()).isNotNull();
        assertThat(result.spread()).isEqualTo(new BigDecimal("0.001"));
    }

    @Test
    void calculate_ShouldHandleSpreadOfOne() {
        BigDecimal faceValue = new BigDecimal("10000.00");
        LocalDate dueDate = LocalDate.now().plusDays(30);
        BigDecimal spread = BigDecimal.ONE;

        SettlementCalculation result = calculator.calculate(faceValue, dueDate, spread);

        assertThat(result.presentValue()).isNotNull();
        assertThat(result.spread()).isEqualTo(BigDecimal.ONE);
    }

    @Test
    void calculate_ShouldValidateDiscountValueCalculation() {
        BigDecimal faceValue = new BigDecimal("10000.00");
        LocalDate dueDate = LocalDate.now().plusDays(30);
        BigDecimal spread = new BigDecimal("0.015");

        SettlementCalculation result = calculator.calculate(faceValue, dueDate, spread);

        assertThat(result.discountValue()).isEqualTo(faceValue.subtract(result.presentValue()));
    }

    @Test
    void calculate_ShouldValidatePresentValuePlusDiscountEqualsFaceValue() {
        BigDecimal faceValue = new BigDecimal("10000.00");
        LocalDate dueDate = LocalDate.now().plusDays(30);
        BigDecimal spread = new BigDecimal("0.015");

        SettlementCalculation result = calculator.calculate(faceValue, dueDate, spread);

        assertThat(result.presentValue().add(result.discountValue())).isEqualTo(faceValue);
    }
}
