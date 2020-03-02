package ru.sedano.db.implementations;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ru.sedano.db.interfaces.Account;
import ru.sedano.db.interfaces.AccountStorage;

public class AccountStorageImpl implements AccountStorage {
    Map<String, Account> internalStorage;

    public AccountStorageImpl() {
        this.internalStorage = new ConcurrentHashMap<>();
    }

    @Override
    public Account getAccount(String name) {
        return internalStorage.get(name);
    }

    @Override
    public void addAccount(Account account) {
        internalStorage.put(account.getName(), account);
    }
}
