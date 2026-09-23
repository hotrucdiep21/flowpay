package com.flowpay.infrastructure.config;

import com.flowpay.infrastructure.persistence.inmemory.InMemoryTransferRepository;
import com.flowpay.infrastructure.persistence.inmemory.InMemoryWalletRepository;
import com.flowpay.transfer.application.TransferService;
import com.flowpay.transfer.application.port.TransferRepository;
import com.flowpay.wallet.application.WalletService;
import com.flowpay.wallet.application.port.WalletRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {
    @Bean
    public WalletRepository walletRepository() {
        return new InMemoryWalletRepository();
    }

    @Bean
    public TransferRepository transferRepository() {
        return new InMemoryTransferRepository();
    }

    @Bean
    public TransferService transferService(WalletRepository walletRepository,
                                           TransferRepository transferRepository) {
        return new TransferService(walletRepository, transferRepository);
    }

    @Bean
    public WalletService walletService(WalletRepository walletRepository) {
        return new WalletService(walletRepository);
    }
}
