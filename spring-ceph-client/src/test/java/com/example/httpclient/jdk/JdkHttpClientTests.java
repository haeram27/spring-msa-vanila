package com.example.httpclient.jdk;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.PushPromiseHandler;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.EvaluatedTimeTests;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class JdkHttpClientTests extends EvaluatedTimeTests{

    @Autowired
    @Qualifier("jdkHttpClient")
    private HttpClient httpClient;

    // https://jsonplaceholder.typicode.com/guide/
    private final String urlGetLocal = "http://localhost:8181/greeting";
    private final String urlGetTodoAll = "https://jsonplaceholder.typicode.com/todos";
    private final String urlGetTodoOne = "https://jsonplaceholder.typicode.com/todos/1";

    @Test
    public void get() throws Exception {
        // HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(urlGetLocal))
            .GET()
            .build();

        /*
            httpClient.send() Exceptions:
            IOException         네트워크 오류, 연결 실패, 응답 타임아웃 등
            InterruptedException 대기 중 스레드 인터럽트
        */
        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (java.io.IOException e) {
            System.err.println("네트워크 오류: " + e.getMessage());
            throw e;
        } catch (InterruptedException e) {
            System.err.println("요청 인터럽트: " + e.getMessage());
            Thread.currentThread().interrupt();
            throw e;
        }

        System.out.println(response.statusCode());
        System.out.println(response.body());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            System.err.println("HTTP Success: " + response.statusCode());
            System.err.println("Response Body: " + response.body());
        }

        if (response.statusCode() >= 400 && response.statusCode() < 500) {
            System.err.println("HTTP Client Error: " + response.statusCode());
            System.err.println("Error Body: " + response.body());
            // 필요 시 예외를 직접 던짐
            throw new RuntimeException("HTTP " + response.statusCode());
        }

        if (response.statusCode() >= 500 && response.statusCode() < 600) {
            System.err.println("HTTP Server Error: " + response.statusCode());
            System.err.println("Error Body: " + response.body());
            // 필요 시 예외를 직접 던짐
            throw new RuntimeException("HTTP " + response.statusCode());
        }
    }

    /*
        HttpClient의 비동기 요청 테스트:
        - sendAsync() 메서드를 사용하여 비동기 HTTP 요청을 보냄
        - CompletableFuture를 반환하여 응답을 비동기적으로 처리
        - 응답이 10초 내에 도착하지 않으면 TimeoutException 발생

        sendAsync() Exceptions:
        - IllegalArgumentException: 요청이 null이거나 잘못된 경우

        future.get() Exceptions:
        - ExecutionException: 비동기 작업 내부에서 예외 발생 (네트워크 오류, HTTP 오류 등) (future.get() 호출 시)
        - InterruptedException: 대기 중 스레드 인터럽트 (future.get() 호출 시)
        - TimeoutException: 10초 내 응답 없음 (future.get() 호출 시)

        HttpClient의 비동기 요청 주의 사항:
        - sendAsync()는 내부적으로 가상 thread가 아닌 실제 스레드를 사용하므로, 많은 요청을 동시에 보내면 스레드 풀 고갈 가능성 있음
        - application에서 비동기 요청 구현 필요시 직접 virtual thread를 사용하며 동기화 api로 구현하는 것을 권장
        (예: Thread.startVirtualThread(() -> { ... })) - Java 19 이상에서 지원
     */
    @Test
    public void getAsync() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(urlGetTodoOne))
            .timeout(Duration.ofSeconds(10))
            .GET()
            .build();

        CompletableFuture<HttpResponse<String>> future =
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        /*
            future.get() Exceptions:
            ExecutionException	비동기 작업 내부에서 예외 발생 (네트워크 오류, HTTP 오류 등)
            InterruptedException	대기 중 스레드 인터럽트
            TimeoutException	10초 내 응답 없음
        */
        HttpResponse<String> response;
        try {
            response = future.get(10, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            System.err.println("응답 타임아웃 (10초 초과)");
            throw e;
        } catch (ExecutionException e) {
            System.err.println("비동기 오류: " + e.getCause().getMessage());
            throw e;
        }

        System.out.println("메인 응답 상태: " + response.statusCode());
        System.out.println("메인 응답 본문: " + response.body());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            System.err.println("HTTP Success: " + response.statusCode());
            System.err.println("Response Body: " + response.body());
        }

        if (response.statusCode() >= 400 && response.statusCode() < 500) {
            System.err.println("HTTP Client Error: " + response.statusCode());
            System.err.println("Error Body: " + response.body());
            // 필요 시 예외를 직접 던짐
            throw new RuntimeException("HTTP " + response.statusCode());
        }

        if (response.statusCode() >= 500 && response.statusCode() < 600) {
            System.err.println("HTTP Server Error: " + response.statusCode());
            System.err.println("Error Body: " + response.body());
            // 필요 시 예외를 직접 던짐
            throw new RuntimeException("HTTP " + response.statusCode());
        }
    }

    /*
        HTTP Push Promise 테스트:
        - Server Push:
          - HTTP/2의 기능으로, 서버가 클라이언트의 요청에 대한 응답과 함께 추가 리소스를 푸시할 수 있음
          - 잘 사용되지 않아 실제 서버에서 지원하는 경우가 드물며, chrome, firefox 등 주요 브라우저에서도 사실상 deprecated 추세
        - 서버가 클라이언트에게 추가 리소스를 미리 푸시할 수 있는 기능을 테스트
        - PushPromiseHandler를 사용하여 푸시된 요청과 응답을 처리
     */
    @Test
    public void getAsyncWithPushPromise() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(urlGetTodoAll))
            .timeout(Duration.ofSeconds(10))
            .GET()
            .build();

        Map<HttpRequest, CompletableFuture<HttpResponse<String>>> pushedResponses =
            new ConcurrentHashMap<>();

        PushPromiseHandler<String> pushPromiseHandler =
            (initiatingRequest, pushRequest, acceptor) -> {
                System.out.println("Push 요청 수신: " + pushRequest.uri());
                pushedResponses.put(pushRequest, acceptor.apply(HttpResponse.BodyHandlers.ofString()));
            };

        CompletableFuture<HttpResponse<String>> future =
            httpClient.sendAsync(request,
                                HttpResponse.BodyHandlers.ofString(),
                                pushPromiseHandler);

        /*
            future.get() Exceptions:
            ExecutionException	비동기 작업 내부에서 예외 발생 (네트워크 오류, HTTP 오류 등)
            InterruptedException	대기 중 스레드 인터럽트
            TimeoutException	10초 내 응답 없음
        */
        HttpResponse<String> response;
        try {
            response = future.get(10, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            System.err.println("응답 타임아웃 (10초 초과)");
            throw e;
        } catch (ExecutionException e) {
            // 실제 원인을 unwrap해서 출력
            System.err.println("비동기 오류: " + e.getCause().getMessage());
            throw e;
        }

        System.out.println("메인 응답 상태: " + response.statusCode());
        System.out.println("메인 응답 본문: " + response.body());

        for (Map.Entry<HttpRequest, CompletableFuture<HttpResponse<String>>> entry
                : pushedResponses.entrySet()) {
            try {
                HttpResponse<String> pushed = entry.getValue().get(5, TimeUnit.SECONDS);
                System.out.println("Push URI: " + entry.getKey().uri());
                System.out.println("Push 상태: " + pushed.statusCode());
            } catch (Exception e) {
                System.err.println("Push 응답 오류: " + entry.getKey().uri() + " - " + e.getMessage());
            }
        }
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

