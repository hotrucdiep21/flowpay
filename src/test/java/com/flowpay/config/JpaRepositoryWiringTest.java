package com.flowpay.config;

import com.flowpay.support.PostgresTestContainerConfig;
import com.flowpay.transfer.application.port.TransferRepository;
import com.flowpay.transfer.infrastructure.persistence.jpa.JpaTransferRepositoryAdapter;
import com.flowpay.wallet.application.port.WalletRepository;
import com.flowpay.wallet.infrastructure.persistence.jpa.JpaWalletRepositoryAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@SpringBootTest
@ActiveProfiles("jpa")
@Import(PostgresTestContainerConfig.class)
class JpaRepositoryWiringTest {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransferRepository transferRepository;

    @Test
    void should_use_jpa_repositories_when_jpa_profile_is_active() {
        assertInstanceOf(
                JpaWalletRepositoryAdapter.class,
                walletRepository
        );

        assertInstanceOf(
                JpaTransferRepositoryAdapter.class,
                transferRepository
        );
    }
}