package com.flowpay.transfer.infrastructure.persistence.jpa;

import com.flowpay.transfer.domain.Transfer;
import com.flowpay.transfer.domain.TransferStatus;
import com.flowpay.wallet.domain.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransferPersistenceMapperTest {
    private final TransferPersistenceMapper mapper =
            new TransferPersistenceMapper();

    @Test
    void should_map_domain_transfer_to_jpa_entity() {
        Transfer transfer = new Transfer(
                "transfer-001",
                "request-001",
                "wallet-an",
                "wallet-binh",
                new Money(new BigDecimal("300000"))
        );

        transfer.markSucceeded();

        TransferJpaEntity entity = mapper.toEntity(transfer);

        assertEquals("transfer-001", entity.getId());
        assertEquals("request-001", entity.getRequestId());
        assertEquals(
                "wallet-an",
                entity.getSenderWalletId()
        );
        assertEquals(
                "wallet-binh",
                entity.getReceiverWalletId()
        );
        assertEquals(
                new BigDecimal("300000"),
                entity.getAmount()
        );
        assertEquals(
                TransferStatus.SUCCEEDED,
                entity.getStatus()
        );
    }

    @Test
    void should_map_jpa_entity_to_domain_transfer() {
        TransferJpaEntity entity = new TransferJpaEntity(
                "transfer-001",
                "request-001",
                "wallet-an",
                "wallet-binh",
                new BigDecimal("300000"),
                TransferStatus.FAILED
        );

        Transfer transfer = mapper.toDomain(entity);

        assertEquals("transfer-001", transfer.getId());
        assertEquals("request-001", transfer.getRequestId());
        assertEquals(
                new BigDecimal("300000"),
                transfer.getAmount().getAmount()
        );
        assertEquals(
                TransferStatus.FAILED,
                transfer.getStatus()
        );
    }
}
