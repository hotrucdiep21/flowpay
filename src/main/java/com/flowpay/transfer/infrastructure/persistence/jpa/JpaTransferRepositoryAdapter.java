package com.flowpay.transfer.infrastructure.persistence.jpa;

import com.flowpay.transfer.application.port.TransferRepository;
import com.flowpay.transfer.domain.Transfer;
import com.flowpay.transfer.domain.exception.DuplicateTransferException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Profile("jpa")
public class JpaTransferRepositoryAdapter
        implements TransferRepository {

    private static final String REQUEST_ID_UNIQUE_CONSTRAINT =
            "uk_transfer_request_id";

    private final SpringDataTransferRepository repository;

    private final TransferPersistenceMapper mapper =
            new TransferPersistenceMapper();

    public JpaTransferRepositoryAdapter(
            SpringDataTransferRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public Optional<Transfer> findByRequestId(String requestId) {
        return repository
                .findByRequestId(requestId)
                .map(mapper::toDomain);
    }

    @Override
    public void save(Transfer transfer) {
        TransferJpaEntity entity = mapper.toEntity(transfer);

        try {
            repository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException exception) {
            if (isRequestIdUniqueConstraintViolation(exception)) {
                throw new DuplicateTransferException(
                        transfer.getRequestId()
                );
            }

            throw exception;
        }
    }

    private boolean isRequestIdUniqueConstraintViolation(
            Throwable throwable
    ) {
        Throwable current = throwable;

        while (current != null) {
            if (current instanceof ConstraintViolationException violation
                    && REQUEST_ID_UNIQUE_CONSTRAINT.equals(
                    violation.getConstraintName()
            )) {
                return true;
            }

            current = current.getCause();
        }

        return false;
    }
}