package ru.sedano.db.implementations;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LockableAccount extends SimpleAccount {
    private ReadWriteLock rwLock = new ReentrantReadWriteLock();

    public LockableAccount(String name) {
        super(name);
    }

    public void lockRead() {
        rwLock.readLock().lock();
    }

    public void lockWrite() {
        rwLock.writeLock().lock();
    }

    public void unlockRead() {
        rwLock.readLock().unlock();
    }

    public void unlockWrite() {
        rwLock.writeLock().unlock();
    }
}
