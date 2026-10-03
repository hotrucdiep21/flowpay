package com.flowpay.transfer.infrastructure.persistence.jpa;

import com.flowpay.transfer.domain.Transfer;
import com.flowpay.wallet.domain.Money;

public class TransferPersistenceMapper {
    TransferJpaEntity toEntity(Transfer transfer) {
        return new TransferJpaEntity(
                transfer.getId(),
                transfer.getRequestId(),
                transfer.getSenderWalletId(),
                transfer.getReceiverWalletId(),
                transfer.getAmount().getAmount(),
                transfer.getStatus()
        );
    }

    Transfer toDomain(TransferJpaEntity entity) {
        return Transfer.restore(
                entity.getId(),
                entity.getRequestId(),
                entity.getSenderWalletId(),
                entity.getReceiverWalletId(),
                new Money(entity.getAmount()),
                entity.getStatus()
        );
    }

    void updateEntity(
            Transfer transfer,
            TransferJpaEntity entity
    ) {
        entity.updateStatus(transfer.getStatus());
    }
}
