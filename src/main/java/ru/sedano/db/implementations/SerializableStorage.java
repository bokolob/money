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

public class SerializableStorage implements Engine {
    private final ExecutorService executorService;
    private final AccountStorage storage;

    public SerializableStorage(AccountStorage storage) {
        executorService = Executors.newSingleThreadExecutor();
        this.storage = storage;
    }

    @Override
    public Future<TransferMoney.TransferResult> transferMoney(TransferMoney operation) {
        return executorService.submit(operation::eval);
    }

    @Override
    public Future<Void> putMoney(PutMoney operation) {
        return executorService.submit(operation::eval);
    }

    @Override
    public Future<String> createAccount() {
        return executorService.submit(this::doCreateAcc);
    }

    @Override
    public Future<Account> lookupAccount(String name) {
        return executorService.submit(() -> storage.getAccount(name));
    }

    private String doCreateAcc() {
        while (true) {
            String tmpName = UUID.randomUUID().toString();
            if (storage.getAccount(tmpName) == null) {
                Account account = new SimpleAccount(tmpName);
                storage.addAccount(account);
                return tmpName;
            }
        }
    }

    @Override
    public Future<BigInteger> getMoneyAmount(GetMoney operation) {
        return executorService.submit(operation::eval);
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
