package ru.sedano;

import java.math.BigInteger;
import java.net.URI;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import ru.sedano.db.implementations.GetMoneyImpl;
import ru.sedano.db.implementations.PutMoneyImpl;
import ru.sedano.db.implementations.SimpleAccount;
import ru.sedano.db.implementations.TransferMoneyImpl;
import ru.sedano.db.interfaces.Account;
import ru.sedano.db.interfaces.AccountStorage;
import ru.sedano.db.interfaces.Engine;
import ru.sedano.db.interfaces.GetMoney;
import ru.sedano.db.interfaces.PutMoney;
import ru.sedano.db.interfaces.TransferMoney;
import ru.sedano.http.exceptions.NotFoundException;
import ru.sedano.http.interfaces.HTTPResponse;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ApplicationTest {

    private Engine engine;
    private Application application;

    @Before
    public void init() {
        // accountStorage = new AccountStorageImpl();
        // engine = new MultiThreadedEngine(accountStorage);
        engine = mock(Engine.class);
        application = new Application(engine);
    }

    @Test
    public void routeAddAccountTest() {
        when(engine.createAccount()).thenReturn(new MyFuture<>("xxx-xxx-xxx"));
        HTTPResponse response = application.route("POST", URI.create("/account"), Collections.emptyMap());

        assertThat(response, Matchers.notNullValue());
        assertThat(response.getStatus(), is(200));
        assertThat(response.getContent(), is("xxx-xxx-xxx"));
        verify(engine, times(1)).createAccount();
    }

    @Test
    public void routePutMoneyTest() {
        Account account = new SimpleAccount("1234-abc00-1234");
        account.add(new BigInteger("1000"));

        when(engine.lookupAccount("1234-abc00-1234")).thenReturn(new MyFuture<>(account));

        PutMoney putMoney = new PutMoneyImpl(account, new BigInteger("100"));

        when(engine.putMoney(putMoney)).thenReturn(new MyFuture<>(null));

        HTTPResponse response = application.route("PUT", URI.create("/account/1234-abc00-1234"),
                Collections.singletonMap("amount", Collections.singletonList("100")));

        verify(engine, times(1)).putMoney(Mockito.any());

        assertThat(response, Matchers.notNullValue());
        assertThat(response.getStatus(), is(200));
        assertThat(response.getContent(), is(""));
    }

    @Test
    public void routeGetMoneyTest() {
        Account account = new SimpleAccount("1234-abc00-1234");
        account.add(new BigInteger("1000000"));
        when(engine.lookupAccount("1234-abc00-1234")).thenReturn(new MyFuture<>(account));

        GetMoney getMoney = new GetMoneyImpl(account);

        when(engine.getMoneyAmount(getMoney)).thenReturn(new MyFuture<>(account.getAmount()));

        HTTPResponse response = application.route("GET", URI.create("/account/1234-abc00-1234"),
                Collections.emptyMap());

        assertThat(response, Matchers.notNullValue());
        assertThat(response.getStatus(), is(200));
        assertThat(response.getContent(), is("1000000"));
        verify(engine, times(1)).getMoneyAmount(Mockito.any());
    }

    @Test
    public void routeTransferMoney() {
        Account from = new SimpleAccount("1234-abc00-1234");
        from.add(new BigInteger("1000000"));

        Account to = new SimpleAccount("222-333");

        when(engine.lookupAccount("1234-abc00-1234")).thenReturn(new MyFuture<>(from));
        when(engine.lookupAccount("222-333")).thenReturn(new MyFuture<>(to));

        BigInteger amount = new BigInteger("987");

        TransferMoney transferMoney = new TransferMoneyImpl(from, to, amount);
        when(engine.transferMoney(transferMoney))
                .thenReturn(new MyFuture<>(new TransferMoney.TransferResult(true, "")));

        HTTPResponse response = application.route("PATCH", URI.create("/transfer/1234-abc00-1234/222-333"),
                Collections.singletonMap("amount", Collections.singletonList("987")));

        assertThat(response, Matchers.notNullValue());
        assertThat(response.getStatus(), is(200));
        assertThat(response.getContent(), is(""));
        verify(engine, times(1)).transferMoney(Mockito.any());
    }

    @Test
    public void routeTransferMoneyFailed() {
        Account from = new SimpleAccount("1234-abc00-1234");
        from.add(new BigInteger("1000000"));

        Account to = new SimpleAccount("222-333");

        when(engine.lookupAccount("1234-abc00-1234")).thenReturn(new MyFuture<>(from));
        when(engine.lookupAccount("222-333")).thenReturn(new MyFuture<>(to));

        BigInteger amount = new BigInteger("987");

        TransferMoney transferMoney = new TransferMoneyImpl(from, to, amount);
        when(engine.transferMoney(transferMoney))
                .thenReturn(new MyFuture<>(new TransferMoney.TransferResult(false, "Something has been going wrong")));

        HTTPResponse response = application.route("PATCH", URI.create("/transfer/1234-abc00-1234/222-333"),
                Collections.singletonMap("amount", Collections.singletonList("987")));

        assertThat(response, Matchers.notNullValue());
        assertThat(response.getStatus(), is(400));
        assertThat(response.getContent(), Matchers.not(""));
        verify(engine, times(1)).transferMoney(Mockito.any());
    }

    @Test
    public void routeTransferMoneyToFakeAccFailed() {
        Account from = new SimpleAccount("1234-abc00-1234");
        from.add(new BigInteger("1000000"));

        when(engine.lookupAccount("1234-abc00-1234")).thenReturn(new MyFuture<>(from));
        when(engine.lookupAccount("222-333")).thenReturn(new MyFuture<>(null));

        boolean caught = false;

        try {
            application.route("PATCH", URI.create("/transfer/1234-abc00-1234/222-333"),
                    Collections.singletonMap("amount", Collections.singletonList("987")));
        } catch (NotFoundException e) {
            caught = true;
        }

        assertTrue(caught);
        verify(engine, times(0)).transferMoney(Mockito.any());
    }


    public static class MyFuture<T> implements Future<T> {
        private T value;

        public MyFuture(T value) {
            this.value = value;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public T get(long timeout, TimeUnit unit) {
            return value;
        }
    }

}