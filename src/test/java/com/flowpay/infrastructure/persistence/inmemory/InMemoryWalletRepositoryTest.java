package com.flowpay.infrastructure.persistence.inmemory;

import com.flowpay.wallet.domain.Money;
import com.flowpay.wallet.domain.Wallet;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Optional;

public class InMemoryWalletRepositoryTest {
    @Test
    void should_save_and_find_wallet_by_id() {
        InMemoryWalletRepository repository = new InMemoryWalletRepository();

        Wallet wallet = new Wallet("wallet-an", "user-an", new Money(new BigDecimal("1000000")));
        repository.save(wallet);

        Optional<Wallet> result = repository.findById("wallet-an");
        assertTrue(result.isPresent());
        assertSame(wallet, result.get());
    }

    @Test
    void should_return_empty_when_wallet_does_not_exist() {
        InMemoryWalletRepository repository = new InMemoryWalletRepository();

        Optional<Wallet> result = repository.findById("unknown-wallet");

        assertTrue(result.isEmpty());
    }
}
