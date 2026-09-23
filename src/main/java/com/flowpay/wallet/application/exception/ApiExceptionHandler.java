package com.flowpay.wallet.application.exception;

import com.flowpay.transfer.domain.exception.DuplicateTransferException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleWalletNotFound(WalletNotFoundException exception) {
        ApiErrorResponse response = new ApiErrorResponse(
                "WALLET_NOT_FOUND",
                exception.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(DuplicateTransferException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateTransfer(
            DuplicateTransferException exception
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                "DUPLICATE_TRANSFER_REQUEST",
                exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }
}
