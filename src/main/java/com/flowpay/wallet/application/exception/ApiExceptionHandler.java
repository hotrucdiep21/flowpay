package com.flowpay.wallet.application.exception;

import com.flowpay.transfer.domain.exception.DuplicateTransferException;
import com.flowpay.wallet.domain.exception.InsufficientBalanceException;
import com.flowpay.wallet.domain.exception.WalletNotActiveException;
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

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ApiErrorResponse> handleInsufficientBalance(InsufficientBalanceException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiErrorResponse("INSUFFICIENT_BALANCE",
                exception.getMessage()));
    }

    @ExceptionHandler(WalletNotActiveException.class)
    public ResponseEntity<ApiErrorResponse> handleWalletNotActive(
            WalletNotActiveException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse(
                        "WALLET_NOT_ACTIVE",
                        exception.getMessage()
                ));
    }
}
