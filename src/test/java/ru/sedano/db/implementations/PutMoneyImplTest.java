package ru.sedano.db.implementations;

import java.math.BigInteger;

import org.junit.Test;
import ru.sedano.db.interfaces.Account;
import ru.sedano.db.interfaces.PutMoney;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class PutMoneyImplTest {

    @Test
    public void puttingMoneyTest() {
        Account account = mock(Account.class);
        BigInteger amount = new BigInteger("12345");

        PutMoney putMoney = new PutMoneyImpl(account, amount);
        putMoney.eval();

        verify(account, times(1)).add(amount);
    }

}