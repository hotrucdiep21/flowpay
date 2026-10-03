package com.flowpay.wallet.infrastructure.persistence.jpa;

import com.flowpay.wallet.application.port.WalletRepository;
import com.flowpay.wallet.domain.Wallet;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Profile("jpa")
public class JpaWalletRepositoryAdapter implements WalletRepository {

    private final SpringDataWalletRepository repository;
    private final WalletPersistenceMapper mapper = new WalletPersistenceMapper();

    public JpaWalletRepositoryAdapter(SpringDataWalletRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Wallet> findById(String walletId) {
        return repository.findById(walletId)
                .map(mapper::toDomain);
    }

    @Override
    public void save(Wallet wallet) {
        WalletJpaEntity entity = repository
                .findById(wallet.getId())
                .map(existingEntity -> {
                    mapper.updateEntity(wallet, existingEntity);
                    return existingEntity;
                })
                .orElseGet(() -> mapper.toEntity(wallet));

        repository.save(entity);
    }
}
