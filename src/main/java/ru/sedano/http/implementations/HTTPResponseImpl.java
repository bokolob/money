package ru.sedano.http.implementations;

import ru.sedano.http.interfaces.HTTPResponse;

public class HTTPResponseImpl implements HTTPResponse {

    private int status;
    private String content;

    public HTTPResponseImpl(int status, String content) {
        this.status = status;
        this.content = content;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public String getContent() {
        return content;
    }
}
