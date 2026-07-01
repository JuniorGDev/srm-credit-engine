package br.com.creditengine.repositories;

import br.com.creditengine.SettlementStatementProjection;
import br.com.creditengine.entities.Settlement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    @Query(value = """
            SELECT
                s.id                       AS id,
                r.seller_name              AS sellerName,
                r.receivable_type          AS receivableType,
                r.face_value               AS faceValue,
                rc.code                    AS receivableCurrency,
                pc.code                    AS paymentCurrency,
                s.exchange_rate_value      AS exchangeRate,
                s.present_value            AS presentValue,
                s.discount_value           AS discountValue,
                s.settled_amount           AS paymentAmount,
                r.due_date                 AS dueDate,
                s.created_at               AS createdAt
            FROM settlement s
            INNER JOIN receivable r
                ON r.id = s.receivable_id
            INNER JOIN currency rc
                ON rc.id = r.currency_id
            INNER JOIN currency pc
                ON pc.id = s.payment_currency_id
            WHERE
                (:sellerName IS NULL
                    OR LOWER(r.seller_name) LIKE LOWER(CONCAT('%', :sellerName, '%')))
            AND
                (:currencyCode IS NULL
                    OR rc.code = :currencyCode
                    OR pc.code = :currencyCode)
            AND
                (:startDate IS NULL
                    OR s.created_at >= :startDate)
            AND
                (:endDate IS NULL
                    OR s.created_at <= :endDate)
            ORDER BY s.created_at DESC
            """,
            nativeQuery = true)
    Page<SettlementStatementProjection> findSettlementStatement(
            @Param("sellerName") String sellerName,
            @Param("currencyCode") String currencyCode,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
}
