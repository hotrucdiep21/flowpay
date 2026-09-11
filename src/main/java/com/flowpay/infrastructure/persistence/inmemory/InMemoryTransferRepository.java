package com.flowpay.infrastructure.persistence.inmemory;

import com.flowpay.transfer.application.port.TransferRepository;
import com.flowpay.transfer.domain.Transfer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryTransferRepository implements TransferRepository {
    private final Map<String, Transfer> transfers = new HashMap<>();

    @Override
    public Optional<Transfer> findByRequestId(String requestId) {
        return Optional.ofNullable(transfers.get(requestId));
    }

    @Override
    public void save(Transfer transfer) {
        transfers.put(transfer.getRequestId(), transfer);
    }
}
