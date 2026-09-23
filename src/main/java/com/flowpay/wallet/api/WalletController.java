package com.flowpay.wallet.api;

import com.flowpay.wallet.application.CreateWalletCommand;
import com.flowpay.wallet.application.WalletService;
import com.flowpay.wallet.domain.Money;
import com.flowpay.wallet.domain.Wallet;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("api/wallets")
public class WalletController {
    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(@RequestBody CreateWalletRequest request) {
        CreateWalletCommand command = new CreateWalletCommand(
                request.ownerId(),
                new Money(request.initialBalance())
        );

        Wallet wallet = walletService.createWallet(command);

        WalletResponse response = WalletResponse.from(wallet);

        URI location = URI.create("/api/wallets/" + wallet.getId());

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{walletId}")
    public WalletResponse getWallet(@PathVariable String walletId) {
        Wallet wallet = walletService.getWallet(walletId);
        return WalletResponse.from(wallet);
    }
}
