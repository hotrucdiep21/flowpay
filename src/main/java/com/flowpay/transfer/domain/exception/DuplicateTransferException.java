package com.flowpay.transfer.domain.exception;

public class DuplicateTransferException extends RuntimeException {
    public DuplicateTransferException() {
        super("Transfer request already exists!");
    }
}
