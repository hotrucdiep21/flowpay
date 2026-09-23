package com.flowpay.wallet.api;

import com.flowpay.wallet.domain.Wallet;
import com.flowpay.wallet.domain.WalletStatus;

import java.math.BigDecimal;

public record WalletResponse(
        String id,
        String ownerId,
        BigDecimal balance,
        WalletStatus status
) {
    public static WalletResponse from(Wallet wallet) {
        return new WalletResponse(
                wallet.getId(),
                wallet.getOwnerId(),
                wallet.getBalance().getAmount(),
                wallet.getStatus()
        );
    }
}
