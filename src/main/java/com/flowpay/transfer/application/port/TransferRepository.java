package com.flowpay.transfer.application.port;

import com.flowpay.transfer.domain.Transfer;

import java.util.Optional;

public interface TransferRepository {
    Optional<Transfer> findByRequestId(String requestId);
    void save(Transfer transfer);
}
