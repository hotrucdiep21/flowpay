package com.flowpay.wallet.infrastructure.persistence.jpa;

import com.flowpay.wallet.domain.Wallet;
import com.flowpay.wallet.domain.WalletStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;


@Entity
@Table(name = "wallets")
public class WalletJpaEntity {
    @Id
    @Column(name = "id", length = 100, nullable = false)
    private String id;

    @Column(name = "owner_id", length = 100, nullable = false)
    private String ownerId;

    @Column(
            name = "balance",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private WalletStatus status;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected WalletJpaEntity() {
    }

    public WalletJpaEntity(
            String id,
            String ownerId,
            BigDecimal balance,
            WalletStatus status
    ) {
        this.id = id;
        this.ownerId = ownerId;
        this.balance = balance;
        this.status = status;
    }

    void update(BigDecimal balance, WalletStatus status) {
        this.balance = balance;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public WalletStatus getStatus() {
        return status;
    }

    public long getVersion() {
        return version;
    }
}
