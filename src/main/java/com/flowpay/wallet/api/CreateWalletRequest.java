package com.flowpay.wallet.api;

import java.math.BigDecimal;

public record CreateWalletRequest(String ownerId,
                                  BigDecimal initialBalance) {

}
