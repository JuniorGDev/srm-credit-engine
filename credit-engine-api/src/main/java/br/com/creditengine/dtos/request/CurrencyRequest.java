package br.com.creditengine.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CurrencyRequest(
        @NotBlank
        @Size(max = 100)
        String name,
        @NotBlank
        @Size(min = 3, max = 3)
        String code
) {
}
