package ru.sedano.db.implementations;

import java.math.BigInteger;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.sedano.db.interfaces.Account;
import ru.sedano.db.interfaces.AccountStorage;
import ru.sedano.db.interfaces.Engine;
import ru.sedano.db.interfaces.TransferMoney;

import static org.junit.Assert.assertFalse;

public class ConcurrentStorageSmokeTest extends StorageSmokeTestBase {

    private static Engine engine;
    private static AccountStorage accountStorage;

    @BeforeClass
    public static void init()
    {
        accountStorage = new AccountStorageImpl();
        engine = new MultiThreadedEngine(accountStorage);
    }

    @AfterClass
    public static void finish() {
        engine.kill();
    }

    @Override
    protected Engine getEngine() {
        return engine;
    }

    @Test
    public void transferDeadLockTest() throws ExecutionException, InterruptedException {
        Account first = new SlowLocksAccount(UUID.randomUUID().toString(), 100);
        Account second = new SlowLocksAccount(UUID.randomUUID().toString(), 50);

        accountStorage.addAccount(first);
        accountStorage.addAccount(second);

        BigInteger amount = new BigInteger("500");

        TransferMoney transferMoneyFirst = new TransferMoneyImpl(first, second, amount);
        TransferMoney transferMoneySecond = new TransferMoneyImpl(second, first, amount);

        Future<TransferMoney.TransferResult> resultFirst = engine.transferMoney(transferMoneyFirst);
        Future<TransferMoney.TransferResult> resultSecond = engine.transferMoney(transferMoneySecond);

        boolean deadlock = false;

        try {
            resultFirst.get(5, TimeUnit.SECONDS);
            resultSecond.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            deadlock = true;
        }

        assertFalse(deadlock);
    }

    public static class SlowLocksAccount extends LockableAccount {
        private long millis;

        public SlowLocksAccount(String name, long millis) {
            super(name);
            this.millis = millis;
        }

        @Override
        public void lockWrite() {
            super.lockWrite();

            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}