package com.flowpay.wallet.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MoneyTest {
    @Test
    void should_create_money_with_valid_amount() {
        Money money = new Money(new BigDecimal("1000000"));

        assertEquals(new BigDecimal("1000000"), money.getAmount());
    }

    @Test
    void should_reject_negative_amount() {
        assertThrows(IllegalArgumentException.class, () -> new Money(new BigDecimal("-1000")));
    }

    @Test
    void should_add_two_money_values() {
        Money first = new Money(new BigDecimal("100000"));
        Money second = new Money(new BigDecimal("50000"));

        Money result = first.add(second);

        assertEquals(new BigDecimal("150000"), result.getAmount());
    }

    @Test
    void should_subtract_two_money_values() {
        Money first = new Money(new BigDecimal("100000"));
        Money second = new Money(new BigDecimal("40000"));

        Money result = first.subtract(second);

        assertEquals(new BigDecimal("60000"), result.getAmount());

    }

    @Test
    void should_reject_subtraction_resulting_in_negative() {
        Money first = new Money(new BigDecimal("40000"));
        Money second = new Money(new BigDecimal("100000"));

        assertThrows(IllegalArgumentException.class, () -> first.subtract(second));
    }

    @Test
    void should_reject_null_amount() {
        assertThrows(IllegalArgumentException.class, ()->new Money(null));
    }
}
