package ru.sedano;

import java.io.IOException;

import ru.sedano.db.implementations.AccountStorageImpl;
import ru.sedano.db.implementations.SerializableStorage;
import ru.sedano.db.interfaces.Engine;
import ru.sedano.http.RestApplication;
import ru.sedano.http.implementations.JDKHttpServer;
import ru.sedano.http.interfaces.HttpServer;

public class Main {

    public static void main(String[] args) throws IOException {
        Engine db = new SerializableStorage(new AccountStorageImpl());
        RestApplication application = new Application(db);
        HttpServer httpServer = new JDKHttpServer(8080, application);
        httpServer.start();
    }
}
