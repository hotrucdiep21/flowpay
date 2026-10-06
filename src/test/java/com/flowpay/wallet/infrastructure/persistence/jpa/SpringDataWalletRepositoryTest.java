package com.flowpay.wallet.infrastructure.persistence.jpa;

import com.flowpay.support.FlowPayJpaTest;
import com.flowpay.wallet.domain.WalletStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

@FlowPayJpaTest
public class SpringDataWalletRepositoryTest {
    @Autowired
    private SpringDataWalletRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void should_save_and_find_wallet_entity() {
        WalletJpaEntity entity = new WalletJpaEntity(
                "wallet-jpa-test",
                "user-an",
                new BigDecimal("1000000"),
                WalletStatus.ACTIVE
        );
        repository.saveAndFlush(entity);

        entityManager.clear();

        WalletJpaEntity savedEntity = repository.findById("wallet-jpa-test").orElseThrow();
        assertEquals("wallet-jpa-test", savedEntity.getId());
        assertEquals("user-an", savedEntity.getOwnerId());
        assertEquals(
                new BigDecimal("1000000.00"),
                savedEntity.getBalance()
        );
        assertEquals(
                WalletStatus.ACTIVE,
                savedEntity.getStatus()
        );
    }
}
