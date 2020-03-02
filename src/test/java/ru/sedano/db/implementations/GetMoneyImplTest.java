package ru.sedano.db.implementations;

import java.math.BigInteger;

import org.junit.Test;
import ru.sedano.db.interfaces.Account;
import ru.sedano.db.interfaces.GetMoney;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GetMoneyImplTest {

    @Test
    public void getMoneyTest() {
        Account account = mock(Account.class);
        BigInteger amount = new BigInteger("345000");
        when(account.getAmount()).thenReturn(amount);
        GetMoney getMoney = new GetMoneyImpl(account);

        BigInteger result = getMoney.eval();

        assertEquals(amount, result);
    }

}