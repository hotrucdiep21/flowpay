package com.flowpay.transfer.application;

import com.flowpay.wallet.domain.Money;

public record TransferCommand(
        String requestId,
        String senderWalletId,
        String receiverWalletId,
        Money amount
) {
}
