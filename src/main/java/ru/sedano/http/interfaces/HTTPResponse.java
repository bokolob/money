package ru.sedano.http.interfaces;

public interface HTTPResponse {
    int getStatus();

    String getContent();
}
