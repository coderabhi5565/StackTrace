package com.stacktrace.comment_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Runs best-effort side effects asynchronously after the surrounding transaction commits. */
@Slf4j
public final class AfterCommit {

    private static final ExecutorService EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    private AfterCommit() {
    }

    public static void runAsync(String what, Runnable task) {
        Runnable safe = () -> {
            try {
                task.run();
            } catch (RuntimeException e) {
                log.warn("Best-effort task '{}' failed: {}", what, e.getMessage());
            }
        };
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    EXECUTOR.submit(safe);
                }
            });
        } else {
            EXECUTOR.submit(safe);
        }
    }
}
