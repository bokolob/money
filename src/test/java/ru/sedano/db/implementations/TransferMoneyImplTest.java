package ru.sedano.db.implementations;

import java.math.BigInteger;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import ru.sedano.db.interfaces.Account;
import ru.sedano.db.interfaces.TransferMoney;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class TransferMoneyImplTest {

    @Test
    public void transferOkTest() {
        Account from = new SimpleAccount("from");
        Account to = new SimpleAccount("to");
        BigInteger amount = new BigInteger("123");

        from.add(amount);

        TransferMoney op = new TransferMoneyImpl(from, to, new BigInteger("100"));
        TransferMoney.TransferResult result = op.eval();

        assertThat(result.isOk(), CoreMatchers.is(true));
        assertThat(from.getAmount(), CoreMatchers.is(new BigInteger("23")));
        assertThat(to.getAmount(), CoreMatchers.is(new BigInteger("100")));
    }

    @Test
    public void transferToSameAccountTest() {
        Account from = new SimpleAccount("from");
        BigInteger amount = new BigInteger("123");
        from.add(amount);

        TransferMoney op = new TransferMoneyImpl(from, from, new BigInteger("100"));

        TransferMoney.TransferResult result = op.eval();

        assertThat(result.isOk(), is(false));
        assertThat(result.getError(), CoreMatchers.notNullValue());
    }

    @Test
    public void notEnoughMoneyTest() {
        Account from = new SimpleAccount("from");
        Account to = new SimpleAccount("to");
        BigInteger amount = new BigInteger("123");
        from.add(amount);

        TransferMoney op = new TransferMoneyImpl(from, to, new BigInteger("200"));
        TransferMoney.TransferResult result = op.eval();

        assertThat(result.isOk(), is(false));
        assertThat(result.getError(), CoreMatchers.notNullValue());
    }
    @Test
    public void negativeMoneyTransferTest() {
        Account from = new SimpleAccount("from");
        Account to = new SimpleAccount("to");
        BigInteger amount = new BigInteger("123");
        from.add(amount);

        TransferMoney op = new TransferMoneyImpl(from, to, new BigInteger("-200"));
        TransferMoney.TransferResult result = op.eval();

        assertThat(result.isOk(), is(false));
        assertThat(result.getError(), CoreMatchers.notNullValue());
    }
}