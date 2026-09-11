package com.flowpay.infrastructure.persistence.inmemory;

import com.flowpay.wallet.application.port.WalletRepository;
import com.flowpay.wallet.domain.Wallet;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryWalletRepository implements WalletRepository {
    private final Map<String, Wallet> wallets = new HashMap<>();

    @Override
    public Optional<Wallet> findById(String walletId) {
        if (wallets.containsKey(walletId)) {
            Wallet wallet = wallets.get(walletId);
            return Optional.of(wallet);
        }
        return Optional.empty();
    }

    @Override
    public void save(Wallet wallet) {
        wallets.put(wallet.getId(), wallet);
    }
}
