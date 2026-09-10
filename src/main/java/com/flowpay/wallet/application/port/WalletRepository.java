package com.flowpay.wallet.application.port;

import com.flowpay.wallet.domain.Wallet;

import java.util.Optional;

public interface WalletRepository {
    Optional<Wallet> findById(String walletId);

    void save(Wallet wallet);
}
