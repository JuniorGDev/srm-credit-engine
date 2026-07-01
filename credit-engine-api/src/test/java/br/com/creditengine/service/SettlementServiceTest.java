package br.com.creditengine.service;

import br.com.creditengine.SettlementStatementProjection;
import br.com.creditengine.calculators.PresentValueCalculator;
import br.com.creditengine.calculators.SettlementCalculation;
import br.com.creditengine.dtos.request.SettlementRequest;
import br.com.creditengine.dtos.response.SettlementResponse;
import br.com.creditengine.dtos.response.SettlementSimulationResponse;
import br.com.creditengine.entities.Currency;
import br.com.creditengine.entities.ExchangeRate;
import br.com.creditengine.entities.Receivable;
import br.com.creditengine.entities.Settlement;
import br.com.creditengine.enums.ReceivableType;
import br.com.creditengine.exceptions.ResourceNotFoundException;
import br.com.creditengine.repositories.CurrencyRepository;
import br.com.creditengine.repositories.ExchangeRateRepository;
import br.com.creditengine.repositories.ReceivableRepository;
import br.com.creditengine.repositories.SettlementRepository;
import br.com.creditengine.strategies.ChequePreDatadoStrategy;
import br.com.creditengine.strategies.DuplicataMercantilStrategy;
import br.com.creditengine.strategies.ReceivableStrategy;
import br.com.creditengine.strategies.StrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @Mock
    private ReceivableRepository receivableRepository;

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private StrategyFactory strategyFactory;

    @Mock
    private PresentValueCalculator presentValueCalculator;

    @InjectMocks
    private SettlementService settlementService;

    private Currency brlCurrency;
    private Currency usdCurrency;
    private ExchangeRate exchangeRate;
    private ReceivableStrategy duplicataStrategy;
    private ReceivableStrategy chequeStrategy;
    private SettlementRequest validRequest;
    private SettlementCalculation calculation;

    @BeforeEach
    void setUp() {
        brlCurrency = new Currency("Brazilian Real", "BRL");
        brlCurrency.setId(1L);

        usdCurrency = new Currency("US Dollar", "USD");
        usdCurrency.setId(2L);

        exchangeRate = new ExchangeRate(brlCurrency, usdCurrency, new BigDecimal("5.17"));
        exchangeRate.setId(1L);

        duplicataStrategy = new DuplicataMercantilStrategy();
        chequeStrategy = new ChequePreDatadoStrategy();

        validRequest = new SettlementRequest(
                "Test Seller",
                new BigDecimal("10000.00"),
                LocalDate.now().plusDays(30),
                "BRL",
                "BRL",
                ReceivableType.DUPLICATA_MERCANTIL
        );

        calculation = new SettlementCalculation(
                new BigDecimal("9500.00"),
                new BigDecimal("500.00"),
                new BigDecimal("0.015")
        );
    }

    @Test
    void simulate_ShouldReturnSimulationResponse_WhenSameCurrency() {
        when(currencyRepository.findByCode("BRL")).thenReturn(Optional.of(brlCurrency));
        when(strategyFactory.getStrategy(ReceivableType.DUPLICATA_MERCANTIL)).thenReturn(duplicataStrategy);
        when(presentValueCalculator.calculate(any(), any(), any())).thenReturn(calculation);

        SettlementSimulationResponse response = settlementService.simulate(validRequest);

        assertNotNull(response);
        assertEquals("BRL", response.receivableCurrency());
        assertEquals("BRL", response.paymentCurrency());
        assertEquals(new BigDecimal("9500.00"), response.presentValue());
        assertEquals(new BigDecimal("500.00"), response.discountValue());
        assertEquals(new BigDecimal("9500.00"), response.paymentAmount());

        verify(currencyRepository).findByCode("BRL");
        verify(strategyFactory).getStrategy(ReceivableType.DUPLICATA_MERCANTIL);
        verify(presentValueCalculator).calculate(any(), any(), any());
        verifyNoInteractions(exchangeRateRepository);
    }

    @Test
    void simulate_ShouldReturnSimulationResponseWithExchangeRate_WhenDifferentCurrencies() {
        SettlementRequest request = new SettlementRequest(
                "Test Seller",
                new BigDecimal("10000.00"),
                LocalDate.now().plusDays(30),
                "USD",
                "BRL",
                ReceivableType.DUPLICATA_MERCANTIL
        );

        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(usdCurrency));
        when(currencyRepository.findByCode("BRL")).thenReturn(Optional.of(brlCurrency));
        when(exchangeRateRepository.findByFromCurrencyAndToCurrency(usdCurrency, brlCurrency))
                .thenReturn(Optional.of(exchangeRate));
        when(strategyFactory.getStrategy(ReceivableType.DUPLICATA_MERCANTIL)).thenReturn(duplicataStrategy);
        when(presentValueCalculator.calculate(any(), any(), any())).thenReturn(calculation);

        SettlementSimulationResponse response = settlementService.simulate(request);

        assertNotNull(response);
        assertEquals("USD", response.receivableCurrency());
        assertEquals("BRL", response.paymentCurrency());
        assertEquals(new BigDecimal("9500.00"), response.presentValue());
        assertEquals(new BigDecimal("500.00"), response.discountValue());
        assertEquals(new BigDecimal("49115.00"), response.paymentAmount());

        verify(currencyRepository).findByCode("USD");
        verify(currencyRepository).findByCode("BRL");
        verify(exchangeRateRepository).findByFromCurrencyAndToCurrency(usdCurrency, brlCurrency);
    }

    @Test
    void simulate_ShouldThrowResourceNotFoundException_WhenReceivableCurrencyNotFound() {
        when(currencyRepository.findByCode("BRL")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> settlementService.simulate(validRequest)
        );

        assertTrue(exception.getMessage().contains("Currency"));
        assertTrue(exception.getMessage().contains("BRL"));
    }

    @Test
    void simulate_ShouldThrowResourceNotFoundException_WhenPaymentCurrencyNotFound() {
        SettlementRequest request = new SettlementRequest(
                "Test Seller",
                new BigDecimal("10000.00"),
                LocalDate.now().plusDays(30),
                "USD",
                "BRL",
                ReceivableType.DUPLICATA_MERCANTIL
        );

        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(usdCurrency));
        when(currencyRepository.findByCode("BRL")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> settlementService.simulate(request)
        );

        assertTrue(exception.getMessage().contains("Currency"));
        assertTrue(exception.getMessage().contains("BRL"));
    }

    @Test
    void simulate_ShouldThrowResourceNotFoundException_WhenExchangeRateNotFound() {
        SettlementRequest request = new SettlementRequest(
                "Test Seller",
                new BigDecimal("10000.00"),
                LocalDate.now().plusDays(30),
                "USD",
                "BRL",
                ReceivableType.DUPLICATA_MERCANTIL
        );

        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(usdCurrency));
        when(currencyRepository.findByCode("BRL")).thenReturn(Optional.of(brlCurrency));
        when(exchangeRateRepository.findByFromCurrencyAndToCurrency(usdCurrency, brlCurrency))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> settlementService.simulate(request)
        );

        assertTrue(exception.getMessage().contains("Exchange rate"));
        assertTrue(exception.getMessage().contains("USD_BRL"));
    }

    @Test
    void save_ShouldReturnSettlementResponse_WhenSameCurrency() {
        when(currencyRepository.findByCode("BRL")).thenReturn(Optional.of(brlCurrency));
        when(strategyFactory.getStrategy(ReceivableType.DUPLICATA_MERCANTIL)).thenReturn(duplicataStrategy);
        when(presentValueCalculator.calculate(any(), any(), any())).thenReturn(calculation);

        Receivable savedReceivable = new Receivable(
                validRequest.sellerName(),
                validRequest.faceValue(),
                validRequest.dueDate(),
                brlCurrency,
                validRequest.receivableType()
        );
        savedReceivable.setId(1L);

        when(receivableRepository.save(any(Receivable.class))).thenReturn(savedReceivable);

        Settlement savedSettlement = new Settlement(
                savedReceivable,
                brlCurrency,
                BigDecimal.ONE,
                calculation.presentValue(),
                calculation.discountValue(),
                calculation.presentValue()
        );
        savedSettlement.setId(1L);

        when(settlementRepository.save(any(Settlement.class))).thenReturn(savedSettlement);

        SettlementResponse response = settlementService.save(validRequest);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Test Seller", response.sellerName());
        assertEquals(new BigDecimal("10000.00"), response.faceValue());
        assertEquals("BRL", response.receivableCurrency());
        assertEquals("BRL", response.paymentCurrency());
        assertEquals(BigDecimal.ONE, response.exchangeRateApplied());
        assertEquals(new BigDecimal("9500.00"), response.presentValue());
        assertEquals(new BigDecimal("500.00"), response.discountValue());
        assertEquals(new BigDecimal("9500.00"), response.settledAmount());
        assertEquals(ReceivableType.DUPLICATA_MERCANTIL, response.receivableType());

        verify(receivableRepository).save(any(Receivable.class));
        verify(settlementRepository).save(any(Settlement.class));
    }

    @Test
    void save_ShouldReturnSettlementResponseWithExchangeRate_WhenDifferentCurrencies() {
        SettlementRequest request = new SettlementRequest(
                "Test Seller",
                new BigDecimal("10000.00"),
                LocalDate.now().plusDays(30),
                "USD",
                "BRL",
                ReceivableType.CHEQUE_PRE_DATADO
        );

        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(usdCurrency));
        when(currencyRepository.findByCode("BRL")).thenReturn(Optional.of(brlCurrency));
        when(exchangeRateRepository.findByFromCurrencyAndToCurrency(usdCurrency, brlCurrency))
                .thenReturn(Optional.of(exchangeRate));
        when(strategyFactory.getStrategy(ReceivableType.CHEQUE_PRE_DATADO)).thenReturn(chequeStrategy);
        when(presentValueCalculator.calculate(any(), any(), any())).thenReturn(calculation);

        Receivable savedReceivable = new Receivable(
                request.sellerName(),
                request.faceValue(),
                request.dueDate(),
                usdCurrency,
                request.receivableType()
        );
        savedReceivable.setId(1L);

        when(receivableRepository.save(any(Receivable.class))).thenReturn(savedReceivable);

        Settlement savedSettlement = new Settlement(
                savedReceivable,
                brlCurrency,
                exchangeRate.getRate(),
                calculation.presentValue(),
                calculation.discountValue(),
                new BigDecimal("49115.00")
        );
        savedSettlement.setId(1L);

        when(settlementRepository.save(any(Settlement.class))).thenReturn(savedSettlement);

        SettlementResponse response = settlementService.save(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Test Seller", response.sellerName());
        assertEquals(new BigDecimal("10000.00"), response.faceValue());
        assertEquals("USD", response.receivableCurrency());
        assertEquals("BRL", response.paymentCurrency());
        assertEquals(new BigDecimal("5.17"), response.exchangeRateApplied());
        assertEquals(new BigDecimal("9500.00"), response.presentValue());
        assertEquals(new BigDecimal("500.00"), response.discountValue());
        assertEquals(new BigDecimal("49115.00"), response.settledAmount());
        assertEquals(ReceivableType.CHEQUE_PRE_DATADO, response.receivableType());
    }

    @Test
    void save_ShouldThrowResourceNotFoundException_WhenCurrencyNotFound() {
        when(currencyRepository.findByCode("BRL")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> settlementService.save(validRequest)
        );

        assertTrue(exception.getMessage().contains("Currency"));
        assertTrue(exception.getMessage().contains("BRL"));

        verify(receivableRepository, never()).save(any());
        verify(settlementRepository, never()).save(any());
    }

    @Test
    void save_ShouldUseChequePreDatadoStrategy_WhenReceivableTypeIsChequePreDatado() {
        SettlementRequest request = new SettlementRequest(
                "Test Seller",
                new BigDecimal("10000.00"),
                LocalDate.now().plusDays(30),
                "BRL",
                "BRL",
                ReceivableType.CHEQUE_PRE_DATADO
        );

        when(currencyRepository.findByCode("BRL")).thenReturn(Optional.of(brlCurrency));
        when(strategyFactory.getStrategy(ReceivableType.CHEQUE_PRE_DATADO)).thenReturn(chequeStrategy);
        when(presentValueCalculator.calculate(any(), any(), any())).thenReturn(calculation);

        Receivable savedReceivable = new Receivable(
                request.sellerName(),
                request.faceValue(),
                request.dueDate(),
                brlCurrency,
                request.receivableType()
        );
        savedReceivable.setId(1L);

        when(receivableRepository.save(any(Receivable.class))).thenReturn(savedReceivable);

        Settlement savedSettlement = new Settlement(
                savedReceivable,
                brlCurrency,
                BigDecimal.ONE,
                calculation.presentValue(),
                calculation.discountValue(),
                calculation.presentValue()
        );
        savedSettlement.setId(1L);

        when(settlementRepository.save(any(Settlement.class))).thenReturn(savedSettlement);

        SettlementResponse response = settlementService.save(request);

        assertNotNull(response);
        assertEquals(ReceivableType.CHEQUE_PRE_DATADO, response.receivableType());

        verify(strategyFactory).getStrategy(ReceivableType.CHEQUE_PRE_DATADO);
    }

    @Test
    void statement_ShouldReturnPageOfSettlementStatements_WhenFiltersAreNull() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<SettlementStatementProjection> settlementPage = new PageImpl<>(List.of());

        when(settlementRepository.findSettlementStatement(null, null, null, null, pageable))
                .thenReturn(settlementPage);

        Page<?> response = settlementService.statement(null, null, null, null, pageable);

        assertNotNull(response);
        assertEquals(0, response.getContent().size());

        verify(settlementRepository).findSettlementStatement(null, null, null, null, pageable);
    }

    @Test
    void statement_ShouldReturnPageOfSettlementStatements_WhenFiltersAreProvided() {
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        String sellerName = "Test Seller";
        String currencyCode = "BRL";
        Pageable pageable = PageRequest.of(0, 10);

        Page<SettlementStatementProjection> settlementPage = new PageImpl<>(List.of());

        when(settlementRepository.findSettlementStatement(
                sellerName, currencyCode, startDate, endDate, pageable
        )).thenReturn(settlementPage);

        Page<?> response = settlementService.statement(startDate, endDate, sellerName, currencyCode, pageable);

        assertNotNull(response);

        verify(settlementRepository).findSettlementStatement(
                sellerName, currencyCode, startDate, endDate, pageable
        );
    }

    @Test
    void simulate_ShouldHandleCaseInsensitiveCurrencyCodes() {
        SettlementRequest request = new SettlementRequest(
                "Test Seller",
                new BigDecimal("10000.00"),
                LocalDate.now().plusDays(30),
                "brl",
                "brl",
                ReceivableType.DUPLICATA_MERCANTIL
        );

        when(currencyRepository.findByCode("brl")).thenReturn(Optional.of(brlCurrency));
        when(strategyFactory.getStrategy(ReceivableType.DUPLICATA_MERCANTIL)).thenReturn(duplicataStrategy);
        when(presentValueCalculator.calculate(any(), any(), any())).thenReturn(calculation);

        SettlementSimulationResponse response = settlementService.simulate(request);

        assertNotNull(response);
        assertEquals("BRL", response.receivableCurrency());
        assertEquals("BRL", response.paymentCurrency());

        verify(currencyRepository).findByCode("brl");
    }

    @Test
    void save_ShouldCreateReceivableWithCorrectFields() {
        when(currencyRepository.findByCode("BRL")).thenReturn(Optional.of(brlCurrency));
        when(strategyFactory.getStrategy(ReceivableType.DUPLICATA_MERCANTIL)).thenReturn(duplicataStrategy);
        when(presentValueCalculator.calculate(any(), any(), any())).thenReturn(calculation);

        Receivable savedReceivable = new Receivable(
                validRequest.sellerName(),
                validRequest.faceValue(),
                validRequest.dueDate(),
                brlCurrency,
                validRequest.receivableType()
        );
        savedReceivable.setId(1L);

        when(receivableRepository.save(any(Receivable.class))).thenReturn(savedReceivable);

        Settlement savedSettlement = new Settlement(
                savedReceivable,
                brlCurrency,
                BigDecimal.ONE,
                calculation.presentValue(),
                calculation.discountValue(),
                calculation.presentValue()
        );
        savedSettlement.setId(1L);

        when(settlementRepository.save(any(Settlement.class))).thenReturn(savedSettlement);

        settlementService.save(validRequest);

        verify(receivableRepository).save(argThat(receivable -> 
                receivable.getSellerName().equals("Test Seller") &&
                receivable.getFaceValue().equals(new BigDecimal("10000.00")) &&
                receivable.getCurrency().equals(brlCurrency) &&
                receivable.getReceivableType() == ReceivableType.DUPLICATA_MERCANTIL
        ));
    }

    @Test
    void save_ShouldCreateSettlementWithCorrectFields() {
        when(currencyRepository.findByCode("BRL")).thenReturn(Optional.of(brlCurrency));
        when(strategyFactory.getStrategy(ReceivableType.DUPLICATA_MERCANTIL)).thenReturn(duplicataStrategy);
        when(presentValueCalculator.calculate(any(), any(), any())).thenReturn(calculation);

        Receivable savedReceivable = new Receivable(
                validRequest.sellerName(),
                validRequest.faceValue(),
                validRequest.dueDate(),
                brlCurrency,
                validRequest.receivableType()
        );
        savedReceivable.setId(1L);

        when(receivableRepository.save(any(Receivable.class))).thenReturn(savedReceivable);

        Settlement savedSettlement = new Settlement(
                savedReceivable,
                brlCurrency,
                BigDecimal.ONE,
                calculation.presentValue(),
                calculation.discountValue(),
                calculation.presentValue()
        );
        savedSettlement.setId(1L);

        when(settlementRepository.save(any(Settlement.class))).thenReturn(savedSettlement);

        settlementService.save(validRequest);

        verify(settlementRepository).save(argThat(settlement ->
                settlement.getPaymentCurrency().equals(brlCurrency) &&
                settlement.getExchangeRateValue().equals(BigDecimal.ONE) &&
                settlement.getPresentValue().equals(calculation.presentValue()) &&
                settlement.getDiscountValue().equals(calculation.discountValue()) &&
                settlement.getSettled_amount().equals(calculation.presentValue())
        ));
    }
}
