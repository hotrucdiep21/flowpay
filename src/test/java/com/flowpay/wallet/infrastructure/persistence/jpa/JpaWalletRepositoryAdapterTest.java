package com.flowpay.wallet.infrastructure.persistence.jpa;

import com.flowpay.wallet.domain.Money;
import com.flowpay.wallet.domain.Wallet;
import com.flowpay.wallet.domain.WalletStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JpaWalletRepositoryAdapterTest {
    @Mock
    private SpringDataWalletRepository repository;
    private JpaWalletRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new JpaWalletRepositoryAdapter(repository);
    }

    @Test
    void should_find_wallet_by_id() {
        WalletJpaEntity entity = new WalletJpaEntity(
                "wallet-an",
                "user-an",
                new BigDecimal("1000000"),
                WalletStatus.BLOCKED
        );

        when(repository.findById("wallet-an"))
                .thenReturn(Optional.of(entity));

        Wallet wallet = adapter
                .findById("wallet-an")
                .orElseThrow();

        assertEquals("wallet-an", wallet.getId());
        assertEquals("user-an", wallet.getOwnerId());
        assertEquals(
                new BigDecimal("1000000"),
                wallet.getBalance().getAmount()
        );
        assertEquals(
                WalletStatus.BLOCKED,
                wallet.getStatus()
        );
    }

    @Test
    void should_save_new_wallet() {
        Wallet wallet = new Wallet(
                "wallet-new",
                "user-an",
                new Money(new BigDecimal("1000000"))
        );

        when(repository.findById("wallet-new"))
                .thenReturn(Optional.empty());

        adapter.save(wallet);

        ArgumentCaptor<WalletJpaEntity> captor =
                ArgumentCaptor.forClass(WalletJpaEntity.class);

        verify(repository).save(captor.capture());

        WalletJpaEntity savedEntity = captor.getValue();

        assertEquals("wallet-new", savedEntity.getId());
        assertEquals("user-an", savedEntity.getOwnerId());
        assertEquals(
                new BigDecimal("1000000"),
                savedEntity.getBalance()
        );
        assertEquals(
                WalletStatus.ACTIVE,
                savedEntity.getStatus()
        );
    }

    @Test
    void should_update_existing_wallet() {
        WalletJpaEntity existingEntity = new WalletJpaEntity(
                "wallet-an",
                "user-an",
                new BigDecimal("1000000"),
                WalletStatus.ACTIVE
        );

        Wallet changedWallet = Wallet.restore(
                "wallet-an",
                "user-an",
                new Money(new BigDecimal("700000")),
                WalletStatus.BLOCKED
        );

        when(repository.findById("wallet-an"))
                .thenReturn(Optional.of(existingEntity));

        adapter.save(changedWallet);

        assertEquals(
                new BigDecimal("700000"),
                existingEntity.getBalance()
        );
        assertEquals(
                WalletStatus.BLOCKED,
                existingEntity.getStatus()
        );

        verify(repository).save(existingEntity);
    }
}
