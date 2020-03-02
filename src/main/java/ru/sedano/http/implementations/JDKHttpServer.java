package ru.sedano.http.implementations;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import ru.sedano.http.RestApplication;
import ru.sedano.http.exceptions.BadRequestException;
import ru.sedano.http.exceptions.NotFoundException;
import ru.sedano.http.interfaces.HTTPResponse;

public class JDKHttpServer implements ru.sedano.http.interfaces.HttpServer {
    private HttpServer httpServer;
    private RestApplication application;

    public JDKHttpServer(int port, RestApplication application) throws IOException {
        this.application = application;
        httpServer = HttpServer.create(new InetSocketAddress(port), 128);
        httpServer.setExecutor(Executors.newCachedThreadPool());
        HttpContext context = httpServer.createContext("/", this::handle);
        context.getFilters().add(new GetParamsFilter());
    }

    @Override
    public void start() {
        httpServer.start();
    }

    @Override
    public void stop() {
        httpServer.stop(0);
        application.stop();
    }

    private void handle(HttpExchange exchange) {
        try {

            @SuppressWarnings("unchecked")
            HTTPResponse response = application.route(exchange.getRequestMethod(), exchange.getRequestURI(),
                    (Map<String, List<String>>) exchange.getAttribute("params"));

            sendResponse(exchange, response.getStatus(), response.getContent());
        } catch (BadRequestException e) {
            return400(exchange);
        } catch (NotFoundException e) {
            return404(exchange);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            return500(exchange);
            e.printStackTrace();
        }
    }

    private void sendResponse(HttpExchange exchange, int code, String txt) throws IOException {
        exchange.sendResponseHeaders(code, txt.length());
        exchange.getResponseBody().write(txt.getBytes());
        exchange.getResponseBody().flush();
        exchange.getResponseBody().close();
    }

    private void return500(HttpExchange exchange) {
        sendError(exchange, 500, "Internal Server Error");
    }

    private void return404(HttpExchange exchange) {
        sendError(exchange, 404, "Not found");
    }

    private void return400(HttpExchange exchange) {
        sendError(exchange, 400, "Bad request");
    }

    private void sendError(HttpExchange exchange, int code, String msg) {
        try {
            sendResponse(exchange, code, msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class GetParamsFilter extends Filter {

        @Override
        public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
            Map<String, List<String>> params = null;

            try {
                params = parseParams(exchange.getRequestURI().getRawQuery());
            } catch (BadRequestException e) {
                exchange.sendResponseHeaders(400, -1);
                exchange.getResponseBody().close();
            }

            exchange.setAttribute("params", params);
            chain.doFilter(exchange);
        }

        private Map<String, List<String>> parseParams(String rawQuery) throws BadRequestException {
            if (rawQuery == null) {
                return Collections.emptyMap();
            }

            Map<String, List<String>> params = new HashMap<>();

            String[] kvPairs = rawQuery.split("[&]");

            for (String kv : kvPairs) {
                String[] pair = kv.split("[=]");

                if (pair.length != 2) {
                    throw new BadRequestException();
                }

                String key = null;
                String value = null;

                try {
                    key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8.toString());
                    value = URLDecoder.decode(pair[1], StandardCharsets.UTF_8.toString());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                params.computeIfAbsent(key, e -> new ArrayList<>()).add(value);
            }

            return params;
        }

        @Override
        public String description() {
            return "Filter parses query params";
        }
    }

}

