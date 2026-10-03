package com.flowpay.transfer.infrastructure.persistence.jpa;

import com.flowpay.transfer.domain.TransferStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transfers")
public class TransferJpaEntity {
    @Id
    @Column(name = "id", length = 100, nullable = false)
    private String id;

    @Column(
            name = "request_id",
            length = 100,
            nullable = false,
            unique = true
    )
    private String requestId;

    @Column(
            name = "sender_wallet_id",
            length = 100,
            nullable = false
    )
    private String senderWalletId;

    @Column(
            name = "receiver_wallet_id",
            length = 100,
            nullable = false
    )
    private String receiverWalletId;

    @Column(
            name = "amount",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private TransferStatus status;

    @Column(
            name = "created_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private Instant createdAt;

    protected TransferJpaEntity() {
    }

    public TransferJpaEntity(
            String id,
            String requestId,
            String senderWalletId,
            String receiverWalletId,
            BigDecimal amount,
            TransferStatus status
    ) {
        this.id = id;
        this.requestId = requestId;
        this.senderWalletId = senderWalletId;
        this.receiverWalletId = receiverWalletId;
        this.amount = amount;
        this.status = status;
    }

    void updateStatus(TransferStatus status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public TransferStatus getStatus() {
        return status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getReceiverWalletId() {
        return receiverWalletId;
    }

    public String getSenderWalletId() {
        return senderWalletId;
    }

    public String getRequestId() {
        return requestId;
    }
}
