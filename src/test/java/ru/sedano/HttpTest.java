package ru.sedano;

import java.io.IOException;

import io.restassured.response.Response;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.sedano.db.implementations.AccountStorageImpl;
import ru.sedano.db.implementations.SerializableStorage;
import ru.sedano.db.interfaces.Engine;
import ru.sedano.http.RestApplication;
import ru.sedano.http.implementations.JDKHttpServer;
import ru.sedano.http.interfaces.HttpServer;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.patch;
import static io.restassured.RestAssured.post;
import static io.restassured.RestAssured.put;
import static org.junit.Assert.assertEquals;

public class HttpTest {

    static final int PORT = 9999;
    static final String URI = "http://localhost:" + PORT + "/";
    static Thread server;
    static HttpServer httpServer;

    @BeforeClass
    public static void initServer() {
        server = new Thread(HttpTest::startServer);
        server.start();

    }

    @AfterClass
    public static void cleanup() {
        if (httpServer != null) {
            httpServer.stop();
        }
    }

    private static void startServer() {
        Engine db = new SerializableStorage(new AccountStorageImpl());
        RestApplication application = new Application(db);
        httpServer = null;

        try {
            httpServer = new JDKHttpServer(PORT, application);
        } catch (IOException e) {
            e.printStackTrace();
        }

        httpServer.start();
    }

    @Test
    public void createAcc() {
        Response createResponse = post(URI + "account");

        assertEquals(createResponse.getStatusCode(), 200);
        String acc = createResponse.getBody().asString();

        Response putMoneyResponse = put(URI + "account/" + acc + "?amount=10003");

        assertEquals(putMoneyResponse.getStatusCode(), 200);

        Response getMoneyResponse = get(URI + "account/" + acc);
        assertEquals(getMoneyResponse.getStatusCode(), 200);
        assertEquals(getMoneyResponse.getBody().asString(), "10003");
    }

    @Test
    public void transferTest() {
        Response createResponse = post(URI + "account");
        String accA = createResponse.getBody().asString();

        createResponse = post(URI + "account");
        String accB = createResponse.getBody().asString();

        put(URI + "account/" + accA + "?amount=10003");
        put(URI + "account/" + accB + "?amount=400");

        patch(URI + "transfer/" + accA + "/" + accB + "?amount=999");

        Response newAmount = get(URI + "account/" + accA);
        assertEquals(newAmount.getBody().asString(), "9004");

        newAmount = get(URI + "account/" + accB);
        assertEquals(newAmount.getBody().asString(), "1399");
    }

    @Test
    public void putOnUnknownAccTest() {
        Response response = put(URI + "account/an-unknown-acc?amount=10003");
        assertEquals(response.getStatusCode(), 404);
    }

    @Test
    public void transferOnUnknownAccTest() {
        Response response = patch(URI + "transfer/unk-acc/next-unk-acc?amount=999");
        assertEquals(response.getStatusCode(), 404);
    }

    @Test
    public void unparsableParams() {
        Response createResponse = post(URI + "account");

        assertEquals(createResponse.getStatusCode(), 200);
        String acc = createResponse.getBody().asString();

        Response putMoneyResponse = put(URI + "account/" + acc + "?amount=10003&sa");

        assertEquals(putMoneyResponse.getStatusCode(), 400);

        putMoneyResponse = put(URI + "account/" + acc + "?amount=10003&sa=");
        assertEquals(putMoneyResponse.getStatusCode(), 400);
    }

}
