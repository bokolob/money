package ru.sedano;

import java.math.BigInteger;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import ru.sedano.db.implementations.GetMoneyImpl;
import ru.sedano.db.implementations.PutMoneyImpl;
import ru.sedano.db.implementations.TransferMoneyImpl;
import ru.sedano.db.interfaces.Account;
import ru.sedano.db.interfaces.Engine;
import ru.sedano.db.interfaces.TransferMoney;
import ru.sedano.http.RestApplication;
import ru.sedano.http.exceptions.BadRequestException;
import ru.sedano.http.exceptions.NotFoundException;
import ru.sedano.http.exceptions.ServerErrorException;
import ru.sedano.http.implementations.HTTPResponseImpl;
import ru.sedano.http.interfaces.HTTPResponse;

public class Application implements RestApplication {
    private Engine db;

    public Application(Engine db) {
        this.db = db;
    }

    @Override
    public HTTPResponse route(String method, URI uri, Map<String, List<String>> params) {
        String path = uri.getPath();

        if ("/account".equals(path) && "POST".equals(method)) {
            return addAccount(uri, params);
        } else if (path.matches("^/account/[a-z0-9-]+$") && "GET".equals(method)) {
            return getMoney(uri, params);
        } else if (path.matches("^/account/[a-z0-9-]+$") && "PUT".equals(method)) {
            return putMoney(uri, params);
        } else if (path.matches("^/transfer/[a-z0-9-]+/[a-z0-9-]+$") && "PATCH".equals(method)) {
            return transferMoney(uri, params);
        } else {
            throw new NotFoundException();
        }
    }

    @Override
    public void stop() {
        db.kill();
    }

    private HTTPResponse transferMoney(URI uri, Map<String, List<String>> params) {
        String[] parts = uri.getPath().split("[/]");
        BigInteger amount = new BigInteger(params.get("amount").get(0));

        Account from = lookupAccount(parts[2]);
        Account to = lookupAccount(parts[3]);

        TransferMoney.TransferResult result = null;

        try {
            result = db.transferMoney(new TransferMoneyImpl(from, to, amount)).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        if (result == null) {
            throw new ServerErrorException();
        }

        if (!result.isOk()) {
            return new HTTPResponseImpl(400, result.getError());
        }

        return new HTTPResponseImpl(200, "");
    }

    private HTTPResponse putMoney(URI uri, Map<String, List<String>> params) {
        String id = uri.getPath().substring("/account/".length());

        Account account = lookupAccount(id);

        if (!params.containsKey("amount")) {
            throw new BadRequestException();
        }

        BigInteger amount = new BigInteger(params.get("amount").get(0));

        try {
            db.putMoney(new PutMoneyImpl(account, amount)).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new ServerErrorException();
        }

        return new HTTPResponseImpl(200, "");
    }

    private Account lookupAccount(String name) {
        Account account;

        try {
            account = db.lookupAccount(name).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ServerErrorException();
        }

        if (account == null) {
            throw new NotFoundException();
        }

        return account;
    }

    private HTTPResponse addAccount(URI uri, Map<String, List<String>> params) {
        String account;

        try {
            account = db.createAccount().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ServerErrorException();
        }

        return new HTTPResponseImpl(200, account);
    }


    private HTTPResponse getMoney(URI uri, Map<String, List<String>> params) {
        String id = uri.getPath().substring("/account/".length());
        Account account = lookupAccount(id);

        BigInteger amount;

        try {
            amount = db.getMoneyAmount(new GetMoneyImpl(account)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ServerErrorException();
        }

        return new HTTPResponseImpl(200, amount.toString());
    }


}
