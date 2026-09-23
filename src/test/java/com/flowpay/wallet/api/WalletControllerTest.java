package com.flowpay.wallet.api;

import com.flowpay.wallet.application.port.WalletRepository;
import com.flowpay.wallet.domain.Money;
import com.flowpay.wallet.domain.Wallet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WalletControllerTest {

    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void should_create_wallet() throws Exception {
        String requestBody = """
                {
                  "ownerId": "user-an",
                  "initialBalance": 1000000
                }
                """;

        mockMvc.perform(post("/api/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.ownerId").value("user-an"))
                .andExpect(jsonPath("$.balance").value(1000000))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void should_get_wallet_by_id() throws Exception {
        String walletId = "wallet-get-001";
        Wallet wallet = new Wallet(
                walletId,
                "user-an",
                new Money(new BigDecimal("1000000"))
        );
        walletRepository.save(wallet);

        mockMvc.perform(get("/api/wallets/{walletId}", "wallet-get-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(walletId))
                .andExpect(jsonPath("$.ownerId").value("user-an"))
                .andExpect(jsonPath("$.balance").value(1000000))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void should_return_404_when_wallet_does_not_exist() throws Exception {
        mockMvc.perform(get("/api/wallets/{walletId}", "missing-wallet"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("WALLET_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Wallet not found: missing-wallet"));
    }
}