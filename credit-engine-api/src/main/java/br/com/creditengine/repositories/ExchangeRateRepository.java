package br.com.creditengine.repositories;

import br.com.creditengine.entities.Currency;
import br.com.creditengine.entities.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    boolean existsByFromCurrencyAndToCurrencyAndIdNot(Currency fromCurrency, Currency toCurrency, Long id);
    boolean existsByFromCurrencyAndToCurrency(Currency fromCurrency, Currency toCurrency);
}
