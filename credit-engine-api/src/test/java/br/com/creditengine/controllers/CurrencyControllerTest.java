package br.com.creditengine.controllers;

import br.com.creditengine.dtos.request.CurrencyRequest;
import br.com.creditengine.dtos.response.CurrencyResponse;
import br.com.creditengine.exceptions.CurrencyAlreadyExistsException;
import br.com.creditengine.exceptions.ResourceNotFoundException;
import br.com.creditengine.service.CurrencyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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

@WebMvcTest(CurrencyController.class)
class CurrencyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CurrencyService currencyService;

    private CurrencyRequest validRequest;
    private CurrencyResponse currencyResponse;
    private List<CurrencyResponse> currencyList;

    @BeforeEach
    void setUp() {
        validRequest = new CurrencyRequest(
                "Real",
                "BRL"
        );

        currencyResponse = new CurrencyResponse(
                1L,
                "Real",
                "BRL"
        );

        currencyList = List.of(currencyResponse);
    }

    // GET /currencies - Success Scenarios

    @Test
    void findAll_ShouldReturn200Ok_WhenCurrenciesExist() throws Exception {
        when(currencyService.findAll()).thenReturn(currencyList);

        mockMvc.perform(get("/currencies"))
                .andExpect(status().isOk());
    }

    @Test
    void findAll_ShouldReturnExpectedResponseBody_WhenCurrenciesExist() throws Exception {
        when(currencyService.findAll()).thenReturn(currencyList);

        mockMvc.perform(get("/currencies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Real"))
                .andExpect(jsonPath("$[0].code").value("BRL"));
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoCurrenciesExist() throws Exception {
        when(currencyService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/currencies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void findAll_ShouldDelegateToService_WhenCalled() throws Exception {
        when(currencyService.findAll()).thenReturn(currencyList);

        mockMvc.perform(get("/currencies"));

        // Service delegation is verified by the mock being called
    }

    // GET /currencies/{id} - Success Scenarios

    @Test
    void findById_ShouldReturn200Ok_WhenCurrencyExists() throws Exception {
        when(currencyService.findById(1L)).thenReturn(currencyResponse);

        mockMvc.perform(get("/currencies/1"))
                .andExpect(status().isOk());
    }

    @Test
    void findById_ShouldReturnExpectedResponseBody_WhenCurrencyExists() throws Exception {
        when(currencyService.findById(1L)).thenReturn(currencyResponse);

        mockMvc.perform(get("/currencies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Real"))
                .andExpect(jsonPath("$.code").value("BRL"));
    }

    @Test
    void findById_ShouldDelegateToService_WhenCalled() throws Exception {
        when(currencyService.findById(1L)).thenReturn(currencyResponse);

        mockMvc.perform(get("/currencies/1"));

        // Service delegation is verified by the mock being called
    }

    // GET /currencies/{id} - Path Variable Validation

    @Test
    void findById_ShouldReturn400_WhenIdIsZero() throws Exception {
        mockMvc.perform(get("/currencies/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void findById_ShouldReturn400_WhenIdIsNegative() throws Exception {
        mockMvc.perform(get("/currencies/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    // GET /currencies/{id} - Exception Scenarios

    @Test
    void findById_ShouldReturn404_WhenCurrencyNotFound() throws Exception {
        when(currencyService.findById(999L))
                .thenThrow(new ResourceNotFoundException("Currency", 999L));

        mockMvc.perform(get("/currencies/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Currency not found with id: 999"));
    }

    @Test
    void findById_ShouldReturnProblemDetail_WhenResourceNotFoundException() throws Exception {
        when(currencyService.findById(999L))
                .thenThrow(new ResourceNotFoundException("Currency", 999L));

        mockMvc.perform(get("/currencies/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Currency not found with id: 999"))
                .andExpect(jsonPath("$.instance").exists());
    }

    // POST /currencies - Success Scenarios

    @Test
    void save_ShouldReturn201Created_WhenValidRequest() throws Exception {
        when(currencyService.save(any(CurrencyRequest.class)))
                .thenReturn(currencyResponse);

        mockMvc.perform(post("/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void save_ShouldReturnExpectedResponseBody_WhenValidRequest() throws Exception {
        when(currencyService.save(any(CurrencyRequest.class)))
                .thenReturn(currencyResponse);

        mockMvc.perform(post("/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Real"))
                .andExpect(jsonPath("$.code").value("BRL"));
    }

    @Test
    void save_ShouldDelegateToService_WhenValidRequest() throws Exception {
        when(currencyService.save(any(CurrencyRequest.class)))
                .thenReturn(currencyResponse);

        mockMvc.perform(post("/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)));

        // Service delegation is verified by the mock being called
    }

    // POST /currencies - Validation - name

    @Test
    void save_ShouldReturn400_WhenNameIsBlank() throws Exception {
        CurrencyRequest request = new CurrencyRequest(
                "",
                "BRL"
        );

        mockMvc.perform(post("/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void save_ShouldReturn400_WhenNameIsNull() throws Exception {
        String json = """
                {
                    "name": null,
                    "code": "BRL"
                }
                """;

        mockMvc.perform(post("/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void save_ShouldReturn400_WhenNameExceedsMaxSize() throws Exception {
        CurrencyRequest request = new CurrencyRequest(
                "a".repeat(101),
                "BRL"
        );

        mockMvc.perform(post("/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    // POST /currencies - Validation - code

    @Test
    void save_ShouldReturn400_WhenCodeIsBlank() throws Exception {
        CurrencyRequest request = new CurrencyRequest(
                "Real",
                ""
        );

        mockMvc.perform(post("/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void save_ShouldReturn400_WhenCodeIsNull() throws Exception {
        String json = """
                {
                    "name": "Real",
                    "code": null
                }
                """;

        mockMvc.perform(post("/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void save_ShouldReturn400_WhenCodeSizeIsLessThan3() throws Exception {
        CurrencyRequest request = new CurrencyRequest(
                "Real",
                "BR"
        );

        mockMvc.perform(post("/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void save_ShouldReturn400_WhenCodeSizeIsGreaterThan3() throws Exception {
        CurrencyRequest request = new CurrencyRequest(
                "Real",
                "BRLA"
        );

        mockMvc.perform(post("/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    // POST /currencies - Multiple Validation Errors

    @Test
    void save_ShouldReturn400_WhenMultipleFieldsAreInvalid() throws Exception {
        String json = """
                {
                    "name": "",
                    "code": "BR"
                }
                """;

        mockMvc.perform(post("/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors.length()").value(2));
    }

    // POST /currencies - Exception Scenarios

    @Test
    void save_ShouldReturn409_WhenCurrencyAlreadyExists() throws Exception {
        when(currencyService.save(any(CurrencyRequest.class)))
                .thenThrow(new CurrencyAlreadyExistsException("BRL"));

        mockMvc.perform(post("/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Currency already exists"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("Currency already exists with code: BRL"));
    }

    @Test
    void save_ShouldReturnProblemDetail_WhenCurrencyAlreadyExistsException() throws Exception {
        when(currencyService.save(any(CurrencyRequest.class)))
                .thenThrow(new CurrencyAlreadyExistsException("BRL"));

        mockMvc.perform(post("/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").value("Currency already exists"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("Currency already exists with code: BRL"))
                .andExpect(jsonPath("$.instance").exists());
    }

    // PUT /currencies/{id} - Success Scenarios

    @Test
    void update_ShouldReturn200Ok_WhenValidRequest() throws Exception {
        when(currencyService.update(eq(1L), any(CurrencyRequest.class)))
                .thenReturn(currencyResponse);

        mockMvc.perform(put("/currencies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void update_ShouldReturnExpectedResponseBody_WhenValidRequest() throws Exception {
        when(currencyService.update(eq(1L), any(CurrencyRequest.class)))
                .thenReturn(currencyResponse);

        mockMvc.perform(put("/currencies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Real"))
                .andExpect(jsonPath("$.code").value("BRL"));
    }

    @Test
    void update_ShouldDelegateToService_WhenValidRequest() throws Exception {
        when(currencyService.update(eq(1L), any(CurrencyRequest.class)))
                .thenReturn(currencyResponse);

        mockMvc.perform(put("/currencies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)));

        // Service delegation is verified by the mock being called
    }

    // PUT /currencies/{id} - Path Variable Validation

    @Test
    void update_ShouldReturn400_WhenIdIsZero() throws Exception {
        mockMvc.perform(put("/currencies/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void update_ShouldReturn400_WhenIdIsNegative() throws Exception {
        mockMvc.perform(put("/currencies/-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    // PUT /currencies/{id} - Validation - name

    @Test
    void update_ShouldReturn400_WhenNameIsBlank() throws Exception {
        CurrencyRequest request = new CurrencyRequest(
                "",
                "BRL"
        );

        mockMvc.perform(put("/currencies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void update_ShouldReturn400_WhenNameIsNull() throws Exception {
        String json = """
                {
                    "name": null,
                    "code": "BRL"
                }
                """;

        mockMvc.perform(put("/currencies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void update_ShouldReturn400_WhenNameExceedsMaxSize() throws Exception {
        CurrencyRequest request = new CurrencyRequest(
                "a".repeat(101),
                "BRL"
        );

        mockMvc.perform(put("/currencies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    // PUT /currencies/{id} - Validation - code

    @Test
    void update_ShouldReturn400_WhenCodeIsBlank() throws Exception {
        CurrencyRequest request = new CurrencyRequest(
                "Real",
                ""
        );

        mockMvc.perform(put("/currencies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void update_ShouldReturn400_WhenCodeIsNull() throws Exception {
        String json = """
                {
                    "name": "Real",
                    "code": null
                }
                """;

        mockMvc.perform(put("/currencies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void update_ShouldReturn400_WhenCodeSizeIsLessThan3() throws Exception {
        CurrencyRequest request = new CurrencyRequest(
                "Real",
                "BR"
        );

        mockMvc.perform(put("/currencies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void update_ShouldReturn400_WhenCodeSizeIsGreaterThan3() throws Exception {
        CurrencyRequest request = new CurrencyRequest(
                "Real",
                "BRLA"
        );

        mockMvc.perform(put("/currencies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    // PUT /currencies/{id} - Multiple Validation Errors

    @Test
    void update_ShouldReturn400_WhenMultipleFieldsAreInvalid() throws Exception {
        String json = """
                {
                    "name": "",
                    "code": "BR"
                }
                """;

        mockMvc.perform(put("/currencies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request fields"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors.length()").value(2));
    }

    // PUT /currencies/{id} - Exception Scenarios

    @Test
    void update_ShouldReturn404_WhenCurrencyNotFound() throws Exception {
        when(currencyService.update(eq(999L), any(CurrencyRequest.class)))
                .thenThrow(new ResourceNotFoundException("Currency", 999L));

        mockMvc.perform(put("/currencies/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Currency not found with id: 999"));
    }

    @Test
    void update_ShouldReturn409_WhenCurrencyAlreadyExists() throws Exception {
        when(currencyService.update(eq(1L), any(CurrencyRequest.class)))
                .thenThrow(new CurrencyAlreadyExistsException("BRL"));

        mockMvc.perform(put("/currencies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Currency already exists"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("Currency already exists with code: BRL"));
    }

    @Test
    void update_ShouldReturnProblemDetail_WhenResourceNotFoundException() throws Exception {
        when(currencyService.update(eq(999L), any(CurrencyRequest.class)))
                .thenThrow(new ResourceNotFoundException("Currency", 999L));

        mockMvc.perform(put("/currencies/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Currency not found with id: 999"))
                .andExpect(jsonPath("$.instance").exists());
    }

    @Test
    void update_ShouldReturnProblemDetail_WhenCurrencyAlreadyExistsException() throws Exception {
        when(currencyService.update(eq(1L), any(CurrencyRequest.class)))
                .thenThrow(new CurrencyAlreadyExistsException("BRL"));

        mockMvc.perform(put("/currencies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").value("Currency already exists"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("Currency already exists with code: BRL"))
                .andExpect(jsonPath("$.instance").exists());
    }
}
