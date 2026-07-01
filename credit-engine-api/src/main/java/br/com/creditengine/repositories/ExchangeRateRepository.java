package br.com.creditengine.repositories;

import br.com.creditengine.entities.Currency;
import br.com.creditengine.entities.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    Optional<ExchangeRate> findByFromCurrencyAndToCurrency(Currency fromCurrency, Currency toCurrency);
    boolean existsByFromCurrencyAndToCurrency(Currency fromCurrency, Currency toCurrency);
}
