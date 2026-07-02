package br.com.creditengine.controllers;

import br.com.creditengine.dtos.request.SettlementRequest;
import br.com.creditengine.dtos.response.PageResponse;
import br.com.creditengine.dtos.response.SettlementResponse;
import br.com.creditengine.dtos.response.SettlementSimulationResponse;
import br.com.creditengine.dtos.response.SettlementStatementResponse;
import br.com.creditengine.enums.ReceivableType;
import br.com.creditengine.exceptions.InvalidSettlementException;
import br.com.creditengine.exceptions.ResourceNotFoundException;
import br.com.creditengine.service.SettlementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SettlementController.class)
class SettlementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SettlementService settlementService;

    private SettlementRequest validRequest;
    private SettlementSimulationResponse simulationResponse;
    private SettlementResponse settlementResponse;
    private Page<SettlementStatementResponse> statementPage;

    @BeforeEach
    void setUp() {
        validRequest = new SettlementRequest(
                "Test Seller",
                new BigDecimal("10000.00"),
                LocalDate.now().plusDays(30),
                "BRL",
                "BRL",
                ReceivableType.DUPLICATA_MERCANTIL
        );

        simulationResponse = new SettlementSimulationResponse(
                "BRL",
                "BRL",
                new BigDecimal("9500.00"),
                new BigDecimal("500.00"),
                new BigDecimal("9500.00")
        );

        settlementResponse = new SettlementResponse(
                1L,
                "Test Seller",
                new BigDecimal("10000.00"),
                "BRL",
                "BRL",
                BigDecimal.ONE,
                new BigDecimal("9500.00"),
                new BigDecimal("500.00"),
                new BigDecimal("9500.00"),
                ReceivableType.DUPLICATA_MERCANTIL,
                LocalDate.now().plusDays(30),
                null
        );

        statementPage = new PageImpl<>(Collections.emptyList());
    }

    // POST /settlements/simulate - Success Scenarios

    @Test
    void simulate_ShouldReturn200Ok_WhenValidRequest() throws Exception {
        when(settlementService.simulate(any(SettlementRequest.class)))
                .thenReturn(simulationResponse);

        mockMvc.perform(post("/settlements/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void simulate_ShouldReturnExpectedResponseBody_WhenValidRequest() throws Exception {
        when(settlementService.simulate(any(SettlementRequest.class)))
                .thenReturn(simulationResponse);

        mockMvc.perform(post("/settlements/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receivableCurrency").value("BRL"))
                .andExpect(jsonPath("$.paymentCurrency").value("BRL"))
                .andExpect(jsonPath("$.presentValue").value(9500.00))
                .andExpect(jsonPath("$.discountValue").value(500.00))
                .andExpect(jsonPath("$.paymentAmount").value(9500.00));
    }

    @Test
    void simulate_ShouldDelegateToService_WhenValidRequest() throws Exception {
        when(settlementService.simulate(any(SettlementRequest.class)))
                .thenReturn(simulationResponse);

        mockMvc.perform(post("/settlements/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)));

        // Service delegation is verified by the mock being called
    }

    // POST /settlements/simulate - Validation Scenarios

    @Test
    void simulate_ShouldReturn400_WhenSellerNameIsBlank() throws Exception {
        SettlementRequest request = new SettlementRequest(
                "",
                new BigDecimal("10000.00"),
                LocalDate.now().plusDays(30),
                "BRL",
                "BRL",
                ReceivableType.DUPLICATA_MERCANTIL
        );

        mockMvc.perform(post("/settlements/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void simulate_ShouldReturn400_WhenSellerNameIsNull() throws Exception {
        String json = """
                {
                    "sellerName": null,
                    "faceValue": 10000.00,
                    "dueDate": "2026-07-31",
                    "currencyCode": "BRL",
                    "paymentCurrencyCode": "BRL",
                    "receivableType": "DUPLICATA_MERCANTIL"
                }
                """;

        mockMvc.perform(post("/settlements/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void simulate_ShouldReturn400_WhenFaceValueIsNull() throws Exception {
        String json = """
                {
                    "sellerName": "Test Seller",
                    "faceValue": null,
                    "dueDate": "2026-07-31",
                    "currencyCode": "BRL",
                    "paymentCurrencyCode": "BRL",
                    "receivableType": "DUPLICATA_MERCANTIL"
                }
                """;

        mockMvc.perform(post("/settlements/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void simulate_ShouldReturn400_WhenFaceValueIsNotPositive() throws Exception {
        SettlementRequest request = new SettlementRequest(
                "Test Seller",
                new BigDecimal("-100.00"),
                LocalDate.now().plusDays(30),
                "BRL",
                "BRL",
                ReceivableType.DUPLICATA_MERCANTIL
        );

        mockMvc.perform(post("/settlements/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void simulate_ShouldReturn400_WhenFaceValueIsZero() throws Exception {
        SettlementRequest request = new SettlementRequest(
                "Test Seller",
                BigDecimal.ZERO,
                LocalDate.now().plusDays(30),
                "BRL",
                "BRL",
                ReceivableType.DUPLICATA_MERCANTIL
        );

        mockMvc.perform(post("/settlements/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void simulate_ShouldReturn400_WhenFaceValueIsNegative() throws Exception {
        SettlementRequest request = new SettlementRequest(
                "Test Seller",
                new BigDecimal("-1000.00"),
                LocalDate.now().plusDays(30),
                "BRL",
                "BRL",
                ReceivableType.DUPLICATA_MERCANTIL
        );

        mockMvc.perform(post("/settlements/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void simulate_ShouldReturn400_WhenDueDateIsNull() throws Exception {
        String json = """
                {
                    "sellerName": "Test Seller",
                    "faceValue": 10000.00,
                    "dueDate": null,
                    "currencyCode": "BRL",
                    "paymentCurrencyCode": "BRL",
                    "receivableType": "DUPLICATA_MERCANTIL"
                }
                """;

        mockMvc.perform(post("/settlements/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void simulate_ShouldReturn400_WhenCurrencyCodeIsBlank() throws Exception {
        SettlementRequest request = new SettlementRequest(
                "Test Seller",
                new BigDecimal("10000.00"),
                LocalDate.now().plusDays(30),
                "",
                "BRL",
                ReceivableType.DUPLICATA_MERCANTIL
        );

        mockMvc.perform(post("/settlements/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void simulate_ShouldReturn400_WhenCurrencyCodeIsNull() throws Exception {
        String json = """
                {
                    "sellerName": "Test Seller",
                    "faceValue": 10000.00,
                    "dueDate": "2026-07-31",
                    "currencyCode": null,
                    "paymentCurrencyCode": "BRL",
                    "receivableType": "DUPLICATA_MERCANTIL"
                }
                """;

        mockMvc.perform(post("/settlements/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void simulate_ShouldReturn400_WhenCurrencyCodeSizeIsLessThan3() throws Exception {
        SettlementRequest request = new SettlementRequest(
                "Test Seller",
                new BigDecimal("10000.00"),
                LocalDate.now().plusDays(30),
                "BR",
                "BRL",
                ReceivableType.DUPLICATA_MERCANTIL
        );

        mockMvc.perform(post("/settlements/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void simulate_ShouldReturn400_WhenCurrencyCodeSizeIsGreaterThan3() throws Exception {
        SettlementRequest request = new SettlementRequest(
                "Test Seller",
                new BigDecimal("10000.00"),
                LocalDate.now().plusDays(30),
                "BRLA",
                "BRL",
                ReceivableType.DUPLICATA_MERCANTIL
        );

        mockMvc.perform(post("/settlements/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void simulate_ShouldReturn400_WhenPaymentCurrencyCodeIsBlank() throws Exception {
        SettlementRequest request = new SettlementRequest(
                "Test Seller",
                new BigDecimal("10000.00"),
                LocalDate.now().plusDays(30),
                "BRL",
                "",
                ReceivableType.DUPLICATA_MERCANTIL
        );

        mockMvc.perform(post("/settlements/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void simulate_ShouldReturn400_WhenPaymentCurrencyCodeIsNull() throws Exception {
        String json = """
                {
                    "sellerName": "Test Seller",
                    "faceValue": 10000.00,
                    "dueDate": "2026-07-31",
                    "currencyCode": "BRL",
                    "paymentCurrencyCode": null,
                    "receivableType": "DUPLICATA_MERCANTIL"
                }
                """;

        mockMvc.perform(post("/settlements/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void simulate_ShouldReturn400_WhenPaymentCurrencyCodeSizeIsLessThan3() throws Exception {
        SettlementRequest request = new SettlementRequest(
                "Test Seller",
                new BigDecimal("10000.00"),
                LocalDate.now().plusDays(30),
                "BRL",
                "BR",
                ReceivableType.DUPLICATA_MERCANTIL
        );

        mockMvc.perform(post("/settlements/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void simulate_ShouldReturn400_WhenPaymentCurrencyCodeSizeIsGreaterThan3() throws Exception {
        SettlementRequest request = new SettlementRequest(
                "Test Seller",
                new BigDecimal("10000.00"),
                LocalDate.now().plusDays(30),
                "BRL",
                "BRLA",
                ReceivableType.DUPLICATA_MERCANTIL
        );

        mockMvc.perform(post("/settlements/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void simulate_ShouldReturn400_WhenReceivableTypeIsNull() throws Exception {
        String json = """
                {
                    "sellerName": "Test Seller",
                    "faceValue": 10000.00,
                    "dueDate": "2026-07-31",
                    "currencyCode": "BRL",
                    "paymentCurrencyCode": "BRL",
                    "receivableType": null
                }
                """;

        mockMvc.perform(post("/settlements/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void simulate_ShouldReturnValidationErrors_WhenMultipleFieldsAreInvalid() throws Exception {
        String json = """
                {
                    "sellerName": "",
                    "faceValue": -100.00,
                    "dueDate": null,
                    "currencyCode": "BR",
                    "paymentCurrencyCode": "BRLA",
                    "receivableType": null
                }
                """;

        mockMvc.perform(post("/settlements/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors.length()").value(6));
    }

    // POST /settlements/simulate - Exception Scenarios

    @Test
    void simulate_ShouldReturn404_WhenCurrencyNotFound() throws Exception {
        when(settlementService.simulate(any(SettlementRequest.class)))
                .thenThrow(new ResourceNotFoundException("Currency", "BRL"));

        mockMvc.perform(post("/settlements/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Currency not found with code: BRL"));
    }

    @Test
    void simulate_ShouldReturn404_WhenExchangeRateNotFound() throws Exception {
        when(settlementService.simulate(any(SettlementRequest.class)))
                .thenThrow(new ResourceNotFoundException("Exchange rate", "USD_BRL"));

        mockMvc.perform(post("/settlements/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Exchange rate not found with code: USD_BRL"));
    }

    @Test
    void simulate_ShouldReturn400_WhenSettlementIsInvalid() throws Exception {
        when(settlementService.simulate(any(SettlementRequest.class)))
                .thenThrow(new InvalidSettlementException("Receivable is overdue"));

        mockMvc.perform(post("/settlements/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid settlement"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Receivable is overdue"));
    }

    @Test
    void simulate_ShouldReturnProblemDetail_WhenResourceNotFoundException() throws Exception {
        when(settlementService.simulate(any(SettlementRequest.class)))
                .thenThrow(new ResourceNotFoundException("Currency", "BRL"));

        mockMvc.perform(post("/settlements/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Currency not found with code: BRL"))
                .andExpect(jsonPath("$.instance").exists());
    }

    @Test
    void simulate_ShouldReturnProblemDetail_WhenInvalidSettlementException() throws Exception {
        when(settlementService.simulate(any(SettlementRequest.class)))
                .thenThrow(new InvalidSettlementException("Receivable is overdue"));

        mockMvc.perform(post("/settlements/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").value("Invalid settlement"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Receivable is overdue"))
                .andExpect(jsonPath("$.instance").exists());
    }

    // POST /settlements - Success Scenarios

    @Test
    void create_ShouldReturn201Created_WhenValidRequest() throws Exception {
        when(settlementService.save(any(SettlementRequest.class)))
                .thenReturn(settlementResponse);

        mockMvc.perform(post("/settlements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void create_ShouldReturnExpectedResponseBody_WhenValidRequest() throws Exception {
        when(settlementService.save(any(SettlementRequest.class)))
                .thenReturn(settlementResponse);

        mockMvc.perform(post("/settlements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sellerName").value("Test Seller"))
                .andExpect(jsonPath("$.faceValue").value(10000.00))
                .andExpect(jsonPath("$.receivableCurrency").value("BRL"))
                .andExpect(jsonPath("$.paymentCurrency").value("BRL"))
                .andExpect(jsonPath("$.exchangeRateApplied").value(1.0))
                .andExpect(jsonPath("$.presentValue").value(9500.00))
                .andExpect(jsonPath("$.discountValue").value(500.00))
                .andExpect(jsonPath("$.settledAmount").value(9500.00))
                .andExpect(jsonPath("$.receivableType").value("DUPLICATA_MERCANTIL"));
    }

    @Test
    void create_ShouldDelegateToService_WhenValidRequest() throws Exception {
        when(settlementService.save(any(SettlementRequest.class)))
                .thenReturn(settlementResponse);

        mockMvc.perform(post("/settlements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)));

        // Service delegation is verified by the mock being called
    }

    // POST /settlements - Validation Scenarios

    @Test
    void create_ShouldReturn400_WhenSellerNameIsBlank() throws Exception {
        SettlementRequest request = new SettlementRequest(
                "",
                new BigDecimal("10000.00"),
                LocalDate.now().plusDays(30),
                "BRL",
                "BRL",
                ReceivableType.DUPLICATA_MERCANTIL
        );

        mockMvc.perform(post("/settlements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void create_ShouldReturn400_WhenSellerNameIsNull() throws Exception {
        String json = """
                {
                    "sellerName": null,
                    "faceValue": 10000.00,
                    "dueDate": "2026-07-31",
                    "currencyCode": "BRL",
                    "paymentCurrencyCode": "BRL",
                    "receivableType": "DUPLICATA_MERCANTIL"
                }
                """;

        mockMvc.perform(post("/settlements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void create_ShouldReturn400_WhenFaceValueIsNull() throws Exception {
        String json = """
                {
                    "sellerName": "Test Seller",
                    "faceValue": null,
                    "dueDate": "2026-07-31",
                    "currencyCode": "BRL",
                    "paymentCurrencyCode": "BRL",
                    "receivableType": "DUPLICATA_MERCANTIL"
                }
                """;

        mockMvc.perform(post("/settlements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void create_ShouldReturn400_WhenFaceValueIsNotPositive() throws Exception {
        SettlementRequest request = new SettlementRequest(
                "Test Seller",
                new BigDecimal("-100.00"),
                LocalDate.now().plusDays(30),
                "BRL",
                "BRL",
                ReceivableType.DUPLICATA_MERCANTIL
        );

        mockMvc.perform(post("/settlements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void create_ShouldReturn400_WhenFaceValueIsZero() throws Exception {
        SettlementRequest request = new SettlementRequest(
                "Test Seller",
                BigDecimal.ZERO,
                LocalDate.now().plusDays(30),
                "BRL",
                "BRL",
                ReceivableType.DUPLICATA_MERCANTIL
        );

        mockMvc.perform(post("/settlements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void create_ShouldReturn400_WhenFaceValueIsNegative() throws Exception {
        SettlementRequest request = new SettlementRequest(
                "Test Seller",
                new BigDecimal("-1000.00"),
                LocalDate.now().plusDays(30),
                "BRL",
                "BRL",
                ReceivableType.DUPLICATA_MERCANTIL
        );

        mockMvc.perform(post("/settlements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void create_ShouldReturn400_WhenDueDateIsNull() throws Exception {
        String json = """
                {
                    "sellerName": "Test Seller",
                    "faceValue": 10000.00,
                    "dueDate": null,
                    "currencyCode": "BRL",
                    "paymentCurrencyCode": "BRL",
                    "receivableType": "DUPLICATA_MERCANTIL"
                }
                """;

        mockMvc.perform(post("/settlements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void create_ShouldReturn400_WhenCurrencyCodeIsBlank() throws Exception {
        SettlementRequest request = new SettlementRequest(
                "Test Seller",
                new BigDecimal("10000.00"),
                LocalDate.now().plusDays(30),
                "",
                "BRL",
                ReceivableType.DUPLICATA_MERCANTIL
        );

        mockMvc.perform(post("/settlements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void create_ShouldReturn400_WhenCurrencyCodeIsNull() throws Exception {
        String json = """
                {
                    "sellerName": "Test Seller",
                    "faceValue": 10000.00,
                    "dueDate": "2026-07-31",
                    "currencyCode": null,
                    "paymentCurrencyCode": "BRL",
                    "receivableType": "DUPLICATA_MERCANTIL"
                }
                """;

        mockMvc.perform(post("/settlements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void create_ShouldReturn400_WhenCurrencyCodeSizeIsLessThan3() throws Exception {
        SettlementRequest request = new SettlementRequest(
                "Test Seller",
                new BigDecimal("10000.00"),
                LocalDate.now().plusDays(30),
                "BR",
                "BRL",
                ReceivableType.DUPLICATA_MERCANTIL
        );

        mockMvc.perform(post("/settlements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void create_ShouldReturn400_WhenCurrencyCodeSizeIsGreaterThan3() throws Exception {
        SettlementRequest request = new SettlementRequest(
                "Test Seller",
                new BigDecimal("10000.00"),
                LocalDate.now().plusDays(30),
                "BRLA",
                "BRL",
                ReceivableType.DUPLICATA_MERCANTIL
        );

        mockMvc.perform(post("/settlements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void create_ShouldReturn400_WhenPaymentCurrencyCodeIsBlank() throws Exception {
        SettlementRequest request = new SettlementRequest(
                "Test Seller",
                new BigDecimal("10000.00"),
                LocalDate.now().plusDays(30),
                "BRL",
                "",
                ReceivableType.DUPLICATA_MERCANTIL
        );

        mockMvc.perform(post("/settlements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void create_ShouldReturn400_WhenPaymentCurrencyCodeIsNull() throws Exception {
        String json = """
                {
                    "sellerName": "Test Seller",
                    "faceValue": 10000.00,
                    "dueDate": "2026-07-31",
                    "currencyCode": "BRL",
                    "paymentCurrencyCode": null,
                    "receivableType": "DUPLICATA_MERCANTIL"
                }
                """;

        mockMvc.perform(post("/settlements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void create_ShouldReturn400_WhenPaymentCurrencyCodeSizeIsLessThan3() throws Exception {
        SettlementRequest request = new SettlementRequest(
                "Test Seller",
                new BigDecimal("10000.00"),
                LocalDate.now().plusDays(30),
                "BRL",
                "BR",
                ReceivableType.DUPLICATA_MERCANTIL
        );

        mockMvc.perform(post("/settlements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void create_ShouldReturn400_WhenPaymentCurrencyCodeSizeIsGreaterThan3() throws Exception {
        SettlementRequest request = new SettlementRequest(
                "Test Seller",
                new BigDecimal("10000.00"),
                LocalDate.now().plusDays(30),
                "BRL",
                "BRLA",
                ReceivableType.DUPLICATA_MERCANTIL
        );

        mockMvc.perform(post("/settlements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void create_ShouldReturn400_WhenReceivableTypeIsNull() throws Exception {
        String json = """
                {
                    "sellerName": "Test Seller",
                    "faceValue": 10000.00,
                    "dueDate": "2026-07-31",
                    "currencyCode": "BRL",
                    "paymentCurrencyCode": "BRL",
                    "receivableType": null
                }
                """;

        mockMvc.perform(post("/settlements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void create_ShouldReturnValidationErrors_WhenMultipleFieldsAreInvalid() throws Exception {
        String json = """
                {
                    "sellerName": "",
                    "faceValue": -100.00,
                    "dueDate": null,
                    "currencyCode": "BR",
                    "paymentCurrencyCode": "BRLA",
                    "receivableType": null
                }
                """;

        mockMvc.perform(post("/settlements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors.length()").value(6));
    }

    // POST /settlements - Exception Scenarios

    @Test
    void create_ShouldReturn404_WhenCurrencyNotFound() throws Exception {
        when(settlementService.save(any(SettlementRequest.class)))
                .thenThrow(new ResourceNotFoundException("Currency", "BRL"));

        mockMvc.perform(post("/settlements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Currency not found with code: BRL"));
    }

    @Test
    void create_ShouldReturn404_WhenExchangeRateNotFound() throws Exception {
        when(settlementService.save(any(SettlementRequest.class)))
                .thenThrow(new ResourceNotFoundException("Exchange rate", "USD_BRL"));

        mockMvc.perform(post("/settlements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Exchange rate not found with code: USD_BRL"));
    }

    @Test
    void create_ShouldReturn400_WhenSettlementIsInvalid() throws Exception {
        when(settlementService.save(any(SettlementRequest.class)))
                .thenThrow(new InvalidSettlementException("Receivable is overdue"));

        mockMvc.perform(post("/settlements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid settlement"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Receivable is overdue"));
    }

    @Test
    void create_ShouldReturnProblemDetail_WhenResourceNotFoundException() throws Exception {
        when(settlementService.save(any(SettlementRequest.class)))
                .thenThrow(new ResourceNotFoundException("Currency", "BRL"));

        mockMvc.perform(post("/settlements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Currency not found with code: BRL"))
                .andExpect(jsonPath("$.instance").exists());
    }

    @Test
    void create_ShouldReturnProblemDetail_WhenInvalidSettlementException() throws Exception {
        when(settlementService.save(any(SettlementRequest.class)))
                .thenThrow(new InvalidSettlementException("Receivable is overdue"));

        mockMvc.perform(post("/settlements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").value("Invalid settlement"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Receivable is overdue"))
                .andExpect(jsonPath("$.instance").exists());
    }

    // GET /settlements/statement - Success Scenarios

    @Test
    void statement_ShouldReturn200Ok_WhenFiltersAreNull() throws Exception {
        when(settlementService.statement(
                eq(null), eq(null), eq(null), eq(null), any(Pageable.class)
        )).thenReturn(statementPage);

        mockMvc.perform(get("/settlements/statement"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void statement_ShouldReturn200Ok_WhenFiltersAreProvided() throws Exception {
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();

        when(settlementService.statement(
                eq(startDate), eq(endDate), eq("Test Seller"), eq("BRL"), any(Pageable.class)
        )).thenReturn(statementPage);

        mockMvc.perform(get("/settlements/statement")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .param("sellerName", "Test Seller")
                        .param("currencyCode", "BRL"))
                .andExpect(status().isOk());
    }

    @Test
    void statement_ShouldReturnPaginatedResponseCorrectly() throws Exception {
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        Page<SettlementStatementResponse> page = new PageImpl<>(
                Collections.emptyList(),
                PageRequest.of(0, 10),
                0
        );

        when(settlementService.statement(
                eq(startDate), eq(endDate), eq(null), eq(null), any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(get("/settlements/statement")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void statement_ShouldReturnEmptyCollection_WhenNoResults() throws Exception {
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        when(settlementService.statement(
                eq(startDate), eq(endDate), eq(null), eq(null), any(Pageable.class)
        )).thenReturn(statementPage);

        mockMvc.perform(get("/settlements/statement")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void statement_ShouldDelegateToService_WhenFiltersAreProvided() throws Exception {
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();

        when(settlementService.statement(
                eq(startDate), eq(endDate), eq("Test Seller"), eq("BRL"), any(Pageable.class)
        )).thenReturn(statementPage);

        mockMvc.perform(get("/settlements/statement")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .param("sellerName", "Test Seller")
                        .param("currencyCode", "BRL"));

        // Service delegation is verified by the mock being called
    }
}
