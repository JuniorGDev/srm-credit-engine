package br.com.creditengine;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface SettlementStatementProjection {
    Long getId();
    String getSellerName();
    String getReceivableType();
    BigDecimal getFaceValue();
    String getReceivableCurrency();
    String getPaymentCurrency();
    BigDecimal getExchangeRate();
    BigDecimal getPresentValue();
    BigDecimal getDiscountValue();
    BigDecimal getPaymentAmount();
    LocalDate getDueDate();
    LocalDateTime getCreatedAt();
}
