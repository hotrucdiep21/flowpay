package com.flowpay.transfer.domain.exception;

import com.flowpay.wallet.application.exception.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

public class DuplicateTransferException extends RuntimeException {
    public DuplicateTransferException(String requestId) {
        super("Transfer request already exists: " + requestId);
    }

    @ExceptionHandler(DuplicateTransferException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateTransfer(DuplicateTransferException exception) {
        ApiErrorResponse response = new ApiErrorResponse(
                "DUPLICATE_TRANSFER_REQUEST",
                exception.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
}
