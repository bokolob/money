package ru.sedano.db.interfaces;

import java.math.BigInteger;

public interface GetMoney extends Operation<BigInteger> {
    Account getAccount();
}
