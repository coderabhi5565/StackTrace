package com.stacktrace.post_service.config;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Runs best-effort side effects (e.g. awarding points) asynchronously once the transaction commits. */
public final class AfterCommit {

    private static final ExecutorService EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    private AfterCommit() {
    }

    public static void runAsync(Runnable task) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    EXECUTOR.submit(task);
                }
            });
        } else {
            EXECUTOR.submit(task);
        }
    }
}
