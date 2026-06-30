package br.com.creditengine.entities;

import jakarta.persistence.*;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "exchange_rate")
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_currency_id", nullable = false)
    private Currency fromCurrency;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_currency_id", nullable = false)
    private Currency toCurrency;
    @Column(nullable = false)
    private BigDecimal rate;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ExchangeRate(Currency fromCurrency, Currency toCurrency, BigDecimal rate) {
        update(rate);
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.createdAt = LocalDateTime.now();
    }

    public void update(
            BigDecimal rate
    ) {
        this.rate = rate;
        this.updatedAt = LocalDateTime.now();
    }
}
