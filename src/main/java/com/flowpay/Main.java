package com.flowpay;

import com.flowpay.infrastructure.persistence.inmemory.InMemoryTransferRepository;
import com.flowpay.infrastructure.persistence.inmemory.InMemoryWalletRepository;
import com.flowpay.transfer.application.TransferCommand;
import com.flowpay.transfer.application.TransferService;
import com.flowpay.transfer.domain.Transfer;
import com.flowpay.wallet.domain.Money;
import com.flowpay.wallet.domain.Wallet;

import java.math.BigDecimal;

public class Main {

    public static void main(String[] args) {
        InMemoryWalletRepository walletRepository = new InMemoryWalletRepository();
        InMemoryTransferRepository transferRepository = new InMemoryTransferRepository();

        TransferService transferService = new TransferService(
                walletRepository,
                transferRepository
        );

        Wallet sender = new Wallet(
                "wallet-an",
                "user-an",
                new Money(new BigDecimal("1000000"))
        );

        Wallet receiver = new Wallet(
                "wallet-binh",
                "user-binh",
                new Money(new BigDecimal("200000"))
        );

        walletRepository.save(sender);
        walletRepository.save(receiver);

        System.out.println("Before Transfer");
        printBalances(sender, receiver);
        TransferCommand command = new TransferCommand(
                "request-001",
                "wallet-an",
                "wallet-binh",
                new Money(new BigDecimal("300000"))
        );

        Transfer transfer = transferService.transfer(command);

        System.out.println("\nAfter transfer:");
        printBalances(sender, receiver);

        System.out.println("\nTransfer:");
        System.out.println("ID: " + transfer.getId());
        System.out.println("Request ID: " + transfer.getRequestId());
        System.out.println("Status: " + transfer.getStatus());

    }

    private static void printBalances(Wallet sender, Wallet receiver) {
        System.out.println(
                "Sender balance: " + sender.getBalance().getAmount()
        );

        System.out.println(
                "Receiver balance: " + receiver.getBalance().getAmount()
        );
    }
}