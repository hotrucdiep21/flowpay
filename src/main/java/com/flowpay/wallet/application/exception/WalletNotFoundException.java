package com.flowpay.wallet.application.exception;

public class WalletNotFoundException extends RuntimeException {
    public WalletNotFoundException(String walletId) {
        super("Wallet not found: " + walletId);
    }
}
