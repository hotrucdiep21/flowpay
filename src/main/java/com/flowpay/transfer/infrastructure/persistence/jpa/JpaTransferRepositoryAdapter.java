package com.flowpay.transfer.infrastructure.persistence.jpa;

import com.flowpay.transfer.application.port.TransferRepository;
import com.flowpay.transfer.domain.Transfer;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Profile("jpa")
public class JpaTransferRepositoryAdapter implements TransferRepository {
    private final SpringDataTransferRepository repository;
    private final TransferPersistenceMapper mapper =
            new TransferPersistenceMapper();

    public JpaTransferRepositoryAdapter(SpringDataTransferRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Transfer> findByRequestId(String requestId) {
        return repository.findByRequestId(requestId)
                .map(mapper::toDomain);
    }

    @Override
    public void save(Transfer transfer) {
        TransferJpaEntity entity = mapper.toEntity(transfer);
        repository.save(entity);
    }
}
