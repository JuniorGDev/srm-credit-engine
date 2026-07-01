package br.com.creditengine.strategies;

import br.com.creditengine.enums.ReceivableType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class DuplicataMercantilStrategyTest {

    @Test
    void getType_ShouldReturnDuplicataMercantil() {
        DuplicataMercantilStrategy strategy = new DuplicataMercantilStrategy();

        assertThat(strategy.getType()).isEqualTo(ReceivableType.DUPLICATA_MERCANTIL);
    }

    @Test
    void getSpread_ShouldReturnCorrectSpreadValue() {
        DuplicataMercantilStrategy strategy = new DuplicataMercantilStrategy();

        assertThat(strategy.getSpread()).isEqualByComparingTo(new BigDecimal("0.015"));
    }

    @Test
    void getSpread_ShouldReturnOnePointFivePercent() {
        DuplicataMercantilStrategy strategy = new DuplicataMercantilStrategy();

        assertThat(strategy.getSpread()).isEqualByComparingTo("0.015");
    }

    @Test
    void strategy_ShouldImplementReceivableStrategy() {
        DuplicataMercantilStrategy strategy = new DuplicataMercantilStrategy();

        assertThat(strategy).isInstanceOf(ReceivableStrategy.class);
    }
}
