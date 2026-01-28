package com.example.httpclient.config;

import java.security.GeneralSecurityException;
import java.time.Duration;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.reactive.HttpComponentsClientHttpConnector;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/*
 * Apache httpclient5 Docs - https://hc.apache.org/httpcomponents-client-5.6.x/index.html
 * Examples - https://github.com/apache/httpcomponents-client/tree/master/httpclient5/src/test/java/org/apache/hc/client5/http/examples
 */

@Configuration
public class HttpClientConfig {

    @Bean
    @Primary
    public JsonMapper restClientObjectMapper(){

        return JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
            .enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();
    }

    /*
     * RestTemplate - Sync, Blocking HttpClient
     */

    @Bean
    @Primary
    public RestTemplate trustAllRestTemplate() {
        try {
            var sslContext = SSLContexts.custom()
                .loadTrustMaterial(null, TrustAllStrategy.INSTANCE)
                .build();

            var tls = ClientTlsStrategyBuilder.create()
                .setSslContext(sslContext)
                .setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .buildClassic();

            // TCP(Socket) level Connection Config
            var cc = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.of(Duration.ofSeconds(2)))  // timeout while waiting for TCP connection establishment
                .setSocketTimeout(Timeout.of(Duration.ofSeconds(5)))   // inactivity timeout of inter-packet
                .build();

            // use connection pool
            var cm = PoolingHttpClientConnectionManagerBuilder.create()
                .setTlsSocketStrategy(tls)
                .setDefaultConnectionConfig(cc)
                .build();

            // use BasicHttpClientConnectionManager for one-time connection
            /*
                var cm = BasicHttpClientConnectionManager.create()
                    .setTlsSocketStrategy(tls)   
                    .build();
            */

            // Application(HTTP) level Connection Config
            var rc = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofSeconds(1)) // timeout while waiting for lent a connection from connection pool
                .setResponseTimeout(Timeout.ofSeconds(5))          // timeout while waiting for server response
                .build();

            var httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultRequestConfig(rc)
                .evictExpiredConnections() // Find, close and remove connections that are stored in the connection pool that are past the server-set validity (TTL)
                .evictIdleConnections(TimeValue.ofSeconds(10)) // evict(remove) connection has no sending request for more than 10 sec
                .build();

            /* 
             * set timeout setting per request in RestTemplate (Spring driven),
             * if timeouts set here then timeout config is not require at ConnectionConfig and RequestConfig
             */
            var rf = new HttpComponentsClientHttpRequestFactory(httpClient);
            rf.setConnectionRequestTimeout(Duration.ofSeconds(1)); // time to resolve idle http connection from conn pool
            rf.setConnectTimeout(Duration.ofSeconds(2));           // time to establish tcp connection
            rf.setReadTimeout(Duration.ofSeconds(5));              // time to wait response against http request from http server

            var template = new RestTemplate(rf);

            /*
             * RestCient can be made by RestTmeplate
             */
            // RestClient client = RestClient.create(template);

            return template;
        } catch (GeneralSecurityException e) {
            throw new org.springframework.beans.factory.BeanCreationException(
                "trustAllRestTemplate", "Failed to build SSL trust-all RestTemplate", e);
        }
    }

    /*
     * RestClient - Sync, Blocking HttpClient
     */

    @Bean
    @Primary
    public RestClient trustAllRestClient() {
        try {
            var sslContext = SSLContexts.custom()
                .loadTrustMaterial(null, TrustAllStrategy.INSTANCE)
                .build();

            var tls = ClientTlsStrategyBuilder.create()
                .setSslContext(sslContext)
                .setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .buildClassic();

            // TCP(Socket) level Connection Config
            var cc = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.of(Duration.ofSeconds(2)))  // timeout while waiting for TCP connection establishment
                .setSocketTimeout(Timeout.of(Duration.ofSeconds(5)))   // inactivity timeout of inter-packet
                .build();

            // use connection pool
            var cm = PoolingHttpClientConnectionManagerBuilder.create()
                .setTlsSocketStrategy(tls)
                .setDefaultConnectionConfig(cc)
                .build();

            // use BasicHttpClientConnectionManager for one-time connection
            /*
                var cm = BasicHttpClientConnectionManager.create()
                    .setTlsSocketStrategy(tls)   
                    .build();
            */

            // Application(HTTP) level Connection Config
            var rc = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofSeconds(1)) // timeout while waiting for lent a connection from connection pool
                .setResponseTimeout(Timeout.ofSeconds(5))          // timeout while waiting for server response
                .build();

            var httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultRequestConfig(rc)
                .evictExpiredConnections() // Find, close and remove connections that are stored in the connection pool that are past the server-set validity (TTL)
                .evictIdleConnections(TimeValue.ofSeconds(10)) // evict(remove) connection has no sending request for more than 10 sec
                .build();

            /* 
             * support timeout setting per request for SYNC way HttpClient (Spring driven),
             * if timeouts set here then timeout config is not require at ConnectionConfig and RequestConfig
             */
            var rf = new HttpComponentsClientHttpRequestFactory(httpClient);
            rf.setConnectionRequestTimeout(Duration.ofSeconds(1)); // time to resolve idle http connection from conn pool
            rf.setConnectTimeout(Duration.ofSeconds(2));           // time to establish tcp connection
            rf.setReadTimeout(Duration.ofSeconds(5));              // time to wait response against http request from http server

            return RestClient.builder()
                .requestFactory(rf)
                .build();
        } catch (GeneralSecurityException e) {
            throw new org.springframework.beans.factory.BeanCreationException(
                "trustAllRestClient", "Failed to build SSL trust-all RestClient", e);
        }
    }

    /*
     * WebClient - Async, Non-Blocking HttpClient
     */

    @Bean
    @Primary
    public WebClient trustAllWebClient() {
        try {
            var sslContext = SSLContexts.custom()
            .loadTrustMaterial(null, TrustAllStrategy.INSTANCE)
            .build();

            // TlsStrategy: async client method
            var tls = ClientTlsStrategyBuilder.create()
                .setSslContext(sslContext)
                .setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .buildAsync();

            // TCP(Socket) level Connection Config
            var cc = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.of(Duration.ofSeconds(2)))  // timeout while waiting for TCP connection establishment
                .setSocketTimeout(Timeout.of(Duration.ofSeconds(5)))   // inactivity timeout of inter-packet
                .build();

            // connection pool: async client method
            var cm = PoolingAsyncClientConnectionManagerBuilder.create()
                .setTlsStrategy(tls)
                .setDefaultConnectionConfig(cc)
                // modify to enhance performance, mainly Connection Per Route
                .setMaxConnTotal(25)    // default: 25
                .setMaxConnPerRoute(5)  // default: 5
                .build();

            // Application(HTTP) level Connection Config
            var rc = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofSeconds(1)) // timeout while waiting for lent a connection from connection pool
                .setResponseTimeout(Timeout.ofSeconds(5))          // timeout while waiting for server response
                .build();

            var asyncClient = HttpAsyncClients.custom()
                .setConnectionManager(cm)
                .setDefaultRequestConfig(rc)
                .evictExpiredConnections() // Find, close and remove connections that are stored in the connection pool that are past the server-set validity (TTL)
                .evictIdleConnections(TimeValue.ofSeconds(10)) // evict(remove) connection has no sending request for more than 10 sec
                .build();

            // assign WebClient to HttpConnector (make Spring control connection lifecycle)
            var connector = new HttpComponentsClientHttpConnector(asyncClient);

        return WebClient.builder()
            .clientConnector(connector)
            .build();
        } catch (GeneralSecurityException e) {
            throw new org.springframework.beans.factory.BeanCreationException(
                "trustAllWebClient", "Failed to build SSL trust-all WebClient", e);
        }
    }
}
