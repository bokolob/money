package ru.sedano.db.implementations;

import java.math.BigInteger;
import java.util.Objects;

import ru.sedano.db.interfaces.Account;
import ru.sedano.db.interfaces.GetMoney;

public class GetMoneyImpl implements GetMoney {
    private Account account;

    public GetMoneyImpl(Account account) {
        this.account = account;
    }

    @Override
    public BigInteger eval() {
        return account.getAmount();
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
        GetMoneyImpl getMoney = (GetMoneyImpl) o;
        return Objects.equals(getAccount(), getMoney.getAccount());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAccount());
    }
}
