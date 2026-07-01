package br.com.creditengine.dtos.request;

import br.com.creditengine.enums.ReceivableType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Receivable request")
public record SettlementRequest(

        @Schema(
                description = "Name of the seller",
                example = "John Doe"
        )
        @NotBlank(message = "Seller name is required")
        String sellerName,
        @Schema(
                description = "Face value of the receivable",
                example = "1000.00"
        )
        @NotNull(message = "Face value is required")
        @Positive
        BigDecimal faceValue,
        @Schema(
                description = "Due date of the receivable",
                example = "2023-01-01"
        )
        @NotNull(message = "Due date is required")
        LocalDate dueDate,
        @Schema(
                description = "Currency code of the receivable",
                example = "BRL"
        )
        @NotBlank(message = "Currency code is required")
        @Size(min = 3, max = 3, message = "Currency code must contain exactly 3 characters")
        String currencyCode,
        @Schema(
                description = "Currency code of the payment",
                example = "BRL"
        )
        @NotBlank(message = "Payment currency code is required")
        @Size(min = 3, max = 3, message = "Payment currency code must contain exactly 3 characters")
        String paymentCurrencyCode,
        @Schema(
                description = "Type of the receivable",
                example = "DUPLICATA_MERCANTIL"
        )
        @NotNull(message = "Receivable type is required")
        ReceivableType receivableType
) {
}
