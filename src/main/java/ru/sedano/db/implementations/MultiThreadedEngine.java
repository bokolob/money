package ru.sedano.db.implementations;

import java.math.BigInteger;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import ru.sedano.db.interfaces.Account;
import ru.sedano.db.interfaces.AccountStorage;
import ru.sedano.db.interfaces.Engine;
import ru.sedano.db.interfaces.GetMoney;
import ru.sedano.db.interfaces.PutMoney;
import ru.sedano.db.interfaces.TransferMoney;

public class MultiThreadedEngine implements Engine {
    private final ExecutorService executorService;
    private final AccountStorage storage;

    public MultiThreadedEngine(AccountStorage storage) {
        executorService = Executors.newCachedThreadPool();
        this.storage = storage;
    }

    @Override
    public Future<TransferMoney.TransferResult> transferMoney(TransferMoney operation) {
        return executorService.submit(() -> doTransfer(operation));
    }

    private TransferMoney.TransferResult doTransfer(TransferMoney operation) {
        Account first;
        Account second;

        if (operation.getFrom().getName().compareTo(operation.getTo().getName()) < 0) {
            first = operation.getFrom();
            second = operation.getTo();
        } else {
            first = operation.getTo();
            second = operation.getFrom();
        }

        try {
            ((LockableAccount) first).lockWrite();
            ((LockableAccount) second).lockWrite();
            return operation.eval();
        } finally {
            ((LockableAccount) first).unlockWrite();
            ((LockableAccount) second).unlockWrite();
        }
    }

    @Override
    public Future<Void> putMoney(PutMoney operation) {
        return executorService.submit(() -> doPut(operation));
    }

    private Void doPut(PutMoney operation) {
        try {
            ((LockableAccount) operation.getAccount()).lockWrite();
            operation.eval();
        } finally {
            ((LockableAccount) operation.getAccount()).unlockWrite();
        }

        return null;
    }

    @Override
    public Future<String> createAccount() {
        return executorService.submit(this::doAddAcc);
    }

    private String doAddAcc() {
        synchronized (storage) {
            while (true) {
                String tmpName = UUID.randomUUID().toString();
                if (storage.getAccount(tmpName) == null) {
                    Account account = new LockableAccount(tmpName);
                    storage.addAccount(account);
                    return tmpName;
                }
            }
        }
    }

    @Override
    public Future<Account> lookupAccount(String name) {
        Account found = storage.getAccount(name);

        return new Future<>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return true;
            }

            @Override
            public Account get() {
                return found;
            }

            @Override
            public Account get(long timeout, TimeUnit unit)
            {
                return found;
            }
        };
    }

    @Override
    public Future<BigInteger> getMoneyAmount(GetMoney operation) {
        return executorService.submit(() -> doGet(operation));
    }

    private BigInteger doGet(GetMoney operation) {
        try {
            ((LockableAccount) operation.getAccount()).lockRead();
            return operation.eval();
        } finally {
            ((LockableAccount) operation.getAccount()).unlockRead();
        }
    }

    @Override
    public void kill() {
        executorService.shutdown();
        try {
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
