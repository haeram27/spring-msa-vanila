package com.example.httpclient.config

import tools.jackson.databind.json.JsonMapper
import java.security.GeneralSecurityException
import java.time.Duration
import org.apache.hc.client5.http.config.ConnectionConfig
import org.apache.hc.client5.http.config.RequestConfig
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier
import org.apache.hc.client5.http.ssl.TrustAllStrategy
import org.apache.hc.core5.ssl.SSLContexts
import org.apache.hc.core5.util.TimeValue
import org.apache.hc.core5.util.Timeout
import org.springframework.beans.factory.BeanCreationException
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestTemplate

@Configuration
class ApacheHttpClientConfig(
    private val mapper: JsonMapper
) {
    @Bean
    fun apacheClientTrustAllRestTemplate(): RestTemplate {
        try {
            val sslContext = SSLContexts.custom()
                .loadTrustMaterial(null, TrustAllStrategy.INSTANCE)
                .build()

            val tls = ClientTlsStrategyBuilder.create()
                .setSslContext(sslContext)
                .setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .buildClassic()

            val connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.of(Duration.ofSeconds(2)))
                .setSocketTimeout(Timeout.of(Duration.ofSeconds(5)))
                .build()

            val connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setTlsSocketStrategy(tls)
                .setDefaultConnectionConfig(connectionConfig)
                .build()

            val requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofSeconds(1))
                .setResponseTimeout(Timeout.ofSeconds(5))
                .build()

            val httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .evictExpiredConnections()
                .evictIdleConnections(TimeValue.ofSeconds(10))
                .build()

            val requestFactory = HttpComponentsClientHttpRequestFactory(httpClient)
            requestFactory.setConnectionRequestTimeout(Duration.ofSeconds(1))
            requestFactory.setReadTimeout(Duration.ofSeconds(5))

            val template = RestTemplate(requestFactory)
            val converters = template.messageConverters
                .filterIsInstance<JacksonJsonHttpMessageConverter>()
                .toMutableList()
            converters.add(JacksonJsonHttpMessageConverter(mapper))
            template.messageConverters = converters

            return template
        } catch (e: GeneralSecurityException) {
            throw BeanCreationException(
                "trustAllRestTemplate",
                "Failed to build SSL trust-all RestTemplate",
                e,
            )
        }
    }

    @Bean
    fun apacheClientTrustAllRestClient(): RestClient {
        try {
            val sslContext = SSLContexts.custom()
                .loadTrustMaterial(null, TrustAllStrategy.INSTANCE)
                .build()

            val tls = ClientTlsStrategyBuilder.create()
                .setSslContext(sslContext)
                .setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .buildClassic()

            val connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.of(Duration.ofSeconds(2)))
                .setSocketTimeout(Timeout.of(Duration.ofSeconds(5)))
                .build()

            val connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setTlsSocketStrategy(tls)
                .setDefaultConnectionConfig(connectionConfig)
                .build()

            val requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofSeconds(1))
                .setResponseTimeout(Timeout.ofSeconds(5))
                .build()

            val httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .evictExpiredConnections()
                .evictIdleConnections(TimeValue.ofSeconds(10))
                .build()

            val requestFactory = HttpComponentsClientHttpRequestFactory(httpClient)
            requestFactory.setConnectionRequestTimeout(Duration.ofSeconds(1))
            requestFactory.setReadTimeout(Duration.ofSeconds(5))

            return RestClient.builder()
                .requestFactory(requestFactory)
                .configureMessageConverters { converters ->
                    converters.withJsonConverter(JacksonJsonHttpMessageConverter(mapper))
                }
                .build()
        } catch (e: GeneralSecurityException) {
            throw BeanCreationException(
                "trustAllRestClient",
                "Failed to build SSL trust-all RestClient",
                e,
            )
        }
    }
}
