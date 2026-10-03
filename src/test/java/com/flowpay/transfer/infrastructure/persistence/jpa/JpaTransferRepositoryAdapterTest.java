package com.flowpay.transfer.infrastructure.persistence.jpa;

import com.flowpay.transfer.application.port.TransferRepository;
import com.flowpay.transfer.domain.Transfer;
import com.flowpay.transfer.domain.TransferStatus;
import com.flowpay.wallet.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles({"jpa", "test"})
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@Import({
        JpaTransferRepositoryAdapter.class,
        TransferPersistenceMapper.class
})
class JpaTransferRepositoryAdapterTest {

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private SpringDataTransferRepository springDataRepository;

    @BeforeEach
    void setUp() {
        springDataRepository.deleteAll();
    }

    @Test
    void should_save_and_find_transfer_by_request_id() {
        Transfer transfer = new Transfer(
                "transfer-001",
                "request-001",
                "wallet-an",
                "wallet-binh",
                new Money(new BigDecimal("300000"))
        );

        transfer.markSucceeded();

        transferRepository.save(transfer);

        Transfer restored = transferRepository
                .findByRequestId("request-001")
                .orElseThrow();

        assertEquals("transfer-001", restored.getId());
        assertEquals("request-001", restored.getRequestId());
        assertEquals(
                "wallet-an",
                restored.getSenderWalletId()
        );
        assertEquals(
                "wallet-binh",
                restored.getReceiverWalletId()
        );

        assertEquals(
                0,
                new BigDecimal("300000").compareTo(
                        restored.getAmount().getAmount()
                )
        );

        assertEquals(
                TransferStatus.SUCCEEDED,
                restored.getStatus()
        );
    }

    @Test
    void should_return_empty_when_request_id_does_not_exist() {
        assertTrue(
                transferRepository
                        .findByRequestId("missing-request")
                        .isEmpty()
        );
    }
}