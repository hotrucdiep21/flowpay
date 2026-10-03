package com.flowpay.wallet.infrastructure.persistence.jpa;

import com.flowpay.wallet.domain.Money;
import com.flowpay.wallet.domain.Wallet;

public class WalletPersistenceMapper {
    WalletJpaEntity toEntity(Wallet wallet) {
        return new WalletJpaEntity(
                wallet.getId(),
                wallet.getOwnerId(),
                wallet.getBalance().getAmount(),
                wallet.getStatus()
        );
    }

    Wallet toDomain(WalletJpaEntity entity) {
        return Wallet.restore(
                entity.getId(),
                entity.getOwnerId(),
                new Money(entity.getBalance()),
                entity.getStatus()
        );
    }

    void updateEntity(
            Wallet wallet,
            WalletJpaEntity entity
    ) {
        entity.update(wallet.getBalance().getAmount(), wallet.getStatus());
    }
}
