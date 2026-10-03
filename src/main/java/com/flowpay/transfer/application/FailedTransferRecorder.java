package com.flowpay.transfer.application;

import com.flowpay.transfer.application.port.TransferRepository;
import com.flowpay.transfer.domain.Transfer;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class FailedTransferRecorder {
    private final TransferRepository transferRepository;

    public FailedTransferRecorder(TransferRepository transferRepository) {
        this.transferRepository = transferRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Transfer failedTransfer) {
        transferRepository.save(failedTransfer);
    }
}
