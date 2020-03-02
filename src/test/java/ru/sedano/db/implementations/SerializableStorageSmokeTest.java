package ru.sedano.db.implementations;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import ru.sedano.db.interfaces.Engine;

public class SerializableStorageSmokeTest extends StorageSmokeTestBase {

    private static Engine engine;

    @BeforeClass
    public static void init() {
        engine = new SerializableStorage(new AccountStorageImpl());
    }

    @AfterClass
    public static void finish() {
        engine.kill();
    }

    @Override
    protected Engine getEngine() {
        return engine;
    }
}