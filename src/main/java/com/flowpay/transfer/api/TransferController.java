package com.flowpay.transfer.api;

import com.flowpay.transfer.application.TransferCommand;
import com.flowpay.transfer.application.TransferService;
import com.flowpay.transfer.domain.Transfer;
import com.flowpay.wallet.domain.Money;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {
    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public ResponseEntity<TransferResponse> createTransfer(@RequestBody CreateTransferRequest request) {
        TransferCommand command = new TransferCommand(
                request.requestId(),
                request.senderWalletId(),
                request.receiverWalletId(),
                new Money(request.amount())
        );

        Transfer transfer = transferService.transfer(command);
        TransferResponse response = TransferResponse.from(transfer);

        URI location = URI.create("/api/transfers/" + transfer.getRequestId());

        return ResponseEntity.created(location).body(response);
    }
}
