package ru.sedano.db.implementations;

import java.math.BigInteger;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import ru.sedano.db.interfaces.Account;

public class SimpleAccount implements Account {
    private AtomicReference<BigInteger> amountHolder; //TODO replace with BigDecimal
    private String name;

    public SimpleAccount(String name) {
        this.name = name;
        this.amountHolder = new AtomicReference<>(BigInteger.ZERO);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public BigInteger getAmount() {
        return amountHolder.get();
    }

    @Override
    public void add(BigInteger value) {
        amountHolder.updateAndGet(v -> v.add(value));
    }

    @Override
    public void dec(BigInteger value) {
        amountHolder.updateAndGet(v -> v.subtract(value));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SimpleAccount that = (SimpleAccount) o;
        return Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}
