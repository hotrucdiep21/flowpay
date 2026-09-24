package com.flowpay.transfer.api;

import com.flowpay.transfer.application.port.TransferRepository;
import com.flowpay.transfer.domain.Transfer;
import com.flowpay.transfer.domain.TransferStatus;
import com.flowpay.wallet.application.port.WalletRepository;
import com.flowpay.wallet.domain.Money;
import com.flowpay.wallet.domain.Wallet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransferRepository transferRepository;

    @Test
    void should_return_404_when_sender_wallet_does_not_exist() throws Exception {
        Wallet receiver = new Wallet(
                "wallet-existing-receiver",
                "user-binh",
                new Money(BigDecimal.ZERO)
        );
        walletRepository.save(receiver);

        String requestBody = """
                {
                    "requestId": "request-missing-sender",
                    "senderWalletId": "missing-sender",
                    "receiverWalletId": "wallet-existing-receiver",
                    "amount": 300000
                }
                """;
        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.code").value("WALLET_NOT_FOUND"))
                .andExpect(
                        jsonPath("$.message").value("Wallet not found: missing-sender"));

        assertEquals(BigDecimal.ZERO, receiver.getBalance().getAmount());

        Transfer failedTransfer = transferRepository.findByRequestId("request-missing-sender").orElseThrow();

        assertEquals(TransferStatus.FAILED, failedTransfer.getStatus());
    }

    @Test
    void should_return_404_when_receiver_wallet_does_not_exist() throws Exception {
        Wallet sender = new Wallet(
                "wallet-existing-sender",
                "user-an",
                new Money(new BigDecimal("1000000"))
        );

        walletRepository.save(sender);

        String requestBody = """
                {
                  "requestId": "request-missing-receiver",
                  "senderWalletId": "wallet-existing-sender",
                  "receiverWalletId": "missing-receiver",
                  "amount": 300000
                }
                """;

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.code").value("WALLET_NOT_FOUND"))
                .andExpect(
                        jsonPath("$.message").value("Wallet not found: missing-receiver"));

        // Sender chưa bị trừ tiền
        assertEquals(
                new BigDecimal("1000000"),
                sender.getBalance().getAmount()
        );
        Transfer failedTransfer = transferRepository
                .findByRequestId("request-missing-receiver")
                .orElseThrow();

        assertEquals(
                TransferStatus.FAILED,
                failedTransfer.getStatus()
        );
    }

    @Test
    void should_transfer_money_between_two_wallets() throws Exception {
        Wallet sender = new Wallet(
                "wallet-api-an",
                "user-an",
                new Money(new BigDecimal("1000000"))
        );

        Wallet receiver = new Wallet(
                "wallet-api-binh",
                "user-binh",
                new Money(new BigDecimal("200000"))
        );

        walletRepository.save(sender);
        walletRepository.save(receiver);

        String requestBody = """
                {
                  "requestId": "request-api-001",
                  "senderWalletId": "wallet-api-an",
                  "receiverWalletId": "wallet-api-binh",
                  "amount": 300000
                }
                """;

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "/api/transfers/request-api-001"
                ))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.requestId")
                        .value("request-api-001"))
                .andExpect(jsonPath("$.senderWalletId")
                        .value("wallet-api-an"))
                .andExpect(jsonPath("$.receiverWalletId")
                        .value("wallet-api-binh"))
                .andExpect(jsonPath("$.amount").value(300000))
                .andExpect(jsonPath("$.status").value("SUCCEEDED"));

        assertEquals(
                new BigDecimal("700000"),
                sender.getBalance().getAmount()
        );

        assertEquals(
                new BigDecimal("500000"),
                receiver.getBalance().getAmount()
        );
    }

    @Test
    void should_reject_duplicate_transfer_request() throws Exception {
        Wallet sender = new Wallet(
                "wallet-duplicate-sender",
                "user-an",
                new Money(new BigDecimal("1000000"))
        );
        Wallet receiver = new Wallet(
                "wallet-duplicate-receiver",
                "user-binh",
                new Money(BigDecimal.ZERO)
        );

        walletRepository.save(sender);
        walletRepository.save(receiver);

        String requestBody = """
                {
                    "requestId":"request-duplicate-api-001",
                    "senderWalletId": "wallet-duplicate-sender",
                    "receiverWalletId": "wallet-duplicate-receiver",
                    "amount": 300000
                }
                """;
//        first request success
        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

//        the second one - failed
        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.code").value("DUPLICATE_TRANSFER_REQUEST"))
                .andExpect(
                        jsonPath("$.message").value("Transfer request already exists: " +
                                "request-duplicate-api-001"));

//        Chungs minh tien chi chuyen dung 1 lan
        assertEquals(new BigDecimal("700000"), sender.getBalance().getAmount());
        assertEquals(new BigDecimal("300000"), receiver.getBalance().getAmount());

    }

    @Test
    void should_return_409_when_sender_balance_is_insufficient() throws Exception {
        Wallet sender = new Wallet(
                "wallet-poor-sender",
                "user-an",
                new Money(new BigDecimal("100000"))
        );

        Wallet receiver = new Wallet(
                "wallet-insufficient-receiver",
                "user-binh",
                new Money(BigDecimal.ZERO)
        );

        walletRepository.save(sender);
        walletRepository.save(receiver);

        String requestBody = """
                {
                  "requestId": "request-insufficient-balance",
                  "senderWalletId": "wallet-poor-sender",
                  "receiverWalletId": "wallet-insufficient-receiver",
                  "amount": 300000
                }
                """;

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_BALANCE"));

        assertEquals(new BigDecimal("100000"), sender.getBalance().getAmount());
        assertEquals(BigDecimal.ZERO, receiver.getBalance().getAmount());

        Transfer failedTransfer = transferRepository.findByRequestId("request-insufficient-balance").orElseThrow();

        assertEquals(TransferStatus.FAILED, failedTransfer.getStatus());
    }

    @Test
    void should_return_409_without_debiting_sender_when_receiver_is_blocked()
            throws Exception {

        Wallet sender = new Wallet(
                "wallet-blocked-test-sender",
                "user-an",
                new Money(new BigDecimal("1000000"))
        );

        Wallet receiver = new Wallet(
                "wallet-blocked-receiver",
                "user-binh",
                new Money(new BigDecimal("200000"))
        );

        receiver.block();

        walletRepository.save(sender);
        walletRepository.save(receiver);

        String requestBody = """
                {
                  "requestId": "request-blocked-receiver",
                  "senderWalletId": "wallet-blocked-test-sender",
                  "receiverWalletId": "wallet-blocked-receiver",
                  "amount": 300000
                }
                """;

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.code")
                                .value("WALLET_NOT_ACTIVE")
                );

        assertEquals(
                new BigDecimal("1000000"),
                sender.getBalance().getAmount()
        );

        assertEquals(
                new BigDecimal("200000"),
                receiver.getBalance().getAmount()
        );

        Transfer failedTransfer = transferRepository
                .findByRequestId("request-blocked-receiver")
                .orElseThrow();

        assertEquals(
                TransferStatus.FAILED,
                failedTransfer.getStatus()
        );
    }
}