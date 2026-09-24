package com.flowpay.wallet.domain.exception;

import com.flowpay.wallet.domain.WalletStatus;

public class WalletNotActiveException extends IllegalStateException {
    public WalletNotActiveException(
            String walletId,
            WalletStatus status
    ) {
        super(
                "Wallet " + walletId
                        + " is not active. Current status: "
                        + status
        );
    }
}
