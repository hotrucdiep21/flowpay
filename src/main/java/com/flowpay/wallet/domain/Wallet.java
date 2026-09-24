package com.flowpay.wallet.domain;

import com.flowpay.wallet.domain.exception.InsufficientBalanceException;
import com.flowpay.wallet.domain.exception.WalletNotActiveException;

public class Wallet {
    private final String id;
    private final String ownerId;
    private Money balance;
    private WalletStatus status;

    public Wallet(String id, String ownerId, Money initialBalance) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Wallet ID must not be blank!");
        }

        if (ownerId == null || ownerId.isBlank()) {
            throw new IllegalArgumentException("Owner Id must not be blank!");
        }

        if (initialBalance == null) {
            throw new IllegalArgumentException("initial balace must not be null!");
        }

        this.id = id;
        this.ownerId = ownerId;
        this.balance = initialBalance;
        this.status = WalletStatus.ACTIVE;
    }


    private void validateTransactionAmount(Money amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Transaction amount must not be null!");
        }

        if (amount.isZero()) {
            throw new IllegalArgumentException("Transaction amount must be greater than zero!");
        }
    }

    private void ensureActive() {
        if (this.status != WalletStatus.ACTIVE) {
            throw new WalletNotActiveException(id, status);
        }
    }

    public void ensureCanCredit(Money amount) {
        validateTransactionAmount(amount);
        ensureActive();
    }

    public void ensureCanDebit(Money amount) {
        validateTransactionAmount(amount);
        ensureActive();

        if (this.balance.isLessThan(amount)) {
            throw new InsufficientBalanceException(id, balance, amount);
        }
    }

    public void block() {
        this.status = WalletStatus.BLOCKED;
    }

    public void close() {
        this.status = WalletStatus.CLOSED;
    }

    public void credit(Money amount) {
        ensureCanCredit(amount);
        this.balance = this.balance.add(amount);
    }

    public void debit(Money amount) {
        ensureCanDebit(amount);
        if (this.balance.isLessThan(amount)) {
            throw new IllegalStateException("Insufficient wallet balance!");
        }

        this.balance = this.balance.subtract(amount);
    }

    public String getId() {
        return id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public Money getBalance() {
        return balance;
    }

    public WalletStatus getStatus() {
        return status;
    }
}
