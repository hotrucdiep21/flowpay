package com.flowpay.wallet.domain;

import org.junit.jupiter.api.Test;


import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class WalletTest {
    @Test
    void should_create_active_wallet_with_initial_balance() {
        Money initialBalance = new Money(new BigDecimal("1000000"));

        Wallet wallet = new Wallet("wallet-an", "user-an", initialBalance);
        assertEquals("wallet-an", wallet.getId());
        assertEquals("user-an", wallet.getOwnerId());
        assertEquals(new BigDecimal("1000000"), wallet.getBalance().getAmount());
        assertEquals(WalletStatus.ACTIVE, wallet.getStatus());
    }

    @Test
    void should_reject_blank_wallet_id() {
        Money initialBalance = new Money(new BigDecimal("1000000"));

        assertThrows(IllegalArgumentException.class, () -> new Wallet(" ", "user-an", initialBalance));
    }

    @Test
    void should_reject_blank_owner_id() {
        Money initialBalance = new Money(new BigDecimal("100000"));

        assertThrows(IllegalArgumentException.class, () -> new Wallet("wallet-id", " ", initialBalance));
    }

    @Test
    void should_reject_null_initial_balance() {
        assertThrows(IllegalArgumentException.class, () -> new Wallet("wallet-id", "user-id", null));
    }

    @Test
    void should_credit_money_to_active_wallet() {
        Wallet wallet = new Wallet("wallet-an", "user-an", new Money(new BigDecimal("1000000")));
        Money creditAmount = new Money(new BigDecimal("200000"));

        wallet.credit(creditAmount);

        assertEquals(new BigDecimal("1200000"), wallet.getBalance().getAmount());

    }

    @Test
    void should_reject_null_credit_amount() {
        Wallet wallet = new Wallet("wallet-an", "user-an", new Money(new BigDecimal("1000000")));

        assertThrows(IllegalArgumentException.class, () -> wallet.credit(null));
    }

    @Test
    void should_reject_credit_when_wallet_is_blocked() {
        Wallet wallet = new Wallet("wallet-an", "user-an", new Money(new BigDecimal("100000")));
        wallet.block();
        assertThrows(IllegalStateException.class, () -> wallet.credit(new Money(new BigDecimal("100000"))));

        assertEquals(new BigDecimal("100000"), wallet.getBalance().getAmount());
    }

    @Test
    void should_reject_credit_when_wallet_is_closed() {
        Wallet wallet = new Wallet("wallet-an", "user-an", new Money(new BigDecimal("1000000")));
        wallet.close();

        assertThrows(IllegalStateException.class, () -> wallet.credit(new Money(new BigDecimal("1000000"))));

        assertEquals(new BigDecimal("1000000"), wallet.getBalance().getAmount());
    }

    @Test
    void should_debit_money_from_active_wallet() {
        Wallet wallet = new Wallet("wallet-an", "user-an", new Money(new BigDecimal("1000000")));

        wallet.debit(new Money(new BigDecimal("300000")));

        assertEquals(new BigDecimal("700000"), wallet.getBalance().getAmount());
    }

    @Test
    void should_reject_debit_when_balance_is_insufficient() {
        Wallet wallet = new Wallet("wallet-an", "user-an", new Money(new BigDecimal("100000")));

        assertThrows(IllegalStateException.class, () -> wallet.debit(new Money(new BigDecimal("200000"))));

        assertEquals(new BigDecimal("100000"), wallet.getBalance().getAmount());
    }

    @Test
    void should_reject_null_debit_amount() {
        Wallet wallet = new Wallet("wallet-an", "user-an", new Money(new BigDecimal("100000")));

        assertThrows(IllegalArgumentException.class, () -> wallet.debit(null));
    }

    @Test
    void should_reject_zero_debit_amount() {
        Wallet wallet = new Wallet("wallet-an", "user-id", new Money(BigDecimal.ZERO));

        assertThrows(IllegalArgumentException.class, () -> wallet.debit(new Money(BigDecimal.ZERO)));
    }

    @Test
    void should_reject_debit_when_wallet_is_blocked() {
        Wallet wallet = new Wallet("wallet-an", "user-an", new Money(new BigDecimal("100000")));
        wallet.block();

        assertThrows(IllegalStateException.class, () -> wallet.debit(new Money(new BigDecimal("50000"))));
        assertEquals(new BigDecimal("100000"), wallet.getBalance().getAmount());
    }
}
