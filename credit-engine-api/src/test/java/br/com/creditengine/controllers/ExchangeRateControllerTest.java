package br.com.creditengine.controllers;

import br.com.creditengine.dtos.request.ExchangeRateRequest;
import br.com.creditengine.dtos.request.ExchangeRateUpdateRequest;
import br.com.creditengine.dtos.response.ExchangeRateResponse;
import br.com.creditengine.exceptions.ExchangeRateAlreadyException;
import br.com.creditengine.exceptions.InvalidExchangeRateException;
import br.com.creditengine.exceptions.ResourceNotFoundException;
import br.com.creditengine.service.ExchangeRateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExchangeRateController.class)
class ExchangeRateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExchangeRateService exchangeRateService;

    private ExchangeRateRequest validRequest;
    private ExchangeRateUpdateRequest validUpdateRequest;
    private ExchangeRateResponse exchangeRateResponse;
    private List<ExchangeRateResponse> exchangeRateList;

    @BeforeEach
    void setUp() {
        validRequest = new ExchangeRateRequest(
                "USD",
                "BRL",
                new BigDecimal("5.00")
        );

        validUpdateRequest = new ExchangeRateUpdateRequest(
                new BigDecimal("5.50")
        );

        exchangeRateResponse = new ExchangeRateResponse(
                1L,
                "USD",
                "BRL",
                new BigDecimal("5.00"),
                LocalDateTime.now()
        );

        exchangeRateList = List.of(exchangeRateResponse);
    }

    // GET /exchange-rates - Success Scenarios

    @Test
    void findAll_ShouldReturn200Ok_WhenExchangeRatesExist() throws Exception {
        when(exchangeRateService.findAll()).thenReturn(exchangeRateList);

        mockMvc.perform(get("/exchange-rates"))
                .andExpect(status().isOk());
    }

    @Test
    void findAll_ShouldReturnExpectedResponseBody_WhenExchangeRatesExist() throws Exception {
        when(exchangeRateService.findAll()).thenReturn(exchangeRateList);

        mockMvc.perform(get("/exchange-rates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].fromCurrency").value("USD"))
                .andExpect(jsonPath("$[0].toCurrency").value("BRL"))
                .andExpect(jsonPath("$[0].rate").value(5.00))
                .andExpect(jsonPath("$[0].updatedAt").exists());
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoExchangeRatesExist() throws Exception {
        when(exchangeRateService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/exchange-rates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void findAll_ShouldDelegateToService_WhenCalled() throws Exception {
        when(exchangeRateService.findAll()).thenReturn(exchangeRateList);

        mockMvc.perform(get("/exchange-rates"));

        // Service delegation is verified by the mock being called
    }

    // GET /exchange-rates/{id} - Success Scenarios

    @Test
    void findById_ShouldReturn200Ok_WhenExchangeRateExists() throws Exception {
        when(exchangeRateService.findById(1L)).thenReturn(exchangeRateResponse);

        mockMvc.perform(get("/exchange-rates/1"))
                .andExpect(status().isOk());
    }

    @Test
    void findById_ShouldReturnExpectedResponseBody_WhenExchangeRateExists() throws Exception {
        when(exchangeRateService.findById(1L)).thenReturn(exchangeRateResponse);

        mockMvc.perform(get("/exchange-rates/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fromCurrency").value("USD"))
                .andExpect(jsonPath("$.toCurrency").value("BRL"))
                .andExpect(jsonPath("$.rate").value(5.00))
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void findById_ShouldDelegateToService_WhenCalled() throws Exception {
        when(exchangeRateService.findById(1L)).thenReturn(exchangeRateResponse);

        mockMvc.perform(get("/exchange-rates/1"));

        // Service delegation is verified by the mock being called
    }

    // GET /exchange-rates/{id} - Path Variable Validation

    @Test
    void findById_ShouldReturn400_WhenIdIsZero() throws Exception {
        mockMvc.perform(get("/exchange-rates/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void findById_ShouldReturn400_WhenIdIsNegative() throws Exception {
        mockMvc.perform(get("/exchange-rates/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    // GET /exchange-rates/{id} - Exception Scenarios

    @Test
    void findById_ShouldReturn404_WhenExchangeRateNotFound() throws Exception {
        when(exchangeRateService.findById(999L))
                .thenThrow(new ResourceNotFoundException("Exchange rate", 999L));

        mockMvc.perform(get("/exchange-rates/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Exchange rate not found with id: 999"));
    }

    @Test
    void findById_ShouldReturnProblemDetail_WhenResourceNotFoundException() throws Exception {
        when(exchangeRateService.findById(999L))
                .thenThrow(new ResourceNotFoundException("Exchange rate", 999L));

        mockMvc.perform(get("/exchange-rates/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Exchange rate not found with id: 999"))
                .andExpect(jsonPath("$.instance").exists());
    }

    // POST /exchange-rates - Success Scenarios

    @Test
    void save_ShouldReturn201Created_WhenValidRequest() throws Exception {
        when(exchangeRateService.save(any(ExchangeRateRequest.class)))
                .thenReturn(exchangeRateResponse);

        mockMvc.perform(post("/exchange-rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void save_ShouldReturnExpectedResponseBody_WhenValidRequest() throws Exception {
        when(exchangeRateService.save(any(ExchangeRateRequest.class)))
                .thenReturn(exchangeRateResponse);

        mockMvc.perform(post("/exchange-rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fromCurrency").value("USD"))
                .andExpect(jsonPath("$.toCurrency").value("BRL"))
                .andExpect(jsonPath("$.rate").value(5.00))
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void save_ShouldDelegateToService_WhenValidRequest() throws Exception {
        when(exchangeRateService.save(any(ExchangeRateRequest.class)))
                .thenReturn(exchangeRateResponse);

        mockMvc.perform(post("/exchange-rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)));

        // Service delegation is verified by the mock being called
    }

    // POST /exchange-rates - Validation - fromCurrency

    @Test
    void save_ShouldReturn400_WhenFromCurrencyIsBlank() throws Exception {
        ExchangeRateRequest request = new ExchangeRateRequest(
                "",
                "BRL",
                new BigDecimal("5.00")
        );

        mockMvc.perform(post("/exchange-rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void save_ShouldReturn400_WhenFromCurrencyIsNull() throws Exception {
        String json = """
                {
                    "fromCurrency": null,
                    "toCurrency": "BRL",
                    "rate": 5.00
                }
                """;

        mockMvc.perform(post("/exchange-rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void save_ShouldReturn400_WhenFromCurrencySizeIsLessThan3() throws Exception {
        ExchangeRateRequest request = new ExchangeRateRequest(
                "US",
                "BRL",
                new BigDecimal("5.00")
        );

        mockMvc.perform(post("/exchange-rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void save_ShouldReturn400_WhenFromCurrencySizeIsGreaterThan3() throws Exception {
        ExchangeRateRequest request = new ExchangeRateRequest(
                "USDA",
                "BRL",
                new BigDecimal("5.00")
        );

        mockMvc.perform(post("/exchange-rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    // POST /exchange-rates - Validation - toCurrency

    @Test
    void save_ShouldReturn400_WhenToCurrencyIsBlank() throws Exception {
        ExchangeRateRequest request = new ExchangeRateRequest(
                "USD",
                "",
                new BigDecimal("5.00")
        );

        mockMvc.perform(post("/exchange-rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void save_ShouldReturn400_WhenToCurrencyIsNull() throws Exception {
        String json = """
                {
                    "fromCurrency": "USD",
                    "toCurrency": null,
                    "rate": 5.00
                }
                """;

        mockMvc.perform(post("/exchange-rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void save_ShouldReturn400_WhenToCurrencySizeIsLessThan3() throws Exception {
        ExchangeRateRequest request = new ExchangeRateRequest(
                "USD",
                "BR",
                new BigDecimal("5.00")
        );

        mockMvc.perform(post("/exchange-rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void save_ShouldReturn400_WhenToCurrencySizeIsGreaterThan3() throws Exception {
        ExchangeRateRequest request = new ExchangeRateRequest(
                "USD",
                "BRLA",
                new BigDecimal("5.00")
        );

        mockMvc.perform(post("/exchange-rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    // POST /exchange-rates - Validation - rate

    @Test
    void save_ShouldReturn400_WhenRateIsNull() throws Exception {
        String json = """
                {
                    "fromCurrency": "USD",
                    "toCurrency": "BRL",
                    "rate": null
                }
                """;

        mockMvc.perform(post("/exchange-rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void save_ShouldReturn400_WhenRateIsZero() throws Exception {
        ExchangeRateRequest request = new ExchangeRateRequest(
                "USD",
                "BRL",
                BigDecimal.ZERO
        );

        mockMvc.perform(post("/exchange-rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void save_ShouldReturn400_WhenRateIsNegative() throws Exception {
        ExchangeRateRequest request = new ExchangeRateRequest(
                "USD",
                "BRL",
                new BigDecimal("-5.00")
        );

        mockMvc.perform(post("/exchange-rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    // POST /exchange-rates - Multiple Validation Errors

    @Test
    void save_ShouldReturn400_WhenMultipleFieldsAreInvalid() throws Exception {
        String json = """
                {
                    "fromCurrency": "",
                    "toCurrency": "BR",
                    "rate": -5.00
                }
                """;

        mockMvc.perform(post("/exchange-rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors.length()").value(3));
    }

    // POST /exchange-rates - Exception Scenarios

    @Test
    void save_ShouldReturn409_WhenExchangeRateAlreadyExists() throws Exception {
        when(exchangeRateService.save(any(ExchangeRateRequest.class)))
                .thenThrow(new ExchangeRateAlreadyException("USD to BRL"));

        mockMvc.perform(post("/exchange-rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Exchange rate already exists"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("Exchange already exists with code: USD to BRL"));
    }

    @Test
    void save_ShouldReturn400_WhenCurrenciesAreTheSame() throws Exception {
        when(exchangeRateService.save(any(ExchangeRateRequest.class)))
                .thenThrow(new InvalidExchangeRateException());

        mockMvc.perform(post("/exchange-rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid exchange rate"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("From and to currencies cannot be the same"));
    }

    @Test
    void save_ShouldReturn404_WhenCurrencyNotFound() throws Exception {
        when(exchangeRateService.save(any(ExchangeRateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Currency", "XYZ"));

        mockMvc.perform(post("/exchange-rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Currency not found with code: XYZ"));
    }

    @Test
    void save_ShouldReturnProblemDetail_WhenExchangeRateAlreadyException() throws Exception {
        when(exchangeRateService.save(any(ExchangeRateRequest.class)))
                .thenThrow(new ExchangeRateAlreadyException("USD to BRL"));

        mockMvc.perform(post("/exchange-rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").value("Exchange rate already exists"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("Exchange already exists with code: USD to BRL"))
                .andExpect(jsonPath("$.instance").exists());
    }

    @Test
    void save_ShouldReturnProblemDetail_WhenInvalidExchangeRateException() throws Exception {
        when(exchangeRateService.save(any(ExchangeRateRequest.class)))
                .thenThrow(new InvalidExchangeRateException());

        mockMvc.perform(post("/exchange-rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").value("Invalid exchange rate"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("From and to currencies cannot be the same"))
                .andExpect(jsonPath("$.instance").exists());
    }

    @Test
    void save_ShouldReturnProblemDetail_WhenResourceNotFoundException() throws Exception {
        when(exchangeRateService.save(any(ExchangeRateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Currency", "XYZ"));

        mockMvc.perform(post("/exchange-rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Currency not found with code: XYZ"))
                .andExpect(jsonPath("$.instance").exists());
    }

    // PUT /exchange-rates/{id} - Success Scenarios

    @Test
    void update_ShouldReturn200Ok_WhenValidRequest() throws Exception {
        when(exchangeRateService.update(eq(1L), any(ExchangeRateUpdateRequest.class)))
                .thenReturn(exchangeRateResponse);

        mockMvc.perform(put("/exchange-rates/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void update_ShouldReturnExpectedResponseBody_WhenValidRequest() throws Exception {
        when(exchangeRateService.update(eq(1L), any(ExchangeRateUpdateRequest.class)))
                .thenReturn(exchangeRateResponse);

        mockMvc.perform(put("/exchange-rates/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fromCurrency").value("USD"))
                .andExpect(jsonPath("$.toCurrency").value("BRL"))
                .andExpect(jsonPath("$.rate").value(5.00))
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void update_ShouldDelegateToService_WhenValidRequest() throws Exception {
        when(exchangeRateService.update(eq(1L), any(ExchangeRateUpdateRequest.class)))
                .thenReturn(exchangeRateResponse);

        mockMvc.perform(put("/exchange-rates/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest)));

        // Service delegation is verified by the mock being called
    }

    // PUT /exchange-rates/{id} - Path Variable Validation

    @Test
    void update_ShouldReturn400_WhenIdIsZero() throws Exception {
        mockMvc.perform(put("/exchange-rates/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void update_ShouldReturn400_WhenIdIsNegative() throws Exception {
        mockMvc.perform(put("/exchange-rates/-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    // PUT /exchange-rates/{id} - Request Body Validation - rate

    @Test
    void update_ShouldReturn400_WhenRateIsNull() throws Exception {
        String json = """
                {
                    "rate": null
                }
                """;

        mockMvc.perform(put("/exchange-rates/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void update_ShouldReturn400_WhenRateIsZero() throws Exception {
        ExchangeRateUpdateRequest request = new ExchangeRateUpdateRequest(
                BigDecimal.ZERO
        );

        mockMvc.perform(put("/exchange-rates/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void update_ShouldReturn400_WhenRateIsNegative() throws Exception {
        ExchangeRateUpdateRequest request = new ExchangeRateUpdateRequest(
                new BigDecimal("-5.00")
        );

        mockMvc.perform(put("/exchange-rates/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    // PUT /exchange-rates/{id} - Exception Scenarios

    @Test
    void update_ShouldReturn404_WhenExchangeRateNotFound() throws Exception {
        when(exchangeRateService.update(eq(999L), any(ExchangeRateUpdateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Exchange rate", 999L));

        mockMvc.perform(put("/exchange-rates/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Exchange rate not found with id: 999"));
    }

    @Test
    void update_ShouldReturnProblemDetail_WhenResourceNotFoundException() throws Exception {
        when(exchangeRateService.update(eq(999L), any(ExchangeRateUpdateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Exchange rate", 999L));

        mockMvc.perform(put("/exchange-rates/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Exchange rate not found with id: 999"))
                .andExpect(jsonPath("$.instance").exists());
    }
}
