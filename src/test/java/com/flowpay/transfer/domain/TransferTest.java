package com.flowpay.transfer.domain;

import com.flowpay.wallet.domain.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TransferTest {
    @Test
    void should_create_pending_transfer() {
        Money amount = new Money(new BigDecimal("300000"));

        Transfer transfer = new Transfer(
                "transfer-001",
                "request-001",
                "wallet-an",
                "wallet-binh",
                amount
        );

        assertEquals("transfer-001", transfer.getId());
        assertEquals("request-001", transfer.getRequestId());
        assertEquals("wallet-an", transfer.getSenderWalletId());
        assertEquals("wallet-binh", transfer.getReceiverWalletId());

        assertEquals(new BigDecimal("300000"), transfer.getAmount().getAmount());

        assertEquals(TransferStatus.PENDING, transfer.getStatus());
    }

    @Test
    void should_reject_transfer_to_the_same_wallet() {
        Money amount = new Money(new BigDecimal("100000"));
        assertThrows(IllegalArgumentException.class, () -> new Transfer("transder-id", "request-id", "transfer-001", "transfer-001", amount));
    }

    private Transfer createTransfer() {
        return new Transfer(
                "transfer-001",
                "request-001",
                "wallet-an",
                "wallet-binh",
                new Money(new BigDecimal("300000"))
        );
    }

    @Test
    void should_marked_pending_transfer_succeeded() {
        Transfer transfer = createTransfer();
        transfer.markSucceeded();

        assertEquals(TransferStatus.SUCCESS, transfer.getStatus());
    }

    @Test
    void should_marked_pending_transfer_as_failed() {
        Transfer transfer = createTransfer();
        transfer.markFailed();

        assertEquals(TransferStatus.FAILED, transfer.getStatus());
    }

    @Test
    void should_reject_status_change_after_transfer_succeeded() {
        Transfer transfer = createTransfer();

        transfer.markSucceeded();

        assertThrows(IllegalStateException.class, transfer::markFailed);
        assertEquals(TransferStatus.SUCCESS, transfer.getStatus());
    }

}
