package com.flowpay.transfer.application;

import com.flowpay.support.PostgresTestContainerConfig;
import com.flowpay.transfer.application.port.TransferRepository;
import com.flowpay.transfer.domain.Transfer;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles({"jpa", "test"})
@Import(PostgresTestContainerConfig.class)
public class TransferTransactionIntegrationTest {
    @Autowired
    private TransferService transferService;

    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private SpringDataTransferRepository springDataTransferRepository;
    @Autowired
    private SpringDataWalletRepository springDataWalletRepository;
    @MockitoBean
    private TransferRepository transferRepository;

    @BeforeEach
    void setUp() {
        springDataTransferRepository.deleteAll();
        springDataWalletRepository.deleteAll();
    }

    @Test
    void should_rollback_wallet_changes_when_transfer_persistence_fails() {
        Wallet sender = new Wallet(
                "wallet-transaction-sender",
                "user-an",
                new Money(new BigDecimal("1000000"))
        );

        Wallet receiver = new Wallet(
                "wallet-transaction-receiver",
                "user-binh",
                new Money(new BigDecimal("200000"))
        );

        walletRepository.save(sender);
        walletRepository.save(receiver);

        TransferCommand command = new TransferCommand(
                "request-transaction-001",
                sender.getId(),
                receiver.getId(),
                new Money(new BigDecimal("300000"))
        );

        when(
                transferRepository.findByRequestId(
                        command.requestId()
                )
        ).thenReturn(Optional.empty());

        doThrow(
                new RuntimeException(
                        "Simulated transfer persistence failure"
                )
        ).when(transferRepository).save(any(Transfer.class));

        assertThrows(
                RuntimeException.class,
                () -> transferService.transfer(command)
        );

        Wallet senderAfter = walletRepository
                .findById(sender.getId())
                .orElseThrow();

        Wallet receiverAfter = walletRepository
                .findById(receiver.getId())
                .orElseThrow();

        assertMoneyEquals(
                "1000000",
                senderAfter.getBalance()
        );

        assertMoneyEquals(
                "200000",
                receiverAfter.getBalance()
        );
    }

    private void assertMoneyEquals(
            String expected,
            Money actual
    ) {
        assertEquals(
                0,
                new BigDecimal(expected)
                        .compareTo(actual.getAmount())
        );
    }
}
