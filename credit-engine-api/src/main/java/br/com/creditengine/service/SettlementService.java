package br.com.creditengine.service;

import br.com.creditengine.calculators.PresentValueCalculator;
import br.com.creditengine.calculators.SettlementCalculation;
import br.com.creditengine.dtos.request.SettlementRequest;
import br.com.creditengine.dtos.response.SettlementResponse;
import br.com.creditengine.dtos.response.SettlementSimulationResponse;
import br.com.creditengine.dtos.response.SettlementStatementResponse;
import br.com.creditengine.entities.Currency;
import br.com.creditengine.entities.ExchangeRate;
import br.com.creditengine.entities.Receivable;
import br.com.creditengine.entities.Settlement;
import br.com.creditengine.exceptions.ResourceNotFoundException;
import br.com.creditengine.repositories.CurrencyRepository;
import br.com.creditengine.repositories.ExchangeRateRepository;
import br.com.creditengine.repositories.ReceivableRepository;
import br.com.creditengine.repositories.SettlementRepository;
import br.com.creditengine.strategies.StrategyFactory;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
public class SettlementService {

    private final CurrencyRepository currencyRepository;
    private final ExchangeRateRepository exchangeRateRepository;
    private final ReceivableRepository receivableRepository;
    private final SettlementRepository settlementRepository;
    private final StrategyFactory strategyFactory;
    private final PresentValueCalculator presentValueCalculator;

    public SettlementService(
            SettlementRepository settlementRepository,
            ReceivableRepository receivableRepository,
            StrategyFactory strategyFactory,
            PresentValueCalculator presentValueCalculator,
            CurrencyRepository currencyRepository,
            ExchangeRateRepository exchangeRateRepository
    ) {
        this.settlementRepository = settlementRepository;
        this.receivableRepository = receivableRepository;
        this.strategyFactory = strategyFactory;
        this.presentValueCalculator = presentValueCalculator;
        this.currencyRepository = currencyRepository;
        this.exchangeRateRepository = exchangeRateRepository;
    }

    public Page<SettlementStatementResponse> statement(
            LocalDate startDate,
            LocalDate endDate,
            String sellerName,
            String currencyCode,
            Pageable pageable
    ){
        return settlementRepository.findSettlementStatement(sellerName, currencyCode, startDate, endDate, pageable).map(SettlementStatementResponse::from);
    }

    public SettlementSimulationResponse simulate(SettlementRequest request) {
        SettlementContext context = buildContext(request);

        return SettlementSimulationResponse.from(
                context.receivableCurrency(),
                context.paymentCurrency(),
                context.calculation(),
                context.paymentAmount()
        );
    }

    @Transactional
    public SettlementResponse save(SettlementRequest request) {

        SettlementContext context = buildContext(request);

        Receivable receivable = saveReceivable(
                request,
                context.receivableCurrency()
        );

        Settlement settlement = saveSettlement(
                receivable,
                context
        );

        return SettlementResponse.from(settlement);
    }

    private SettlementContext buildContext(SettlementRequest request) {

        Currency receivableCurrency = findCurrency(request.currencyCode());

        Currency paymentCurrency = resolvePaymentCurrency(
                request,
                receivableCurrency
        );

        ExchangeRate exchangeRate = resolveExchangeRate(
                receivableCurrency,
                paymentCurrency
        );

        var calculation = calculatePresentValue(request);

        BigDecimal paymentAmount = calculatePaymentAmount(
                calculation.presentValue(),
                exchangeRate
        );

        return new SettlementContext(
                receivableCurrency,
                paymentCurrency,
                exchangeRate,
                calculation,
                paymentAmount
        );
    }

    private SettlementCalculation calculatePresentValue(SettlementRequest request) {

        var strategy = strategyFactory.getStrategy(request.receivableType());

        return presentValueCalculator.calculate(
                request.faceValue(),
                request.dueDate(),
                strategy.getSpread()
        );
    }

    private Receivable saveReceivable(
            SettlementRequest request,
            Currency currency
    ) {

        var receivable = new Receivable(
                request.sellerName(),
                request.faceValue(),
                request.dueDate(),
                currency,
                request.receivableType()
        );

        return receivableRepository.save(receivable);
    }

    private Settlement saveSettlement(
            Receivable receivable,
            SettlementContext context
    ) {

        Settlement settlement = new Settlement(
                receivable,
                context.paymentCurrency(),
                context.exchangeRate() != null
                        ? context.exchangeRate().getRate()
                        : BigDecimal.ONE,
                context.calculation().presentValue(),
                context.calculation().discountValue(),
                context.paymentAmount()
        );

        return settlementRepository.save(settlement);
    }

    private Currency resolvePaymentCurrency(
            SettlementRequest request,
            Currency receivableCurrency
    ) {

        if (isSameCurrency(
                request.currencyCode(),
                request.paymentCurrencyCode()
        )) {
            return receivableCurrency;
        }

        return findCurrency(request.paymentCurrencyCode());
    }

    private ExchangeRate resolveExchangeRate(
            Currency receivableCurrency,
            Currency paymentCurrency
    ) {

        if (receivableCurrency.equals(paymentCurrency)) {
            return null;
        }

        return findExchangeRate(
                receivableCurrency,
                paymentCurrency
        );
    }

    private boolean isSameCurrency(String from, String to) {
        return from.equalsIgnoreCase(to);
    }

    private Currency findCurrency(String code) {
        return currencyRepository.findByCode(code)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Currency", code));
    }

    private ExchangeRate findExchangeRate(
            Currency from,
            Currency to
    ) {

        return exchangeRateRepository
                .findByFromCurrencyAndToCurrency(from, to)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Exchange rate",
                                from.getCode() + "_" + to.getCode()
                        ));
    }

    private BigDecimal calculatePaymentAmount(
            BigDecimal presentValue,
            ExchangeRate exchangeRate
    ) {

        if (exchangeRate == null) {
            return presentValue;
        }

        return presentValue
                .multiply(exchangeRate.getRate())
                .setScale(2, RoundingMode.HALF_UP);
    }

    public record SettlementContext(
            Currency receivableCurrency,
            Currency paymentCurrency,
            ExchangeRate exchangeRate,
            SettlementCalculation calculation,
            BigDecimal paymentAmount
    ) {
    }
}
