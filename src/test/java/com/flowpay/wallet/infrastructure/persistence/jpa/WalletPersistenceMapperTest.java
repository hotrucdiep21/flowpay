package com.flowpay.wallet.infrastructure.persistence.jpa;

import com.flowpay.wallet.domain.Money;
import com.flowpay.wallet.domain.Wallet;
import com.flowpay.wallet.domain.WalletStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WalletPersistenceMapperTest {
    private final WalletPersistenceMapper mapper = new WalletPersistenceMapper();

    //    Domain ----> Entity
    @Test
    void should_map_domain_wallet_to_jpa_entity() {
        Wallet wallet = new Wallet(
                "wallet-an",
                "user-an",
                new Money(new BigDecimal("1000000"))
        );

        WalletJpaEntity entity = mapper.toEntity(wallet);

        assertEquals("wallet-an", entity.getId());
        assertEquals("user-an", entity.getOwnerId());
        assertEquals(
                new BigDecimal("1000000"),
                entity.getBalance()
        );
        assertEquals(
                WalletStatus.ACTIVE,
                entity.getStatus()
        );
    }

    //    Entity ------> Domain
    @Test
    void should_map_jpa_entity_to_domain_wallet() {
        WalletJpaEntity entity = new WalletJpaEntity(
                "wallet-an",
                "user-an",
                new BigDecimal("1000000"),
                WalletStatus.BLOCKED
        );

        Wallet wallet = mapper.toDomain(entity);

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
}
