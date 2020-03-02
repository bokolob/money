package ru.sedano.db.interfaces;

import java.math.BigInteger;

public interface Account {
    String getName();

    BigInteger getAmount();

    void add(BigInteger value);

    void dec(BigInteger value);
}
