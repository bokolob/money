package ru.sedano.db.interfaces;

public interface AccountStorage {
    Account getAccount(String name);

    void addAccount(Account account); // race condition?
}
