package br.com.creditengine.entities;

import br.com.creditengine.enums.ReceivableType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "receivable")
public class Receivable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "seller_name", nullable = false, length = 150)
    private String sellerName;
    @Column(name = "face_value", nullable = false)
    private BigDecimal faceValue;
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;
    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "currency_id", nullable = false)
    private Currency currency;
    @Enumerated(EnumType.STRING)
    @Column(name = "receivable_type", nullable = false)
    private ReceivableType receivableType;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Receivable(
            String sellerName,
            BigDecimal faceValue,
            LocalDate dueDate,
            Currency currency,
            ReceivableType receivableType
    ) {
        this.sellerName = sellerName;
        this.faceValue = faceValue;
        this.dueDate = dueDate;
        this.currency = currency;
        this.receivableType = receivableType;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
