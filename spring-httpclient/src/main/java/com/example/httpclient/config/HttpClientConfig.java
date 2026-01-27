package com.example.httpclient.config;

import java.security.GeneralSecurityException;
import java.time.Duration;

import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.ssl.SSLContexts;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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

            // use connection pool
            var cm = PoolingHttpClientConnectionManagerBuilder.create()
                .setTlsSocketStrategy(tls)
                .build();

            // use BasicHttpClientConnectionManager for one-time connection
            /*
                var cm = BasicHttpClientConnectionManager.create()
                    .setTlsSocketStrategy(tls)   
                    .build();
            */

            var httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .evictExpiredConnections()
                .build();

            // set timeout setting per request in RestTemplate (Spring driven)
            var rf = new HttpComponentsClientHttpRequestFactory(httpClient);
            rf.setConnectionRequestTimeout(Duration.ofSeconds(1)); // time to resolve idle http connection from conn pool
            rf.setConnectTimeout(Duration.ofSeconds(2)); // time to establish tcp connection
            rf.setReadTimeout(Duration.ofSeconds(5)); // time to wait response against http request from http server

            return new RestTemplate(rf);
        } catch (GeneralSecurityException e) {
            throw new org.springframework.beans.factory.BeanCreationException(
                "trustAllRestTemplate", "Failed to build SSL trust-all RestTemplate", e);
        }
    }

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

            // use connection pool
            var cm = PoolingHttpClientConnectionManagerBuilder.create()
                .setTlsSocketStrategy(tls)
                .build();

            // use BasicHttpClientConnectionManager for one-time connection
            /*
                var cm = BasicHttpClientConnectionManager.create()
                    .setTlsSocketStrategy(tls)   
                    .build();
            */

            var httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .evictExpiredConnections()
                .build();

            // set timeout setting per request in RestTemplate (Spring driven)
            var rf = new HttpComponentsClientHttpRequestFactory(httpClient);
            rf.setConnectionRequestTimeout(Duration.ofSeconds(1)); // time to resolve idle http connection from conn pool
            rf.setConnectTimeout(Duration.ofSeconds(2)); // time to establish tcp connection
            rf.setReadTimeout(Duration.ofSeconds(5)); // time to wait response against http request from http server

            return RestClient.builder()
                .requestFactory(rf)
                .build();
        } catch (GeneralSecurityException e) {
            throw new org.springframework.beans.factory.BeanCreationException(
                "trustAllRestClient", "Failed to build SSL trust-all RestClient", e);
        }
    }
}
