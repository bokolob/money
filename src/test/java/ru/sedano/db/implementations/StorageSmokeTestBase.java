package ru.sedano.db.implementations;

import java.math.BigInteger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import ru.sedano.db.interfaces.Account;
import ru.sedano.db.interfaces.Engine;
import ru.sedano.db.interfaces.PutMoney;
import ru.sedano.db.interfaces.TransferMoney;

import static org.junit.Assert.assertThat;

public abstract class StorageSmokeTestBase {
    protected abstract Engine getEngine();

    private Account makeAccount() throws ExecutionException, InterruptedException {
        return getEngine().lookupAccount(getEngine().createAccount().get()).get();
    }

    @Test
    public void transferMoney() throws ExecutionException, InterruptedException {
        Account from = makeAccount();
        Account to = makeAccount();
        BigInteger amount = new BigInteger("123");
        from.add(amount);

        TransferMoney op = new TransferMoneyImpl(from, to, new BigInteger("100"));

        Future<TransferMoney.TransferResult> resultFuture = getEngine().transferMoney(op);

        TransferMoney.TransferResult result = resultFuture.get();

        assertThat(result.isOk(), CoreMatchers.is(true));
        assertThat(from.getAmount(), CoreMatchers.is(new BigInteger("23")));
        assertThat(to.getAmount(), CoreMatchers.is(new BigInteger("100")));
    }

    @Test
    public void putMoney() throws ExecutionException, InterruptedException {
        Account account = makeAccount();

        PutMoney op = new PutMoneyImpl(account, new BigInteger("123456789"));
        Future<Void> resultFuture = getEngine().putMoney(op);

        resultFuture.get();

        assertThat(account.getAmount(), CoreMatchers.equalTo(new BigInteger("123456789")));
    }

    @Test
    public void getMoneyAmount() throws ExecutionException, InterruptedException {
        Account account = makeAccount();
        account.add(new BigInteger("200"));

        Future<BigInteger> resultFuture = getEngine().getMoneyAmount(new GetMoneyImpl(account));

        BigInteger result = resultFuture.get();

        assertThat(result, CoreMatchers.equalTo(new BigInteger("200")));
    }


}
