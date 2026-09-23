package com.flowpay.transfer.api;

import java.math.BigDecimal;

public record CreateTransferRequest(
        String requestId,
        String senderWalletId,
        String receiverWalletId,
        BigDecimal amount
) {
}
