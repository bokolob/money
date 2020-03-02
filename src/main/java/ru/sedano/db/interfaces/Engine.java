package ru.sedano.db.interfaces;

import java.math.BigInteger;
import java.util.concurrent.Future;

public interface Engine {
    Future<TransferMoney.TransferResult> transferMoney(TransferMoney operation);

    Future<Void> putMoney(PutMoney operation);

    Future<String> createAccount();

    Future<Account> lookupAccount(String name);

    Future<BigInteger> getMoneyAmount(GetMoney operation);

    void kill();
}
