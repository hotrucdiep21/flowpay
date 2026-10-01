package com.flowpay.wallet.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataWalletRepository extends JpaRepository<WalletJpaEntity, String> {
}
