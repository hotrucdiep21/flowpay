package com.flowpay.transfer.application;

import com.flowpay.infrastructure.persistence.inmemory.InMemoryTransferRepository;
import com.flowpay.infrastructure.persistence.inmemory.InMemoryWalletRepository;
import com.flowpay.transfer.domain.Transfer;
import com.flowpay.transfer.domain.TransferStatus;
import com.flowpay.transfer.domain.exception.DuplicateTransferException;
import com.flowpay.wallet.domain.Money;
import com.flowpay.wallet.domain.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class TransferServiceTest {
    private InMemoryWalletRepository walletRepository;
    private InMemoryTransferRepository transferRepository;
    private TransferService transferService;

    @BeforeEach
    void setUp() {
        walletRepository = new InMemoryWalletRepository();
        transferRepository = new InMemoryTransferRepository();

        transferService = new TransferService(
                walletRepository,
                transferRepository
        );
    }

    @Test
    void should_transfer_money_between_two_wallets() {
        Wallet sender = new Wallet(
                "wallet-an",
                "user-an",
                new Money(new BigDecimal("1000000"))
        );

        Wallet receiver = new Wallet(
                "wallet-binh",
                "user-binh",
                new Money(new BigDecimal("200000"))
        );

        walletRepository.save(sender);
        walletRepository.save(receiver);

        TransferCommand command = new TransferCommand(
                "request-001",
                "wallet-an",
                "wallet-binh",
                new Money(new BigDecimal("300000"))
        );

        Transfer transfer = transferService.transfer(command);

        assertEquals(new BigDecimal("700000"), sender.getBalance().getAmount());
        assertEquals(new BigDecimal("500000"), receiver.getBalance().getAmount());
    }

    @Test
    void should_reject_when_sender_wallet_does_not_exist() {
        TransferCommand command = new TransferCommand(
                "request-001",
                "missing-wallet",
                "wallet-binh",
                new Money(new BigDecimal("3000000"))
        );

        assertThrows(IllegalArgumentException.class, () -> transferService.transfer(command));
    }

    @Test
    void should_reject_when_receiver_wallet_does_not_exist() {
        Wallet sender = new Wallet(
                "wallet-an",
                "user-an",
                new Money(new BigDecimal("1000000"))
        );

        walletRepository.save(sender);

        TransferCommand command = new TransferCommand(
                "request-001",
                "wallet-an",
                "missing-wallet",
                new Money(new BigDecimal("30000"))
        );

        assertThrows(IllegalArgumentException.class, () -> transferService.transfer(command));
    }

    @Test
    void should_reject_when_sender_balance_is_insufficient() {
        Wallet sender = new Wallet(
                "wallet-an",
                "user-id",
                new Money(new BigDecimal("50000"))
        );
        Wallet receiver = new Wallet(
                "wallet-binh",
                "user-binh",
                new Money(new BigDecimal("50000"))
        );

        walletRepository.save(sender);
        walletRepository.save(receiver);

        TransferCommand command = new TransferCommand(
                "transfer-001",
                "wallet-an",
                "wallet-binh",
                new Money(new BigDecimal("1000000"))
        );

        assertThrows(IllegalStateException.class, () -> transferService.transfer(command));
        assertEquals(new BigDecimal("50000"), sender.getBalance().getAmount());
        assertEquals(new BigDecimal("50000"), receiver.getBalance().getAmount());
        assertTrue(transferRepository.findByRequestId("transfer-001").isEmpty());
    }

    @Test
    void should_reject_duplicate_request_id() {
        Wallet sender = new Wallet(
                "wallet-an",
                "user-an",
                new Money(new BigDecimal("2000000"))
        );

        Wallet receiver = new Wallet(
                "wallet-binh",
                "user-binh",
                new Money(BigDecimal.ZERO)
        );

        walletRepository.save(sender);
        walletRepository.save(receiver);

        TransferCommand command = new TransferCommand(
                "request-001",
                "wallet-an",
                "wallet-binh",
                new Money(new BigDecimal("1000000"))
        );

        transferService.transfer(command);

        assertThrows(DuplicateTransferException.class, () -> transferService.transfer(command));
        assertEquals(new BigDecimal("1000000"), sender.getBalance().getAmount());
        assertEquals(new BigDecimal("1000000"), receiver.getBalance().getAmount());
    }

    @Test
    void should_reject_transfer_when_sender_is_blocked() {
        Wallet sender = new Wallet(
                "wallet-an",
                "user-an",
                new Money(new BigDecimal("1000000"))
        );

        Wallet receiver = new Wallet(
                "wallet-binh",
                "user-binh",
                new Money(new BigDecimal("1000000"))
        );

        sender.block();
        walletRepository.save(sender);
        walletRepository.save(receiver);

        TransferCommand command = new TransferCommand(
                "request-001",
                "wallet-an",
                "wallet-binh",
                new Money(new BigDecimal("500000"))
        );
        assertThrows(IllegalStateException.class, () -> transferService.transfer(command));
        assertEquals(new BigDecimal("1000000"), sender.getBalance().getAmount());
        assertEquals(new BigDecimal("1000000"), receiver.getBalance().getAmount());
        assertTrue(transferRepository.findByRequestId("request-001").isEmpty());
    }

    @Test
    void should_reject_transfer_when_receiver_is_blocked() {
        Wallet sender = new Wallet(
                "wallet-an",
                "user-an",
                new Money(new BigDecimal("1000000"))
        );

        Wallet receiver = new Wallet(
                "wallet-binh",
                "user-binh",
                new Money(new BigDecimal("1000000"))
        );

        receiver.block();
        walletRepository.save(sender);
        walletRepository.save(receiver);

        TransferCommand command = new TransferCommand(
                "request-001",
                "wallet-an",
                "wallet-binh",
                new Money(new BigDecimal("500000"))
        );
        assertThrows(IllegalStateException.class, () -> transferService.transfer(command));
        assertEquals(new BigDecimal("1000000"), sender.getBalance().getAmount());
        assertEquals(new BigDecimal("1000000"), receiver.getBalance().getAmount());
        assertTrue(transferRepository.findByRequestId("request-001").isEmpty());
    }

    @Test
    void should_save_succeeded_transfer() {
        Wallet sender = new Wallet(
                "wallet-an",
                "user-an",
                new Money(new BigDecimal("1000000"))
        );
        Wallet receiver = new Wallet(
                "wallet-binh",
                "user-binh",
                new Money(new BigDecimal("1000000"))
        );

        walletRepository.save(sender);
        walletRepository.save(receiver);

        TransferCommand command = new TransferCommand(
                "request-001",
                "wallet-an",
                "wallet-binh",
                new Money(new BigDecimal("300000"))
        );

        transferService.transfer(command);

        Transfer savedTransfer = transferRepository.findByRequestId("request-001")
                .orElseThrow();

        assertEquals(TransferStatus.SUCCEEDED, savedTransfer.getStatus());
        assertEquals(new BigDecimal("300000"), savedTransfer.getAmount().getAmount());
    }
}
