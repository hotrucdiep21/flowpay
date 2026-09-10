package com.flowpay.transfer.domain;

import com.flowpay.wallet.domain.Money;

public class Transfer {
    private final String id;
    private final String requestId;
    private final String senderWalletId;
    private final String receiverWalletId;
    private final Money amount;
    private TransferStatus status;

    public Transfer(String id, String requestId, String senderWalletId, String receiverWalletId, Money amount) {
        validateTransfer(id, requestId, senderWalletId, receiverWalletId, amount);

        this.id = id;
        this.requestId = requestId;
        this.senderWalletId = senderWalletId;
        this.receiverWalletId = receiverWalletId;
        this.amount = amount;
        this.status = TransferStatus.PENDING;
    }


    private static void validateTransfer(String id,
                                         String requestId,
                                         String senderWalletId,
                                         String receiverWalletId,
                                         Money amount) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Transfer ID must not be blank!");
        }
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("Request ID must not be blank!");
        }
        if (senderWalletId == null || senderWalletId.isBlank()) {
            throw new IllegalArgumentException("Sender wallet ID must not be blank!");
        }
        if (receiverWalletId == null || receiverWalletId.isBlank()) {
            throw new IllegalArgumentException("Receiver wallet ID must not be blank!");
        }
        if (amount == null) {
            throw new IllegalArgumentException("Transfer must not null!");
        }
        if (amount.isZero()) {
            throw new IllegalArgumentException("Amount must be greater than zero!");
        }
        if (senderWalletId.equals(receiverWalletId)) {
            throw new IllegalArgumentException("Sender and receiver wallet must be different!");
        }
    }

    private void ensurePending() {
        if(this.status != TransferStatus.PENDING) {
            throw new IllegalStateException(
                    "Only pending transfer can change status!"
            );
        }
    }

    public void markSucceeded() {
        ensurePending();
        this.status = TransferStatus.SUCCESS;
    }

    public void markFailed() {
        ensurePending();
        this.status = TransferStatus.FAILED;
    }

    public String getId() {
        return id;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getSenderWalletId() {
        return senderWalletId;
    }

    public String getReceiverWalletId() {
        return receiverWalletId;
    }

    public Money getAmount() {
        return amount;
    }

    public TransferStatus getStatus() {
        return status;
    }
}
