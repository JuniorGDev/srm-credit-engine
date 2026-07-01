package br.com.creditengine.strategies;

import br.com.creditengine.enums.ReceivableType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StrategyFactoryTest {

    private StrategyFactory strategyFactory;

    @BeforeEach
    void setUp() {
        DuplicataMercantilStrategy duplicataStrategy = new DuplicataMercantilStrategy();
        ChequePreDatadoStrategy chequeStrategy = new ChequePreDatadoStrategy();
        strategyFactory = new StrategyFactory(List.of(duplicataStrategy, chequeStrategy));
    }

    @Test
    void getStrategy_ShouldReturnDuplicataMercantilStrategy_WhenTypeIsDuplicataMercantil() {
        ReceivableStrategy strategy = strategyFactory.getStrategy(ReceivableType.DUPLICATA_MERCANTIL);

        assertThat(strategy).isNotNull();
        assertThat(strategy.getType()).isEqualTo(ReceivableType.DUPLICATA_MERCANTIL);
    }

    @Test
    void getStrategy_ShouldReturnChequePreDatadoStrategy_WhenTypeIsChequePreDatado() {
        ReceivableStrategy strategy = strategyFactory.getStrategy(ReceivableType.CHEQUE_PRE_DATADO);

        assertThat(strategy).isNotNull();
        assertThat(strategy.getType()).isEqualTo(ReceivableType.CHEQUE_PRE_DATADO);
    }

    @Test
    void getStrategy_ShouldReturnCorrectSpread_WhenTypeIsDuplicataMercantil() {
        ReceivableStrategy strategy = strategyFactory.getStrategy(ReceivableType.DUPLICATA_MERCANTIL);

        assertThat(strategy.getSpread()).isEqualByComparingTo(new BigDecimal("0.015"));
    }

    @Test
    void getStrategy_ShouldReturnCorrectSpread_WhenTypeIsChequePreDatado() {
        ReceivableStrategy strategy = strategyFactory.getStrategy(ReceivableType.CHEQUE_PRE_DATADO);

        assertThat(strategy.getSpread()).isEqualByComparingTo(new BigDecimal("0.025"));
    }

    @Test
    void getStrategy_ShouldReturnDuplicataMercantilInstance() {
        ReceivableStrategy strategy = strategyFactory.getStrategy(ReceivableType.DUPLICATA_MERCANTIL);

        assertThat(strategy).isInstanceOf(DuplicataMercantilStrategy.class);
    }

    @Test
    void getStrategy_ShouldReturnChequePreDatadoInstance() {
        ReceivableStrategy strategy = strategyFactory.getStrategy(ReceivableType.CHEQUE_PRE_DATADO);

        assertThat(strategy).isInstanceOf(ChequePreDatadoStrategy.class);
    }

    @Test
    void getStrategy_ShouldThrowException_WhenTypeIsNull() {
        assertThatThrownBy(() -> strategyFactory.getStrategy(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void getStrategy_ShouldReturnSameInstance_WhenCalledMultipleTimesWithSameType() {
        ReceivableStrategy strategy1 = strategyFactory.getStrategy(ReceivableType.DUPLICATA_MERCANTIL);
        ReceivableStrategy strategy2 = strategyFactory.getStrategy(ReceivableType.DUPLICATA_MERCANTIL);

        assertThat(strategy1).isSameAs(strategy2);
    }

    @Test
    void getStrategy_ShouldSupportBothReceivableTypes() {
        ReceivableStrategy duplicataStrategy = strategyFactory.getStrategy(ReceivableType.DUPLICATA_MERCANTIL);
        ReceivableStrategy chequeStrategy = strategyFactory.getStrategy(ReceivableType.CHEQUE_PRE_DATADO);

        assertThat(duplicataStrategy.getType()).isEqualTo(ReceivableType.DUPLICATA_MERCANTIL);
        assertThat(chequeStrategy.getType()).isEqualTo(ReceivableType.CHEQUE_PRE_DATADO);
    }

    @Test
    void constructor_ShouldInitializeStrategies_WhenValidListProvided() {
        DuplicataMercantilStrategy duplicataStrategy = new DuplicataMercantilStrategy();
        ChequePreDatadoStrategy chequeStrategy = new ChequePreDatadoStrategy();
        StrategyFactory factory = new StrategyFactory(List.of(duplicataStrategy, chequeStrategy));

        assertThat(factory.getStrategy(ReceivableType.DUPLICATA_MERCANTIL)).isNotNull();
        assertThat(factory.getStrategy(ReceivableType.CHEQUE_PRE_DATADO)).isNotNull();
    }

    @Test
    void constructor_ShouldHandleSingleStrategy() {
        DuplicataMercantilStrategy duplicataStrategy = new DuplicataMercantilStrategy();
        StrategyFactory factory = new StrategyFactory(List.of(duplicataStrategy));

        assertThat(factory.getStrategy(ReceivableType.DUPLICATA_MERCANTIL)).isNotNull();
    }
}
