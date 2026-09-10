package com.flowpay.infrastructure.persistence.inmemory;

import com.flowpay.transfer.domain.Transfer;
import com.flowpay.wallet.domain.Money;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Optional;

public class InMemoryTransferRepositoryTest {
    @Test
    void should_save_and_find_transfer_by_request_id() {
        InMemoryTransferRepository repository = new InMemoryTransferRepository();
        Transfer transfer = new Transfer("transfer-001", "request-001", "wallet-an", "wallet-binh", new Money(new BigDecimal("300000")));

        repository.save(transfer);

        Optional<Transfer> result = repository.findByRequestId("request-001");

        assertTrue(result.isPresent());
        assertSame(transfer, result.get());
    }
}
