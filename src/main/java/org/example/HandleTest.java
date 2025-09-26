package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class HandleTest implements HttpHandler {
    public HandleTest(){};
    private String info = "Hello! This is a test for Context which will be provided on localhost";

    public String getInfo() {
        return info;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        byte[] responseBytes = getInfo().getBytes();
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, responseBytes.length);

        try(OutputStream os = exchange.getResponseBody()){
            os.write(responseBytes);
        }
    }
}
