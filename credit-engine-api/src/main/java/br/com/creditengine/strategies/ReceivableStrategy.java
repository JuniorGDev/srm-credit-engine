package br.com.creditengine.strategies;

import br.com.creditengine.enums.ReceivableType;

import java.math.BigDecimal;

public interface ReceivableStrategy {
    ReceivableType getType();
    BigDecimal getSpread();
}
