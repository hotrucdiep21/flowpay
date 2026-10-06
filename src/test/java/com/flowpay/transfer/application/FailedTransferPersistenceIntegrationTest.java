package com.flowpay.transfer.application;

import com.flowpay.support.PostgresTestContainerConfig;
import com.flowpay.transfer.domain.TransferStatus;
import com.flowpay.transfer.infrastructure.persistence.jpa.SpringDataTransferRepository;
import com.flowpay.wallet.application.port.WalletRepository;
import com.flowpay.wallet.domain.Money;
import com.flowpay.wallet.domain.Wallet;
import com.flowpay.wallet.infrastructure.persistence.jpa.SpringDataWalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles({"jpa", "test"})
@Import(PostgresTestContainerConfig.class)
public class FailedTransferPersistenceIntegrationTest {
    @Autowired
    private TransferService transferService;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private SpringDataTransferRepository springDataTransferRepository;
    @Autowired
    private SpringDataWalletRepository springDataWalletRepository;

    @BeforeEach
    void setUp() {
        springDataTransferRepository.deleteAll();
        springDataWalletRepository.deleteAll();
    }

    @Test
    void should_persist_failed_transfer_when_balance_is_insufficient() {
        Wallet sender = new Wallet(
                "failed-sender",
                "user-an",
                new Money(new BigDecimal("50000"))
        );

        Wallet receiver = new Wallet(
                "failed-receiver",
                "user-binh",
                new Money(new BigDecimal("50000"))
        );

        walletRepository.save(sender);
        walletRepository.save(receiver);

        TransferCommand command = new TransferCommand(
                "request-failed-001",
                sender.getId(),
                receiver.getId(),
                new Money(new BigDecimal("100000"))
        );

        assertThrows(
                IllegalStateException.class,
                () -> transferService.transfer(command)
        );

        var savedTransfer = springDataTransferRepository
                .findByRequestId("request-failed-001")
                .orElseThrow();

        assertEquals(
                TransferStatus.FAILED,
                savedTransfer.getStatus()
        );

        Wallet senderAfter = walletRepository
                .findById(sender.getId())
                .orElseThrow();

        Wallet receiverAfter = walletRepository
                .findById(receiver.getId())
                .orElseThrow();

        assertEquals(
                0,
                new BigDecimal("50000").compareTo(
                        senderAfter.getBalance().getAmount()
                )
        );

        assertEquals(
                0,
                new BigDecimal("50000").compareTo(
                        receiverAfter.getBalance().getAmount()
                )
        );
    }
}
