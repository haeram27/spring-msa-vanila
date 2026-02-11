package com.example.httpclient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.EvaluatedTimeTests;

@SpringBootTest
public class JdkHttpRequestTests extends EvaluatedTimeTests{

    @Autowired
    @Qualifier("jdkHttpClient")
    private HttpClient httpClient;

    // https://jsonplaceholder.typicode.com/guide/
    private final String urlGetTodoAll = "https://jsonplaceholder.typicode.com/todos";
    private final String urlGetTodoOne = "https://jsonplaceholder.typicode.com/todos/1";

    @Test
    public void get() throws Exception {
        // HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(urlGetTodoOne))
            .GET()
            .build();

        HttpResponse response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println(response.statusCode());
        System.out.println(response.body());
    }

    @Test
    public void build() throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(urlGetTodoOne))
            .timeout(Duration.ofSeconds(5))  // response timeout
            .GET()
            .build();

        HttpResponse response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println(response.statusCode());
        System.out.println(response.body());
    }
}
