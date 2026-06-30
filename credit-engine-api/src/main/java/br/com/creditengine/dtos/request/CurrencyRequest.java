package br.com.creditengine.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Currency request")
public record CurrencyRequest(

        @Schema(
                description = "Name of the currency",
                example = "Real"
        )
        @NotBlank(message = "Name is required")
        @Size(max = 100)
        String name,

        @Schema(
                description = "Code of the currency",
                example = "BRL"
        )
        @NotBlank(message = "Code is required")
        @Size(min = 3, max = 3)
        String code
) {
}
