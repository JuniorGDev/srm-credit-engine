package br.com.creditengine.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "currency")
public class Currency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, length = 3, unique = true)
    private String code;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Currency(String name, String code) {
        update(name, code);
        this.createdAt = LocalDateTime.now();
    }

    public void update(String name, String code) {
        this.name = name;
        this.code = code.trim().toUpperCase();
        this.updatedAt = LocalDateTime.now();
    }
}
