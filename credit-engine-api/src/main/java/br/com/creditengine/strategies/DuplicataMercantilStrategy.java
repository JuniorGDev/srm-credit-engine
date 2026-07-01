package br.com.creditengine.strategies;

import br.com.creditengine.enums.ReceivableType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DuplicataMercantilStrategy implements ReceivableStrategy {

    @Override
    public ReceivableType getType() {
        return ReceivableType.DUPLICATA_MERCANTIL;
    }

    @Override
    public BigDecimal getSpread() {
        return new BigDecimal("0.015");
    }

}
