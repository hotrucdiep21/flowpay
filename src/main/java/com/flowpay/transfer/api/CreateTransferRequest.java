package com.flowpay.transfer.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateTransferRequest(
        @NotBlank(message = "Request ID must not be blank")
        String requestId,
        @NotBlank(message = "Sender Wallet Id must not be blank")
        String senderWalletId,
        @NotBlank(message = "Receiver Wallet Id must not be blank")
        String receiverWalletId,
        @NotNull(message = "Amount must not be null")
        @Positive(message = "Amount must be greater than zero")
        BigDecimal amount
) {
}
