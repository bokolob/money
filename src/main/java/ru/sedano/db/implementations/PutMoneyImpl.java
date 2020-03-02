package ru.sedano.db.implementations;

import java.math.BigInteger;
import java.util.Objects;

import ru.sedano.db.interfaces.Account;
import ru.sedano.db.interfaces.PutMoney;

public class PutMoneyImpl implements PutMoney {
    private Account account;
    private BigInteger amount;

    public PutMoneyImpl(Account account, BigInteger amount) {
        this.account = account;
        this.amount = amount;
    }

    @Override
    public Void eval() {
        account.add(amount);
        return null;
    }

    @Override
    public Account getAccount() {
        return account;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PutMoneyImpl putMoney = (PutMoneyImpl) o;
        return Objects.equals(getAccount(), putMoney.getAccount()) &&
                Objects.equals(amount, putMoney.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAccount(), amount);
    }
}
