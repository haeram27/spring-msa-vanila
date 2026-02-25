package com.example.httpclient.config;

import java.net.http.HttpClient;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.json.JsonMapper;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class JdkHttpClientConfig {

    private final JsonMapper mapper;

    @Bean
    @Primary
    public SSLContext trustAllSSLContext() throws KeyManagementException, NoSuchAlgorithmException, NullPointerException {

        // Create TrustManager trusts ALL server certs
        var trustAllCerts = new javax.net.ssl.TrustManager[]{
            new javax.net.ssl.X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
            }
        };

        // Create SSLContext
        var sslContext = javax.net.ssl.SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

        return sslContext;
    }

    @Bean
    @Primary
    public HttpClient jdkHttpClient(@Qualifier("trustAllSSLContext") SSLContext sslContext) {

        return HttpClient.newBuilder()
            .sslContext(sslContext)
            .connectTimeout(Duration.ofSeconds(2)) // connection timeout
            .build();
    }

    /**
     * RestTemplate - Sync, Blocking HttpClient
     */
    @Bean
    @Primary
    public RestTemplate jdkClientTrustAllRestTemplate(@Qualifier("jdkHttpClient") HttpClient httpClient) {

        /*
         * set timeout setting per request in RestTemplate (Spring driven),
         * if timeouts set here then timeout config is not require at ConnectionConfig and RequestConfig
         */
        var rf = new JdkClientHttpRequestFactory(httpClient);
        rf.setReadTimeout(Duration.ofSeconds(5)); // time to wait response against http request from http server

        var template = new RestTemplate(rf);
        // use converter to map json response as JsonNode of Jackson with user defined JsonMapper(mapper)
        var converters = template.getMessageConverters().stream().filter(c -> c instanceof MappingJackson2HttpMessageConverter).collect(Collectors.toList());
        converters.add(new MappingJackson2HttpMessageConverter(mapper));
        template.setMessageConverters(converters);

        return template;
    }

    /**
     * RestClient - Sync, Blocking HttpClient
     */
    @Bean
    @Primary
    public RestClient jdkClientTrustAllRestClient(@Qualifier("jdkHttpClient") HttpClient httpClient) {

        var rf = new JdkClientHttpRequestFactory(httpClient);
        rf.setReadTimeout(Duration.ofSeconds(5));

        return RestClient.builder()
                .requestFactory(rf)
                // use converter to map json response as JsonNode of Jackson with user defined JsonMapper(mapper)
                .messageConverters(converters -> {
                    converters.removeIf(c -> c instanceof MappingJackson2HttpMessageConverter);
                    converters.add(new MappingJackson2HttpMessageConverter(mapper));
                })
                .build();
    }
}
