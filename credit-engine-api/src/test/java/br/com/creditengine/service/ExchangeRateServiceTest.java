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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @Mock
    private CurrencyRepository currencyRepository;

    @InjectMocks
    private ExchangeRateService exchangeRateService;

    private Currency brlCurrency;
    private Currency usdCurrency;
    private ExchangeRate exchangeRate;
    private ExchangeRateRequest validRequest;
    private ExchangeRateUpdateRequest validUpdateRequest;

    @BeforeEach
    void setUp() {
        brlCurrency = new Currency("Brazilian Real", "BRL");
        brlCurrency.setId(1L);

        usdCurrency = new Currency("US Dollar", "USD");
        usdCurrency.setId(2L);

        exchangeRate = new ExchangeRate(brlCurrency, usdCurrency, new BigDecimal("5.17"));
        exchangeRate.setId(1L);

        validRequest = new ExchangeRateRequest("BRL", "USD", new BigDecimal("5.17"));
        validUpdateRequest = new ExchangeRateUpdateRequest(new BigDecimal("5.50"));
    }

    @Test
    void findAll_ShouldReturnListOfExchangeRateResponses_WhenExchangeRatesExist() {
        ExchangeRate exchangeRate2 = new ExchangeRate(usdCurrency, brlCurrency, new BigDecimal("0.19"));
        exchangeRate2.setId(2L);

        when(exchangeRateRepository.findAll()).thenReturn(List.of(exchangeRate, exchangeRate2));

        List<ExchangeRateResponse> responses = exchangeRateService.findAll();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).id()).isEqualTo(1L);
        assertThat(responses.get(0).fromCurrency()).isEqualTo("BRL");
        assertThat(responses.get(0).toCurrency()).isEqualTo("USD");
        assertThat(responses.get(0).rate()).isEqualTo(new BigDecimal("5.17"));
        assertThat(responses.get(1).id()).isEqualTo(2L);
        assertThat(responses.get(1).fromCurrency()).isEqualTo("USD");
        assertThat(responses.get(1).toCurrency()).isEqualTo("BRL");

        verify(exchangeRateRepository).findAll();
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoExchangeRatesExist() {
        when(exchangeRateRepository.findAll()).thenReturn(List.of());

        List<ExchangeRateResponse> responses = exchangeRateService.findAll();

        assertThat(responses).isEmpty();

        verify(exchangeRateRepository).findAll();
    }

    @Test
    void findById_ShouldReturnExchangeRateResponse_WhenExchangeRateExists() {
        when(exchangeRateRepository.findById(1L)).thenReturn(Optional.of(exchangeRate));

        ExchangeRateResponse response = exchangeRateService.findById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.fromCurrency()).isEqualTo("BRL");
        assertThat(response.toCurrency()).isEqualTo("USD");
        assertThat(response.rate()).isEqualTo(new BigDecimal("5.17"));
        assertThat(response.updatedAt()).isNotNull();

        verify(exchangeRateRepository).findById(1L);
    }

    @Test
    void findById_ShouldThrowResourceNotFoundException_WhenExchangeRateNotFound() {
        when(exchangeRateRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exchangeRateService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Exchange rate")
                .hasMessageContaining("999");

        verify(exchangeRateRepository).findById(999L);
    }

    @Test
    void save_ShouldReturnExchangeRateResponse_WhenValidRequest() {
        when(currencyRepository.findByCode("BRL")).thenReturn(Optional.of(brlCurrency));
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(usdCurrency));
        when(exchangeRateRepository.existsByFromCurrencyAndToCurrency(brlCurrency, usdCurrency))
                .thenReturn(false);
        when(exchangeRateRepository.save(any(ExchangeRate.class))).thenReturn(exchangeRate);

        ExchangeRateResponse response = exchangeRateService.save(validRequest);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.fromCurrency()).isEqualTo("BRL");
        assertThat(response.toCurrency()).isEqualTo("USD");
        assertThat(response.rate()).isEqualTo(new BigDecimal("5.17"));

        verify(currencyRepository).findByCode("BRL");
        verify(currencyRepository).findByCode("USD");
        verify(exchangeRateRepository).existsByFromCurrencyAndToCurrency(brlCurrency, usdCurrency);
        verify(exchangeRateRepository).save(any(ExchangeRate.class));
    }

    @Test
    void save_ShouldThrowInvalidExchangeRateException_WhenFromAndToCurrenciesAreSame() {
        ExchangeRateRequest request = new ExchangeRateRequest("BRL", "BRL", new BigDecimal("5.17"));

        assertThatThrownBy(() -> exchangeRateService.save(request))
                .isInstanceOf(InvalidExchangeRateException.class)
                .hasMessageContaining("From and to currencies cannot be the same");

        verify(currencyRepository, never()).findByCode(anyString());
        verify(exchangeRateRepository, never()).save(any());
    }

    @Test
    void save_ShouldThrowInvalidExchangeRateException_WhenCurrenciesAreEqualCaseInsensitive() {
        ExchangeRateRequest request = new ExchangeRateRequest("brl", "BRL", new BigDecimal("5.17"));

        assertThatThrownBy(() -> exchangeRateService.save(request))
                .isInstanceOf(InvalidExchangeRateException.class)
                .hasMessageContaining("From and to currencies cannot be the same");

        verify(currencyRepository, never()).findByCode(anyString());
        verify(exchangeRateRepository, never()).save(any());
    }

    @Test
    void save_ShouldThrowResourceNotFoundException_WhenFromCurrencyNotFound() {
        when(currencyRepository.findByCode("BRL")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exchangeRateService.save(validRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Currency")
                .hasMessageContaining("BRL");

        verify(currencyRepository).findByCode("BRL");
        verify(currencyRepository, never()).findByCode("USD");
        verify(exchangeRateRepository, never()).save(any());
    }

    @Test
    void save_ShouldThrowResourceNotFoundException_WhenToCurrencyNotFound() {
        when(currencyRepository.findByCode("BRL")).thenReturn(Optional.of(brlCurrency));
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exchangeRateService.save(validRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Currency")
                .hasMessageContaining("USD");

        verify(currencyRepository).findByCode("BRL");
        verify(currencyRepository).findByCode("USD");
        verify(exchangeRateRepository, never()).save(any());
    }

    @Test
    void save_ShouldThrowExchangeRateAlreadyException_WhenExchangeRateForCurrencyPairExists() {
        when(currencyRepository.findByCode("BRL")).thenReturn(Optional.of(brlCurrency));
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(usdCurrency));
        when(exchangeRateRepository.existsByFromCurrencyAndToCurrency(brlCurrency, usdCurrency))
                .thenReturn(true);

        assertThatThrownBy(() -> exchangeRateService.save(validRequest))
                .isInstanceOf(ExchangeRateAlreadyException.class)
                .hasMessageContaining("Exchange already exists")
                .hasMessageContaining("BRL to USD");

        verify(currencyRepository).findByCode("BRL");
        verify(currencyRepository).findByCode("USD");
        verify(exchangeRateRepository).existsByFromCurrencyAndToCurrency(brlCurrency, usdCurrency);
        verify(exchangeRateRepository, never()).save(any());
    }

    @Test
    void save_ShouldCreateExchangeRateWithCorrectFields_WhenValidRequest() {
        when(currencyRepository.findByCode("BRL")).thenReturn(Optional.of(brlCurrency));
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(usdCurrency));
        when(exchangeRateRepository.existsByFromCurrencyAndToCurrency(brlCurrency, usdCurrency))
                .thenReturn(false);
        when(exchangeRateRepository.save(any(ExchangeRate.class))).thenReturn(exchangeRate);

        exchangeRateService.save(validRequest);

        verify(exchangeRateRepository).save(argThat(rate ->
                rate.getFromCurrency().equals(brlCurrency) &&
                rate.getToCurrency().equals(usdCurrency) &&
                rate.getRate().equals(new BigDecimal("5.17")) &&
                rate.getCreatedAt() != null &&
                rate.getUpdatedAt() != null
        ));
    }

    @Test
    void update_ShouldReturnExchangeRateResponse_WhenValidRequest() {
        when(exchangeRateRepository.findById(1L)).thenReturn(Optional.of(exchangeRate));
        ExchangeRate updatedExchangeRate = new ExchangeRate(brlCurrency, usdCurrency, new BigDecimal("5.50"));
        updatedExchangeRate.setId(1L);
        updatedExchangeRate.setUpdatedAt(LocalDateTime.now());
        when(exchangeRateRepository.save(any(ExchangeRate.class))).thenReturn(updatedExchangeRate);

        ExchangeRateResponse response = exchangeRateService.update(1L, validUpdateRequest);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.fromCurrency()).isEqualTo("BRL");
        assertThat(response.toCurrency()).isEqualTo("USD");
        assertThat(response.rate()).isEqualTo(new BigDecimal("5.50"));

        verify(exchangeRateRepository).findById(1L);
        verify(exchangeRateRepository).save(any(ExchangeRate.class));
    }

    @Test
    void update_ShouldThrowResourceNotFoundException_WhenExchangeRateNotFound() {
        when(exchangeRateRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exchangeRateService.update(999L, validUpdateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Exchange rate")
                .hasMessageContaining("999");

        verify(exchangeRateRepository).findById(999L);
        verify(exchangeRateRepository, never()).save(any());
    }

    @Test
    void update_ShouldUpdateExchangeRateRate_WhenValidRequest() {
        when(exchangeRateRepository.findById(1L)).thenReturn(Optional.of(exchangeRate));
        ExchangeRate updatedExchangeRate = new ExchangeRate(brlCurrency, usdCurrency, new BigDecimal("5.50"));
        updatedExchangeRate.setId(1L);
        updatedExchangeRate.setUpdatedAt(LocalDateTime.now());
        when(exchangeRateRepository.save(any(ExchangeRate.class))).thenReturn(updatedExchangeRate);

        exchangeRateService.update(1L, validUpdateRequest);

        verify(exchangeRateRepository).save(argThat(rate ->
                rate.getRate().equals(new BigDecimal("5.50")) &&
                rate.getUpdatedAt() != null
        ));
    }

    @Test
    void update_ShouldPreserveOriginalCurrencyPair_WhenUpdatingRate() {
        when(exchangeRateRepository.findById(1L)).thenReturn(Optional.of(exchangeRate));
        ExchangeRate updatedExchangeRate = new ExchangeRate(brlCurrency, usdCurrency, new BigDecimal("5.50"));
        updatedExchangeRate.setId(1L);
        updatedExchangeRate.setUpdatedAt(LocalDateTime.now());
        when(exchangeRateRepository.save(any(ExchangeRate.class))).thenReturn(updatedExchangeRate);

        exchangeRateService.update(1L, validUpdateRequest);

        verify(exchangeRateRepository).save(argThat(rate ->
                rate.getFromCurrency().equals(brlCurrency) &&
                rate.getToCurrency().equals(usdCurrency)
        ));
    }

    @Test
    void findAll_ShouldCallRepositoryFindAllOnce() {
        when(exchangeRateRepository.findAll()).thenReturn(List.of());

        exchangeRateService.findAll();

        verify(exchangeRateRepository, times(1)).findAll();
    }

    @Test
    void findById_ShouldCallRepositoryFindByIdOnce() {
        when(exchangeRateRepository.findById(1L)).thenReturn(Optional.of(exchangeRate));

        exchangeRateService.findById(1L);

        verify(exchangeRateRepository, times(1)).findById(1L);
    }

    @Test
    void save_ShouldCallRepositorySaveOnce() {
        when(currencyRepository.findByCode("BRL")).thenReturn(Optional.of(brlCurrency));
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(usdCurrency));
        when(exchangeRateRepository.existsByFromCurrencyAndToCurrency(brlCurrency, usdCurrency))
                .thenReturn(false);
        when(exchangeRateRepository.save(any(ExchangeRate.class))).thenReturn(exchangeRate);

        exchangeRateService.save(validRequest);

        verify(exchangeRateRepository, times(1)).save(any(ExchangeRate.class));
    }

    @Test
    void save_ShouldCallRepositoryExistsByFromCurrencyAndToCurrencyOnce() {
        when(currencyRepository.findByCode("BRL")).thenReturn(Optional.of(brlCurrency));
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(usdCurrency));
        when(exchangeRateRepository.existsByFromCurrencyAndToCurrency(brlCurrency, usdCurrency))
                .thenReturn(false);
        when(exchangeRateRepository.save(any(ExchangeRate.class))).thenReturn(exchangeRate);

        exchangeRateService.save(validRequest);

        verify(exchangeRateRepository, times(1)).existsByFromCurrencyAndToCurrency(brlCurrency, usdCurrency);
    }

    @Test
    void save_ShouldCallCurrencyRepositoryFindByCodeTwice() {
        when(currencyRepository.findByCode("BRL")).thenReturn(Optional.of(brlCurrency));
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(usdCurrency));
        when(exchangeRateRepository.existsByFromCurrencyAndToCurrency(brlCurrency, usdCurrency))
                .thenReturn(false);
        when(exchangeRateRepository.save(any(ExchangeRate.class))).thenReturn(exchangeRate);

        exchangeRateService.save(validRequest);

        verify(currencyRepository, times(1)).findByCode("BRL");
        verify(currencyRepository, times(1)).findByCode("USD");
    }

    @Test
    void update_ShouldCallRepositoryFindByIdOnce() {
        when(exchangeRateRepository.findById(1L)).thenReturn(Optional.of(exchangeRate));
        when(exchangeRateRepository.save(any(ExchangeRate.class))).thenReturn(exchangeRate);

        exchangeRateService.update(1L, validUpdateRequest);

        verify(exchangeRateRepository, times(1)).findById(1L);
    }

    @Test
    void update_ShouldCallRepositorySaveOnce() {
        when(exchangeRateRepository.findById(1L)).thenReturn(Optional.of(exchangeRate));
        when(exchangeRateRepository.save(any(ExchangeRate.class))).thenReturn(exchangeRate);

        exchangeRateService.update(1L, validUpdateRequest);

        verify(exchangeRateRepository, times(1)).save(any(ExchangeRate.class));
    }

    @Test
    void save_ShouldNotCallRepositorySave_WhenValidationFails() {
        ExchangeRateRequest request = new ExchangeRateRequest("BRL", "BRL", new BigDecimal("5.17"));

        assertThatThrownBy(() -> exchangeRateService.save(request))
                .isInstanceOf(InvalidExchangeRateException.class);

        verify(exchangeRateRepository, never()).save(any());
    }

    @Test
    void update_ShouldNotCallRepositorySave_WhenExchangeRateNotFound() {
        when(exchangeRateRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exchangeRateService.update(999L, validUpdateRequest))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(exchangeRateRepository, never()).save(any());
    }
}
