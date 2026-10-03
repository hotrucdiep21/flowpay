package com.flowpay.transfer.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataTransferRepository extends JpaRepository<TransferJpaEntity, String> {
    Optional<TransferJpaEntity> findByRequestId(String requestId);
}
