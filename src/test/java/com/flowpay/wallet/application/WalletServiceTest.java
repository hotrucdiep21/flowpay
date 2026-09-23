package com.flowpay.wallet.application;

import com.flowpay.infrastructure.persistence.inmemory.InMemoryWalletRepository;
import com.flowpay.wallet.domain.Money;
import com.flowpay.wallet.domain.Wallet;
import com.flowpay.wallet.domain.WalletStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class WalletServiceTest {
    private InMemoryWalletRepository walletRepository;
    private WalletService walletService;

    @BeforeEach
    void setUp() {
        walletRepository = new InMemoryWalletRepository();
        walletService = new WalletService(walletRepository);
    }

    @Test
    void should_create_and_save_wallet() {
        CreateWalletCommand command = new CreateWalletCommand(
                "user-an",
                new Money(new BigDecimal("1000000"))
        );

        Wallet wallet = walletService.createWallet(command);

        assertNotNull(wallet.getId());
        assertFalse(wallet.getId().isBlank());
        assertEquals("user-an", wallet.getOwnerId());

        assertEquals(
                new BigDecimal("1000000"),
                wallet.getBalance().getAmount()
        );

        assertEquals(WalletStatus.ACTIVE, wallet.getStatus());
        Wallet savedWallet = walletRepository.findById(wallet.getId()).orElseThrow();

        assertSame(wallet, savedWallet);
    }
}
