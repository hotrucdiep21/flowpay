package com.flowpay.infrastructure.config;

import com.flowpay.transfer.application.TransferService;
import com.flowpay.transfer.application.port.TransferRepository;
import com.flowpay.wallet.application.WalletService;
import com.flowpay.wallet.application.port.WalletRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ApplicationConfigTest {
    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private TransferService transferService;

    @Autowired
    private WalletService walletService;

    @Test
    void should_create_application_dependencies() {
        assertNotNull(walletRepository);
        assertNotNull(transferRepository);
        assertNotNull(transferService);
        assertNotNull(walletService);
    }
}
