package ru.sedano.db.implementations;

import java.math.BigInteger;
import java.util.Objects;

import ru.sedano.db.interfaces.Account;
import ru.sedano.db.interfaces.TransferMoney;

public class TransferMoneyImpl implements TransferMoney {
    private Account from;
    private Account to;
    private BigInteger amount;

    public TransferMoneyImpl(Account from, Account to, BigInteger amount) {
        this.from = from;
        this.to = to;
        this.amount = amount;
    }

    @Override
    public TransferResult eval() {

        if (amount.compareTo(BigInteger.ZERO) < 0) {
            return new TransferResult(false, "An attempt to transfer a negative amount of money");
        }

        if (from.equals(to)) {
            return new TransferResult(false, "Transfer between the same accounts is impossible");
        }

        if (from.getAmount().compareTo(amount) < 0) {
            return new TransferMoney.TransferResult(false, "Not enough money on account");
        }

        from.dec(amount);
        to.add(amount);
        return new TransferResult(true, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TransferMoneyImpl that = (TransferMoneyImpl) o;
        return Objects.equals(getFrom(), that.getFrom()) &&
                Objects.equals(getTo(), that.getTo()) &&
                Objects.equals(amount, that.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFrom(), getTo(), amount);
    }

    @Override
    public Account getFrom() {
        return from;
    }

    @Override
    public Account getTo() {
        return to;
    }
}
