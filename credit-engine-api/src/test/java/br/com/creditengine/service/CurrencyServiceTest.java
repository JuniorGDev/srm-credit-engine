package br.com.creditengine.service;

import br.com.creditengine.dtos.request.CurrencyRequest;
import br.com.creditengine.dtos.response.CurrencyResponse;
import br.com.creditengine.entities.Currency;
import br.com.creditengine.exceptions.CurrencyAlreadyExistsException;
import br.com.creditengine.exceptions.ResourceNotFoundException;
import br.com.creditengine.repositories.CurrencyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrencyServiceTest {

    @Mock
    private CurrencyRepository currencyRepository;

    @InjectMocks
    private CurrencyService currencyService;

    private Currency currency;
    private CurrencyRequest validRequest;

    @BeforeEach
    void setUp() {
        currency = new Currency("Brazilian Real", "BRL");
        currency.setId(1L);

        validRequest = new CurrencyRequest("Brazilian Real", "BRL");
    }

    @Test
    void save_ShouldReturnCurrencyResponse_WhenValidRequest() {
        when(currencyRepository.existsByCode("BRL")).thenReturn(false);
        when(currencyRepository.save(any(Currency.class))).thenReturn(currency);

        CurrencyResponse response = currencyService.save(validRequest);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Brazilian Real");
        assertThat(response.code()).isEqualTo("BRL");

        verify(currencyRepository).existsByCode("BRL");
        verify(currencyRepository).save(any(Currency.class));
    }

    @Test
    void save_ShouldCreateCurrencyWithCorrectFields_WhenValidRequest() {
        when(currencyRepository.existsByCode("BRL")).thenReturn(false);
        when(currencyRepository.save(any(Currency.class))).thenReturn(currency);

        currencyService.save(validRequest);

        verify(currencyRepository).save(argThat(c ->
                c.getName().equals("Brazilian Real") &&
                c.getCode().equals("BRL") &&
                c.getCreatedAt() != null
        ));
    }

    @Test
    void save_ShouldConvertCodeToUpperCase_WhenValidRequest() {
        CurrencyRequest request = new CurrencyRequest("Brazilian Real", "brl");
        when(currencyRepository.existsByCode("brl")).thenReturn(false);
        when(currencyRepository.save(any(Currency.class))).thenReturn(currency);

        currencyService.save(request);

        verify(currencyRepository).save(argThat(c -> c.getCode().equals("BRL")));
    }

    @Test
    void save_ShouldThrowCurrencyAlreadyExistsException_WhenCodeAlreadyExists() {
        when(currencyRepository.existsByCode("BRL")).thenReturn(true);

        assertThatThrownBy(() -> currencyService.save(validRequest))
                .isInstanceOf(CurrencyAlreadyExistsException.class)
                .hasMessageContaining("Currency already exists with code: BRL");

        verify(currencyRepository).existsByCode("BRL");
        verify(currencyRepository, never()).save(any());
    }

    @Test
    void findById_ShouldReturnCurrencyResponse_WhenCurrencyExists() {
        when(currencyRepository.findById(1L)).thenReturn(Optional.of(currency));

        CurrencyResponse response = currencyService.findById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Brazilian Real");
        assertThat(response.code()).isEqualTo("BRL");

        verify(currencyRepository).findById(1L);
    }

    @Test
    void findById_ShouldThrowResourceNotFoundException_WhenCurrencyNotFound() {
        when(currencyRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> currencyService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Currency")
                .hasMessageContaining("999");

        verify(currencyRepository).findById(999L);
    }

    @Test
    void findAll_ShouldReturnListOfCurrencyResponses_WhenCurrenciesExist() {
        Currency usdCurrency = new Currency("US Dollar", "USD");
        usdCurrency.setId(2L);

        when(currencyRepository.findAll()).thenReturn(List.of(currency, usdCurrency));

        List<CurrencyResponse> responses = currencyService.findAll();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).id()).isEqualTo(1L);
        assertThat(responses.get(0).name()).isEqualTo("Brazilian Real");
        assertThat(responses.get(0).code()).isEqualTo("BRL");
        assertThat(responses.get(1).id()).isEqualTo(2L);
        assertThat(responses.get(1).name()).isEqualTo("US Dollar");
        assertThat(responses.get(1).code()).isEqualTo("USD");

        verify(currencyRepository).findAll();
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoCurrenciesExist() {
        when(currencyRepository.findAll()).thenReturn(List.of());

        List<CurrencyResponse> responses = currencyService.findAll();

        assertThat(responses).isEmpty();

        verify(currencyRepository).findAll();
    }

    @Test
    void update_ShouldReturnCurrencyResponse_WhenValidRequest() {
        when(currencyRepository.findById(1L)).thenReturn(Optional.of(currency));
        when(currencyRepository.existsByCodeAndIdNot("USD", 1L)).thenReturn(false);
        Currency updatedCurrency = new Currency("US Dollar", "USD");
        updatedCurrency.setId(1L);
        when(currencyRepository.save(any(Currency.class))).thenReturn(updatedCurrency);

        CurrencyRequest updateRequest = new CurrencyRequest("US Dollar", "USD");
        CurrencyResponse response = currencyService.update(1L, updateRequest);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("US Dollar");
        assertThat(response.code()).isEqualTo("USD");

        verify(currencyRepository).findById(1L);
        verify(currencyRepository).existsByCodeAndIdNot("USD", 1L);
        verify(currencyRepository).save(any(Currency.class));
    }

    @Test
    void update_ShouldUpdateCurrencyFields_WhenValidRequest() {
        when(currencyRepository.findById(1L)).thenReturn(Optional.of(currency));
        when(currencyRepository.existsByCodeAndIdNot("USD", 1L)).thenReturn(false);
        Currency updatedCurrency = new Currency("US Dollar", "USD");
        updatedCurrency.setId(1L);
        when(currencyRepository.save(any(Currency.class))).thenReturn(updatedCurrency);

        CurrencyRequest updateRequest = new CurrencyRequest("US Dollar", "USD");
        currencyService.update(1L, updateRequest);

        verify(currencyRepository).save(argThat(c ->
                c.getName().equals("US Dollar") &&
                c.getCode().equals("USD") &&
                c.getUpdatedAt() != null
        ));
    }

    @Test
    void update_ShouldConvertCodeToUpperCase_WhenValidRequest() {
        when(currencyRepository.findById(1L)).thenReturn(Optional.of(currency));
        when(currencyRepository.existsByCodeAndIdNot("usd", 1L)).thenReturn(false);
        Currency updatedCurrency = new Currency("US Dollar", "USD");
        updatedCurrency.setId(1L);
        when(currencyRepository.save(any(Currency.class))).thenReturn(updatedCurrency);

        CurrencyRequest updateRequest = new CurrencyRequest("US Dollar", "usd");
        currencyService.update(1L, updateRequest);

        verify(currencyRepository).save(argThat(c -> c.getCode().equals("USD")));
    }

    @Test
    void update_ShouldThrowCurrencyAlreadyExistsException_WhenCodeAlreadyExistsForDifferentId() {
        when(currencyRepository.findById(1L)).thenReturn(Optional.of(currency));
        when(currencyRepository.existsByCodeAndIdNot("USD", 1L)).thenReturn(true);

        CurrencyRequest updateRequest = new CurrencyRequest("US Dollar", "USD");

        assertThatThrownBy(() -> currencyService.update(1L, updateRequest))
                .isInstanceOf(CurrencyAlreadyExistsException.class)
                .hasMessageContaining("Currency already exists with code: USD");

        verify(currencyRepository).findById(1L);
        verify(currencyRepository).existsByCodeAndIdNot("USD", 1L);
        verify(currencyRepository, never()).save(any());
    }

    @Test
    void update_ShouldAllowUpdatingSameCurrencyWithSameCode_WhenCodeUnchanged() {
        when(currencyRepository.findById(1L)).thenReturn(Optional.of(currency));
        when(currencyRepository.existsByCodeAndIdNot("BRL", 1L)).thenReturn(false);
        Currency updatedCurrency = new Currency("Brazilian Real Updated", "BRL");
        updatedCurrency.setId(1L);
        when(currencyRepository.save(any(Currency.class))).thenReturn(updatedCurrency);

        CurrencyRequest updateRequest = new CurrencyRequest("Brazilian Real Updated", "BRL");
        CurrencyResponse response = currencyService.update(1L, updateRequest);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Brazilian Real Updated");
        assertThat(response.code()).isEqualTo("BRL");

        verify(currencyRepository).findById(1L);
        verify(currencyRepository).existsByCodeAndIdNot("BRL", 1L);
        verify(currencyRepository).save(any(Currency.class));
    }

    @Test
    void update_ShouldThrowResourceNotFoundException_WhenCurrencyNotFound() {
        when(currencyRepository.findById(999L)).thenReturn(Optional.empty());

        CurrencyRequest updateRequest = new CurrencyRequest("US Dollar", "USD");

        assertThatThrownBy(() -> currencyService.update(999L, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Currency")
                .hasMessageContaining("999");

        verify(currencyRepository).findById(999L);
        verify(currencyRepository, never()).existsByCodeAndIdNot(anyString(), anyLong());
        verify(currencyRepository, never()).save(any());
    }

    @Test
    void save_ShouldCallRepositoryExistsByCodeOnce() {
        when(currencyRepository.existsByCode("BRL")).thenReturn(false);
        when(currencyRepository.save(any(Currency.class))).thenReturn(currency);

        currencyService.save(validRequest);

        verify(currencyRepository, times(1)).existsByCode("BRL");
    }

    @Test
    void save_ShouldCallRepositorySaveOnce() {
        when(currencyRepository.existsByCode("BRL")).thenReturn(false);
        when(currencyRepository.save(any(Currency.class))).thenReturn(currency);

        currencyService.save(validRequest);

        verify(currencyRepository, times(1)).save(any(Currency.class));
    }

    @Test
    void save_ShouldNotCallRepositorySave_WhenValidationFails() {
        when(currencyRepository.existsByCode("BRL")).thenReturn(true);

        assertThatThrownBy(() -> currencyService.save(validRequest))
                .isInstanceOf(CurrencyAlreadyExistsException.class);

        verify(currencyRepository, never()).save(any());
    }

    @Test
    void findById_ShouldCallRepositoryFindByIdOnce() {
        when(currencyRepository.findById(1L)).thenReturn(Optional.of(currency));

        currencyService.findById(1L);

        verify(currencyRepository, times(1)).findById(1L);
    }

    @Test
    void findAll_ShouldCallRepositoryFindAllOnce() {
        when(currencyRepository.findAll()).thenReturn(List.of());

        currencyService.findAll();

        verify(currencyRepository, times(1)).findAll();
    }

    @Test
    void update_ShouldCallRepositoryFindByIdOnce() {
        when(currencyRepository.findById(1L)).thenReturn(Optional.of(currency));
        when(currencyRepository.existsByCodeAndIdNot("USD", 1L)).thenReturn(false);
        when(currencyRepository.save(any(Currency.class))).thenReturn(currency);

        CurrencyRequest updateRequest = new CurrencyRequest("US Dollar", "USD");
        currencyService.update(1L, updateRequest);

        verify(currencyRepository, times(1)).findById(1L);
    }

    @Test
    void update_ShouldCallRepositoryExistsByCodeAndIdNotOnce() {
        when(currencyRepository.findById(1L)).thenReturn(Optional.of(currency));
        when(currencyRepository.existsByCodeAndIdNot("USD", 1L)).thenReturn(false);
        when(currencyRepository.save(any(Currency.class))).thenReturn(currency);

        CurrencyRequest updateRequest = new CurrencyRequest("US Dollar", "USD");
        currencyService.update(1L, updateRequest);

        verify(currencyRepository, times(1)).existsByCodeAndIdNot("USD", 1L);
    }

    @Test
    void update_ShouldCallRepositorySaveOnce() {
        when(currencyRepository.findById(1L)).thenReturn(Optional.of(currency));
        when(currencyRepository.existsByCodeAndIdNot("USD", 1L)).thenReturn(false);
        when(currencyRepository.save(any(Currency.class))).thenReturn(currency);

        CurrencyRequest updateRequest = new CurrencyRequest("US Dollar", "USD");
        currencyService.update(1L, updateRequest);

        verify(currencyRepository, times(1)).save(any(Currency.class));
    }

    @Test
    void update_ShouldNotCallRepositorySave_WhenCurrencyNotFound() {
        when(currencyRepository.findById(999L)).thenReturn(Optional.empty());

        CurrencyRequest updateRequest = new CurrencyRequest("US Dollar", "USD");

        assertThatThrownBy(() -> currencyService.update(999L, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(currencyRepository, never()).save(any());
    }

    @Test
    void update_ShouldNotCallRepositorySave_WhenValidationFails() {
        when(currencyRepository.findById(1L)).thenReturn(Optional.of(currency));
        when(currencyRepository.existsByCodeAndIdNot("USD", 1L)).thenReturn(true);

        CurrencyRequest updateRequest = new CurrencyRequest("US Dollar", "USD");

        assertThatThrownBy(() -> currencyService.update(1L, updateRequest))
                .isInstanceOf(CurrencyAlreadyExistsException.class);

        verify(currencyRepository, never()).save(any());
    }
}
