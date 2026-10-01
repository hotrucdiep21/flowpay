package com.flowpay.wallet.domain;

import com.flowpay.wallet.domain.exception.InsufficientBalanceException;
import com.flowpay.wallet.domain.exception.WalletNotActiveException;

public final class Wallet {

    private final String id;
    private final String ownerId;
    private Money balance;
    private WalletStatus status;

    /**
     * Tạo một Wallet hoàn toàn mới.
     * Wallet mới luôn bắt đầu với trạng thái ACTIVE.
     */
    public Wallet(
            String id,
            String ownerId,
            Money initialBalance
    ) {
        this(
                id,
                ownerId,
                initialBalance,
                WalletStatus.ACTIVE
        );
    }

    /**
     * Constructor dùng chung cho việc tạo mới và khôi phục Wallet.
     */
    private Wallet(
            String id,
            String ownerId,
            Money balance,
            WalletStatus status
    ) {
        validateWallet(id, ownerId, balance, status);

        this.id = id;
        this.ownerId = ownerId;
        this.balance = balance;
        this.status = status;
    }

    /**
     * Khôi phục một Wallet đã tồn tại từ database.
     *
     * Không tự đặt status thành ACTIVE mà giữ nguyên trạng thái đã lưu.
     */
    public static Wallet restore(
            String id,
            String ownerId,
            Money balance,
            WalletStatus status
    ) {
        return new Wallet(
                id,
                ownerId,
                balance,
                status
        );
    }

    private static void validateWallet(
            String id,
            String ownerId,
            Money balance,
            WalletStatus status
    ) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Wallet ID must not be blank!"
            );
        }

        if (ownerId == null || ownerId.isBlank()) {
            throw new IllegalArgumentException(
                    "Owner ID must not be blank!"
            );
        }

        if (balance == null) {
            throw new IllegalArgumentException(
                    "Wallet balance must not be null!"
            );
        }

        if (status == null) {
            throw new IllegalArgumentException(
                    "Wallet status must not be null!"
            );
        }
    }

    private static void validateTransactionAmount(Money amount) {
        if (amount == null) {
            throw new IllegalArgumentException(
                    "Transaction amount must not be null!"
            );
        }

        if (amount.isZero()) {
            throw new IllegalArgumentException(
                    "Transaction amount must be greater than zero!"
            );
        }
    }

    private void ensureActive() {
        if (this.status != WalletStatus.ACTIVE) {
            throw new WalletNotActiveException(
                    this.id,
                    this.status
            );
        }
    }

    /**
     * Kiểm tra Wallet có đủ điều kiện nhận tiền hay không.
     * Method này chỉ kiểm tra, chưa thay đổi số dư.
     */
    public void ensureCanCredit(Money amount) {
        validateTransactionAmount(amount);
        ensureActive();
    }

    /**
     * Kiểm tra Wallet có đủ điều kiện bị trừ tiền hay không.
     * Method này chỉ kiểm tra, chưa thay đổi số dư.
     */
    public void ensureCanDebit(Money amount) {
        validateTransactionAmount(amount);
        ensureActive();

        if (this.balance.isLessThan(amount)) {
            throw new InsufficientBalanceException(
                    this.id,
                    this.balance,
                    amount
            );
        }
    }

    public void credit(Money amount) {
        ensureCanCredit(amount);
        this.balance = this.balance.add(amount);
    }

    public void debit(Money amount) {
        ensureCanDebit(amount);
        this.balance = this.balance.subtract(amount);
    }

    public void block() {
        this.status = WalletStatus.BLOCKED;
    }

    public void close() {
        this.status = WalletStatus.CLOSED;
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