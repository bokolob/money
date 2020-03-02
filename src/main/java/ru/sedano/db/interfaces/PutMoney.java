package ru.sedano.db.interfaces;

public interface PutMoney extends Operation<Void> {
    Account getAccount();
}
