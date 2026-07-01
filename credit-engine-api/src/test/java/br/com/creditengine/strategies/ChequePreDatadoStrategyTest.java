package br.com.creditengine.strategies;

import br.com.creditengine.enums.ReceivableType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ChequePreDatadoStrategyTest {

    @Test
    void getType_ShouldReturnChequePreDatado() {
        ChequePreDatadoStrategy strategy = new ChequePreDatadoStrategy();

        assertThat(strategy.getType()).isEqualTo(ReceivableType.CHEQUE_PRE_DATADO);
    }

    @Test
    void getSpread_ShouldReturnCorrectSpreadValue() {
        ChequePreDatadoStrategy strategy = new ChequePreDatadoStrategy();

        assertThat(strategy.getSpread()).isEqualByComparingTo(new BigDecimal("0.025"));
    }

    @Test
    void getSpread_ShouldReturnTwoPointFivePercent() {
        ChequePreDatadoStrategy strategy = new ChequePreDatadoStrategy();

        assertThat(strategy.getSpread()).isEqualByComparingTo("0.025");
    }

    @Test
    void strategy_ShouldImplementReceivableStrategy() {
        ChequePreDatadoStrategy strategy = new ChequePreDatadoStrategy();

        assertThat(strategy).isInstanceOf(ReceivableStrategy.class);
    }
}
