package com.flowpay.transfer.api;

import com.flowpay.transfer.domain.Transfer;
import com.flowpay.transfer.domain.TransferStatus;

import java.math.BigDecimal;

public record TransferResponse(
        String id,
        String requestId,
        String senderWalletId,
        String receiverWalletId,
        BigDecimal amount,
        TransferStatus status
) {
    public static TransferResponse from(Transfer transfer) {
        return new TransferResponse(
                transfer.getId(),
                transfer.getRequestId(),
                transfer.getSenderWalletId(),
                transfer.getReceiverWalletId(),
                transfer.getAmount().getAmount(),
                transfer.getStatus()
        );
    }
}
