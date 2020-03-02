package ru.sedano.http;

import java.net.URI;
import java.util.List;
import java.util.Map;

import ru.sedano.http.interfaces.HTTPResponse;

public interface RestApplication {
    HTTPResponse route(String method, URI uri, Map<String, List<String>> params);

    void stop();
}
