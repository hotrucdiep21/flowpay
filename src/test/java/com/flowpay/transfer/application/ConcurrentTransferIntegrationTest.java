package com.flowpay.transfer.application;

import com.flowpay.transfer.domain.Transfer;
import com.flowpay.transfer.infrastructure.persistence.jpa.SpringDataTransferRepository;
import com.flowpay.wallet.domain.Money;
import com.flowpay.wallet.domain.Wallet;
import com.flowpay.wallet.domain.WalletStatus;
import com.flowpay.wallet.infrastructure.persistence.jpa.JpaWalletRepositoryAdapter;
import com.flowpay.wallet.infrastructure.persistence.jpa.SpringDataWalletRepository;
import com.flowpay.wallet.infrastructure.persistence.jpa.WalletJpaEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;

@SpringBootTest
@ActiveProfiles("jpa")
class ConcurrentTransferIntegrationTest {

    private static final String SENDER_ID = "wallet-concurrent-sender";
    private static final String RECEIVER_ONE_ID = "wallet-receiver-one";
    private static final String RECEIVER_TWO_ID = "wallet-receiver-two";

    @Autowired
    private TransferService transferService;

    @Autowired
    private SpringDataWalletRepository walletRepository;

    @Autowired
    private SpringDataTransferRepository transferRepository;

    @MockitoSpyBean
    private JpaWalletRepositoryAdapter walletRepositoryAdapter;

    private ExecutorService executor;
    private CyclicBarrier senderReadBarrier;

    @BeforeEach
    void setUp() {
        transferRepository.deleteAllInBatch();
        walletRepository.deleteAllInBatch();

        walletRepository.saveAllAndFlush(List.of(
                new WalletJpaEntity(
                        SENDER_ID,
                        "user-an",
                        new BigDecimal("500000"),
                        WalletStatus.ACTIVE
                ),
                new WalletJpaEntity(
                        RECEIVER_ONE_ID,
                        "user-binh",
                        BigDecimal.ZERO,
                        WalletStatus.ACTIVE
                ),
                new WalletJpaEntity(
                        RECEIVER_TWO_ID,
                        "user-cuong",
                        BigDecimal.ZERO,
                        WalletStatus.ACTIVE
                )
        ));

        executor = Executors.newFixedThreadPool(2);
        senderReadBarrier = new CyclicBarrier(2);

        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Optional<Wallet> wallet =
                    (Optional<Wallet>) invocation.callRealMethod();

            String walletId = invocation.getArgument(0);

            if (SENDER_ID.equals(walletId)) {
                try {
                    senderReadBarrier.await(5, TimeUnit.SECONDS);
                } catch (Exception exception) {
                    throw new IllegalStateException(
                            "Could not synchronize concurrent transactions",
                            exception
                    );
                }
            }

            return wallet;
        }).when(walletRepositoryAdapter).findById(anyString());
    }

    @AfterEach
    void tearDown() {
        executor.shutdownNow();
    }

    @Test
    void should_allow_only_one_transfer_when_two_requests_spend_same_balance()
            throws Exception {

        TransferCommand firstCommand = new TransferCommand(
                "concurrent-request-001",
                SENDER_ID,
                RECEIVER_ONE_ID,
                new Money(new BigDecimal("400000"))
        );

        TransferCommand secondCommand = new TransferCommand(
                "concurrent-request-002",
                SENDER_ID,
                RECEIVER_TWO_ID,
                new Money(new BigDecimal("400000"))
        );

        Future<Transfer> firstResult =
                executor.submit(() -> transferService.transfer(firstCommand));

        Future<Transfer> secondResult =
                executor.submit(() -> transferService.transfer(secondCommand));

        int succeededCount = 0;
        int failedCount = 0;

        for (Future<Transfer> result : List.of(firstResult, secondResult)) {
            try {
                result.get(10, TimeUnit.SECONDS);
                succeededCount++;
            } catch (ExecutionException exception) {
                failedCount++;

                assertTrue(
                        hasCause(
                                exception,
                                ObjectOptimisticLockingFailureException.class
                        ),
                        "Expected an optimistic locking failure"
                );
            }
        }

        assertEquals(1, succeededCount);
        assertEquals(1, failedCount);

        WalletJpaEntity sender = walletRepository
                .findById(SENDER_ID)
                .orElseThrow();

        WalletJpaEntity receiverOne = walletRepository
                .findById(RECEIVER_ONE_ID)
                .orElseThrow();

        WalletJpaEntity receiverTwo = walletRepository
                .findById(RECEIVER_TWO_ID)
                .orElseThrow();

        assertMoneyEquals("100000", sender.getBalance());

        BigDecimal totalReceiverBalance =
                receiverOne.getBalance().add(receiverTwo.getBalance());

        assertMoneyEquals("400000", totalReceiverBalance);

        assertEquals(1L, transferRepository.count());
        assertEquals(1L, sender.getVersion());
    }

    private static void assertMoneyEquals(
            String expected,
            BigDecimal actual
    ) {
        assertEquals(
                0,
                new BigDecimal(expected).compareTo(actual)
        );
    }

    private static boolean hasCause(
            Throwable throwable,
            Class<? extends Throwable> expectedType
    ) {
        Throwable current = throwable;

        while (current != null) {
            if (expectedType.isInstance(current)) {
                return true;
            }

            current = current.getCause();
        }

        return false;
    }
}