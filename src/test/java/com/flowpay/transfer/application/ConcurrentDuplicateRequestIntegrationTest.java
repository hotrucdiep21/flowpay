package com.flowpay.transfer.application;

import com.flowpay.support.PostgresTestContainerConfig;
import com.flowpay.transfer.domain.Transfer;
import com.flowpay.transfer.domain.exception.DuplicateTransferException;
import com.flowpay.transfer.infrastructure.persistence.jpa.JpaTransferRepositoryAdapter;
import com.flowpay.transfer.infrastructure.persistence.jpa.SpringDataTransferRepository;
import com.flowpay.wallet.domain.Money;
import com.flowpay.wallet.domain.WalletStatus;
import com.flowpay.wallet.infrastructure.persistence.jpa.SpringDataWalletRepository;
import com.flowpay.wallet.infrastructure.persistence.jpa.WalletJpaEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

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
@Import(PostgresTestContainerConfig.class)
class ConcurrentDuplicateRequestIntegrationTest {

    private static final String REQUEST_ID =
            "same-request-different-wallets";

    @Autowired
    private TransferService transferService;

    @Autowired
    private SpringDataWalletRepository walletRepository;

    @Autowired
    private SpringDataTransferRepository transferRepository;

    @MockitoSpyBean
    private JpaTransferRepositoryAdapter transferRepositoryAdapter;

    private ExecutorService executor;
    private CyclicBarrier duplicateCheckBarrier;

    @BeforeEach
    void setUp() {
        transferRepository.deleteAllInBatch();
        walletRepository.deleteAllInBatch();

        walletRepository.saveAllAndFlush(List.of(
                wallet("sender-one", "user-one", "500000"),
                wallet("receiver-one", "receiver-user-one", "0"),
                wallet("sender-two", "user-two", "500000"),
                wallet("receiver-two", "receiver-user-two", "0")
        ));

        executor = Executors.newFixedThreadPool(2);
        duplicateCheckBarrier = new CyclicBarrier(2);

        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Optional<Transfer> existingTransfer =
                    (Optional<Transfer>) invocation.callRealMethod();

            String requestId = invocation.getArgument(0);

            if (REQUEST_ID.equals(requestId)) {
                try {
                    duplicateCheckBarrier.await(
                            5,
                            TimeUnit.SECONDS
                    );
                } catch (Exception exception) {
                    throw new IllegalStateException(
                            "Could not synchronize duplicate checks",
                            exception
                    );
                }
            }

            return existingTransfer;
        }).when(transferRepositoryAdapter)
                .findByRequestId(anyString());
    }

    @AfterEach
    void tearDown() {
        executor.shutdownNow();
    }

    @Test
    void should_translate_concurrent_unique_constraint_violation_to_duplicate_transfer()
            throws Exception {

        TransferCommand firstCommand = new TransferCommand(
                REQUEST_ID,
                "sender-one",
                "receiver-one",
                new Money(new BigDecimal("100000"))
        );

        TransferCommand secondCommand = new TransferCommand(
                REQUEST_ID,
                "sender-two",
                "receiver-two",
                new Money(new BigDecimal("100000"))
        );

        Future<Transfer> firstResult = executor.submit(
                () -> transferService.transfer(firstCommand)
        );

        Future<Transfer> secondResult = executor.submit(
                () -> transferService.transfer(secondCommand)
        );

        int succeededCount = 0;
        int failedCount = 0;
        Throwable failedException = null;

        for (Future<Transfer> result :
                List.of(firstResult, secondResult)) {

            try {
                result.get(10, TimeUnit.SECONDS);
                succeededCount++;
            } catch (ExecutionException exception) {
                failedCount++;
                failedException = exception;
            }
        }

        assertEquals(1, succeededCount);
        assertEquals(1, failedCount);
        assertEquals(1L, transferRepository.count());

        BigDecimal totalSenderBalance =
                balanceOf("sender-one")
                        .add(balanceOf("sender-two"));

        BigDecimal totalReceiverBalance =
                balanceOf("receiver-one")
                        .add(balanceOf("receiver-two"));

        assertMoneyEquals("900000", totalSenderBalance);
        assertMoneyEquals("100000", totalReceiverBalance);

        assertTrue(
                hasCause(
                        failedException,
                        DuplicateTransferException.class
                ),
                "Database unique violation should become DuplicateTransferException"
        );
    }

    private WalletJpaEntity wallet(
            String id,
            String ownerId,
            String balance
    ) {
        return new WalletJpaEntity(
                id,
                ownerId,
                new BigDecimal(balance),
                WalletStatus.ACTIVE
        );
    }

    private BigDecimal balanceOf(String walletId) {
        return walletRepository
                .findById(walletId)
                .orElseThrow()
                .getBalance();
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