package br.com.creditengine.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "settlement")
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receivable_id", nullable = false)
    private Receivable receivable;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_currency_id", nullable = false)
    private Currency paymentCurrency;
    @Column(name = "exchange_rate_value", nullable = false)
    private BigDecimal exchangeRateValue;
    @Column(name = "present_value", nullable = false)
    private BigDecimal presentValue;
    @Column(name = "discount_value", nullable = false)
    private BigDecimal discountValue;
    @Column(name = "settled_amount", nullable = false)
    private BigDecimal settled_amount;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Settlement(
            Receivable receivable,
            Currency paymentCurrency,
            BigDecimal exchangeRateValue,
            BigDecimal presentValue,
            BigDecimal discountValue,
            BigDecimal settled_amount
    ) {
        this.receivable = receivable;
        this.paymentCurrency = paymentCurrency;
        this.exchangeRateValue = exchangeRateValue;
        this.presentValue = presentValue;
        this.discountValue = discountValue;
        this.settled_amount = settled_amount;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
