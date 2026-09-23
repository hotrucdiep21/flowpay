package com.flowpay.transfer.application;

import com.flowpay.transfer.application.port.TransferRepository;
import com.flowpay.transfer.domain.Transfer;
import com.flowpay.transfer.domain.exception.DuplicateTransferException;
import com.flowpay.wallet.application.exception.WalletNotFoundException;
import com.flowpay.wallet.application.port.WalletRepository;
import com.flowpay.wallet.domain.Money;
import com.flowpay.wallet.domain.Wallet;

import java.util.UUID;

public class TransferService {
    private final WalletRepository walletRepository;
    private final TransferRepository transferRepository;

    public TransferService(WalletRepository walletRepository, TransferRepository transferRepository) {
        this.walletRepository = walletRepository;
        this.transferRepository = transferRepository;
    }

    public Transfer transfer(TransferCommand command) {
        if (transferRepository
                .findByRequestId(command.requestId())
                .isPresent()) {
            throw new DuplicateTransferException(command.requestId());
        }

        Transfer transfer = new Transfer(
                UUID.randomUUID().toString(),
                command.requestId(),
                command.senderWalletId(),
                command.receiverWalletId(),
                command.amount()
        );

        try {
            Wallet sender = walletRepository
                    .findById(command.senderWalletId())
                    .orElseThrow(() ->
                            new WalletNotFoundException(command.senderWalletId())
                    );

            Wallet receiver = walletRepository
                    .findById(command.receiverWalletId())
                    .orElseThrow(() ->
                            new WalletNotFoundException(command.receiverWalletId())
                    );

            Money amount = command.amount();

            sender.ensureCanDebit(amount);
            receiver.ensureCanCredit(amount);

            sender.debit(amount);
            receiver.credit(amount);

            transfer.markSucceeded();

            walletRepository.save(sender);
            walletRepository.save(receiver);
            transferRepository.save(transfer);

            return transfer;

        } catch (WalletNotFoundException | IllegalArgumentException | IllegalStateException exception) {
            transfer.markFailed();
            transferRepository.save(transfer);
            throw exception;
        }
    }
}
