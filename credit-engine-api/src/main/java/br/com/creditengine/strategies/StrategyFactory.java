package br.com.creditengine.strategies;

import br.com.creditengine.enums.ReceivableType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class StrategyFactory {

    private final Map<ReceivableType, ReceivableStrategy> strategies;

    public StrategyFactory(List<ReceivableStrategy> strategies) {
        this.strategies = strategies.stream()
                .collect(Collectors.toMap(ReceivableStrategy::getType, Function.identity()));
    }

    public ReceivableStrategy getStrategy(ReceivableType type) {
        return Optional.ofNullable(strategies.get(type))
                .orElseThrow(() -> new IllegalArgumentException("Invalid receivable type: " + type.name()));
    }
}
