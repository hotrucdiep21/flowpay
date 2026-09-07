package com.flowpay.wallet.domain;

import java.math.BigDecimal;

public final class Money {
    private final BigDecimal amount;

    public Money(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount must not be null!");
        }

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must not be negative!");
        }

        this.amount = amount;
    }

    private static void validateOther(Money other) {
        if(other == null) {
            throw new IllegalArgumentException("Money must not be null!");
        }
    }

    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isLessThan(Money other) {
        validateOther(other);
        return this.amount.compareTo(other.amount) < 0;
    }

    public Money add(Money other) {
        validateOther(other);
        return new Money(this.amount.add(other.amount));
    }

    public Money subtract(Money other) {
        validateOther(other);
        return new Money(this.amount.subtract(other.amount));
    }

    public BigDecimal getAmount() {
        return amount;
    }


}
