package br.com.creditengine.dtos.response;

import br.com.creditengine.projections.SettlementStatementProjection;
import br.com.creditengine.enums.ReceivableType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Settlement statement response")
public record SettlementStatementResponse(

        @Schema(
                description = "Settlement identifier",
                example = "1"
        )
        Long id,

        @Schema(
                description = "Name of the seller",
                example = "Global Export Inc."
        )
        String sellerName,

        @Schema(
                description = "Type of the receivable",
                example = "CHEQUE_PRE_DATADO"
        )
        ReceivableType receivableType,

        @Schema(
                description = "Face value of the receivable",
                example = "5000.00"
        )
        BigDecimal faceValue,

        @Schema(
                description = "Currency of the receivable",
                example = "USD"
        )
        String receivableCurrency,

        @Schema(
                description = "Currency used for settlement payment",
                example = "BRL"
        )
        String paymentCurrency,

        @Schema(
                description = "Exchange rate applied during settlement. Returns 1.00 when no currency conversion is required.",
                example = "5.1722"
        )
        BigDecimal exchangeRate,

        @Schema(
                description = "Present value calculated after applying the pricing formula",
                example = "4582.70"
        )
        BigDecimal presentValue,

        @Schema(
                description = "Discount amount applied to the receivable",
                example = "417.30"
        )
        BigDecimal discountValue,

        @Schema(
                description = "Final amount paid to the seller",
                example = "23702.64"
        )
        BigDecimal paymentAmount,

        @Schema(
                description = "Due date of the receivable",
                example = "2026-09-15"
        )
        LocalDate dueDate,

        @Schema(
                description = "Settlement creation date and time",
                example = "2026-07-01T03:11:07"
        )
        LocalDateTime createdAt

) {

    public static SettlementStatementResponse from(
            SettlementStatementProjection projection
    ) {

        return new SettlementStatementResponse(
                projection.getId(),
                projection.getSellerName(),
                ReceivableType.valueOf(projection.getReceivableType()),
                projection.getFaceValue(),
                projection.getReceivableCurrency(),
                projection.getPaymentCurrency(),
                projection.getExchangeRate(),
                projection.getPresentValue(),
                projection.getDiscountValue(),
                projection.getPaymentAmount(),
                projection.getDueDate(),
                projection.getCreatedAt()
        );
    }
}
