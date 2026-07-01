package br.com.creditengine.service;

import br.com.creditengine.dtos.request.CurrencyRequest;
import br.com.creditengine.dtos.response.CurrencyResponse;
import br.com.creditengine.entities.Currency;
import br.com.creditengine.exceptions.CurrencyAlreadyExistsException;
import br.com.creditengine.exceptions.ResourceNotFoundException;
import br.com.creditengine.repositories.CurrencyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CurrencyService {

    private final CurrencyRepository currencyRepository;

    public CurrencyService(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    public CurrencyResponse save(CurrencyRequest currencyRequest) {
        validateCurrencyCode(currencyRequest.code());
        var currency = new Currency(
                currencyRequest.name(),
                currencyRequest.code()
        );

        var currencySaved = currencyRepository.save(currency);

        return CurrencyResponse.from(currencySaved);
    }

    public CurrencyResponse findById(Long id) {
        return CurrencyResponse.from(findCurrency(id));
    }

    public List<CurrencyResponse> findAll() {
        return currencyRepository.findAll().stream().map(CurrencyResponse::from).toList();
    }

    public CurrencyResponse update(Long id, CurrencyRequest currencyRequest) {
        var currencyUpdated = findCurrency(id);
        validateCurrencyCode(id, currencyRequest.code());
        currencyUpdated.update(
                currencyRequest.name(),
                currencyRequest.code()
        );
        var currency = currencyRepository.save(currencyUpdated);
        return CurrencyResponse.from(currency);
    }

    private void validateCurrencyCode(String code) {
        if (currencyRepository.existsByCode(code)) {
            throw new CurrencyAlreadyExistsException(code);
        }
    }

    private void validateCurrencyCode(Long id, String code) {
        if (currencyRepository.existsByCodeAndIdNot(code, id)) {
            throw new CurrencyAlreadyExistsException(code);
        }
    }

    private Currency findCurrency(Long id) {
        return currencyRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Currency", id));
    }
}
