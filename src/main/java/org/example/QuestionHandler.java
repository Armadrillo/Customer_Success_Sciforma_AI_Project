package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class QuestionHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
//      Check request method
        if("Post".equalsIgnoreCase(exchange.getRequestMethod())){
//            Read the request body
            InputStream input = exchange.getRequestBody();
            String body = new String(input.readAllBytes());
            System.out.println("Raw data " + body);

//            Parse JSON (using Jackson)
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> jsonMap = mapper.readValue(body, Map.class);
            String question = jsonMap.get("question");
            System.out.println("User asked " + question);

//          I should send this question variable to bot (and learn about %s)
            String apiBody = """
        {
          "model": "sciforma-success-test-001",
          "max_tokens": 1256,
          "messages": [
            {"role": "user", "content": "%s"}
          ]
        }
        """.formatted(question);

//            Send Post Request to Falcon AI
            String apiKey = new APIKey().getKey();
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://falconai.planview-prod.io/api/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization","Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(apiBody))
                    .build();
//          API response
            HttpResponse<String> apiResponse;
            try{
                apiResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            System.out.println("API Response : " + apiResponse);
            System.out.println("API Key: " + apiKey);
            System.out.println("Request Body: " + apiBody);
            System.out.println("Request Headers: Authorization=Bearer " + apiKey);

//            Extract answer from bot
            Map<String, Object> responseMap = mapper.readValue(apiResponse.body(), Map.class);
            Map<String, Object> firstChoice = ((List<Map<String, Object>>)responseMap.get("choices")).get(0);
            Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
            String answer = (String) message.get("content");
//            String jsonResponse = mapper.writeValueAsString(Map.of("answer", answer));

//            Send back a response
//            byte[] responseByte = jsonResponse.getBytes();
            byte[] responseByte = answer.getBytes();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseByte.length);

            try(OutputStream os = exchange.getResponseBody()){
                os.write(responseByte);
            }
        }
        else exchange.sendResponseHeaders(405, -1);
    }
}
