package br.com.creditengine.strategies;

import br.com.creditengine.enums.ReceivableType;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

@Component
public class ChequePreDatadoStrategy implements ReceivableStrategy {

    @Override
    public ReceivableType getType() {
        return ReceivableType.CHEQUE_PRE_DATADO;
    }

    @Override
    public BigDecimal getSpread() {
        return new BigDecimal("0.025");
    }

}
