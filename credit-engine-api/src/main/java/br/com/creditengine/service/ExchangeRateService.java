package br.com.creditengine.service;

import br.com.creditengine.dtos.request.ExchangeRateRequest;
import br.com.creditengine.dtos.request.ExchangeRateUpdateRequest;
import br.com.creditengine.dtos.response.ExchangeRateResponse;
import br.com.creditengine.entities.Currency;
import br.com.creditengine.entities.ExchangeRate;
import br.com.creditengine.exceptions.ExchangeRateAlreadyException;
import br.com.creditengine.exceptions.InvalidExchangeRateException;
import br.com.creditengine.exceptions.ResourceNotFoundException;
import br.com.creditengine.repositories.CurrencyRepository;
import br.com.creditengine.repositories.ExchangeRateRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final CurrencyRepository currencyRepository;

    public ExchangeRateService(ExchangeRateRepository exchangeRateRepository, CurrencyRepository currencyRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.currencyRepository = currencyRepository;
    }

    public List<ExchangeRateResponse> findAll() {
        return exchangeRateRepository.findAll().stream().map(ExchangeRateResponse::from).toList();
    }

    public ExchangeRateResponse findById(Long id) {
        return ExchangeRateResponse.from(findExchangeRate(id));
    }

    public ExchangeRateResponse save(ExchangeRateRequest exchangeRateRequest) {
        validateDifferentCurrencies(exchangeRateRequest.fromCurrency(), exchangeRateRequest.toCurrency());
        var fromCurrency = findCurrency(exchangeRateRequest.fromCurrency());
        var toCurrency = findCurrency(exchangeRateRequest.toCurrency());
        validateExchangeRateAlreadyExists(fromCurrency, toCurrency);
        var exchangeRate = new ExchangeRate(fromCurrency, toCurrency, exchangeRateRequest.rate());
        var saved = exchangeRateRepository.save(exchangeRate);
        return ExchangeRateResponse.from(saved);
    }

    public ExchangeRateResponse update(Long id, ExchangeRateUpdateRequest exchangeRateUpdateRequest) {
        var exchangeRateUpdate = findExchangeRate(id);
        exchangeRateUpdate.update(exchangeRateUpdateRequest.rate());
        var updated = exchangeRateRepository.save(exchangeRateUpdate);
        return ExchangeRateResponse.from(updated);
    }

    private ExchangeRate findExchangeRate(Long id) {
        return exchangeRateRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Exchange rate", id));
    }

    private Currency findCurrency(String code) {
        return currencyRepository.findByCode(code).orElseThrow(() -> new ResourceNotFoundException("Currency", code));
    }

    private void validateExchangeRateAlreadyExists(Currency fromCurrency, Currency toCurrency) {
        if (exchangeRateRepository.existsByFromCurrencyAndToCurrency(fromCurrency, toCurrency)) {
            throw new ExchangeRateAlreadyException(fromCurrency.getCode() + " to " + toCurrency.getCode());
        }
    }

    private void validateDifferentCurrencies(String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            throw new InvalidExchangeRateException();
        }
    }
}
