package com.flowpay.wallet.application;

import com.flowpay.wallet.application.exception.WalletNotFoundException;
import com.flowpay.wallet.application.port.WalletRepository;
import com.flowpay.wallet.domain.Wallet;

import java.util.UUID;

public class WalletService {
    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public Wallet createWallet(CreateWalletCommand command) {
        Wallet wallet = new Wallet(
                UUID.randomUUID().toString(),
                command.ownerId(),
                command.initialBalance()
        );
        walletRepository.save(wallet);

        return wallet;
    }

    public Wallet getWallet(String walletId) {
        return walletRepository.findById(walletId).orElseThrow(() -> new WalletNotFoundException(walletId));
    }
}
