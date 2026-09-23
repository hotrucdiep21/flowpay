package com.flowpay.wallet.application;

import com.flowpay.wallet.domain.Money;

public record CreateWalletCommand(String ownerId,
                                  Money initialBalance) {

}
