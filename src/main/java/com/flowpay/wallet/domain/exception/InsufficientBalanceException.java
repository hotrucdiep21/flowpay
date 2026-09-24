package com.flowpay.wallet.domain.exception;

import com.flowpay.wallet.domain.Money;

public class InsufficientBalanceException extends IllegalStateException {
    public InsufficientBalanceException(String walletId, Money availableBalance, Money requestedAmount) {
        super(
                "Wallet " + walletId
                        + " has insufficient balance. Available: "
                        + availableBalance.getAmount()
                        + ", requested: "
                        + requestedAmount.getAmount()
        );
    }
}
