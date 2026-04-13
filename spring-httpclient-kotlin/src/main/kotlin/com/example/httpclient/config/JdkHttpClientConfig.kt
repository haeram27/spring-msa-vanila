package com.example.httpclient.config

import tools.jackson.databind.json.JsonMapper
import java.net.http.HttpClient
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.time.Duration
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestTemplate

@Configuration
class JdkHttpClientConfig(
    private val mapper: JsonMapper,
) {
    @Bean
    @Primary
    @Throws(KeyManagementException::class, NoSuchAlgorithmException::class, NullPointerException::class)
    fun trustAllSSLContext(): SSLContext {
        val trustAllCerts = arrayOf<TrustManager>(
            object : X509TrustManager {
                override fun getAcceptedIssuers(): Array<X509Certificate>? = null
                override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {}
                override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {}
            }
        )

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, SecureRandom())
        return sslContext
    }

    @Bean
    @Primary
    fun jdkHttpClient(@Qualifier("trustAllSSLContext") sslContext: SSLContext): HttpClient {
        return HttpClient.newBuilder()
            .sslContext(sslContext)
            .connectTimeout(Duration.ofSeconds(2))
            .build()
    }

    @Bean
    @Primary
    fun jdkClientTrustAllRestTemplate(@Qualifier("jdkHttpClient") httpClient: HttpClient): RestTemplate {
        val requestFactory = JdkClientHttpRequestFactory(httpClient)
        requestFactory.setReadTimeout(Duration.ofSeconds(5))

        val template = RestTemplate(requestFactory)
        val converters = template.messageConverters
            .filterIsInstance<JacksonJsonHttpMessageConverter>()
            .toMutableList()
        converters.add(JacksonJsonHttpMessageConverter(mapper))
        template.messageConverters = converters

        return template
    }

    @Bean
    @Primary
    fun jdkClientTrustAllRestClient(@Qualifier("jdkHttpClient") httpClient: HttpClient): RestClient {
        val requestFactory = JdkClientHttpRequestFactory(httpClient)
        requestFactory.setReadTimeout(Duration.ofSeconds(5))

        return RestClient.builder()
            .requestFactory(requestFactory)
            .configureMessageConverters { converters ->
                converters.withJsonConverter(JacksonJsonHttpMessageConverter(mapper))
            }
            .build()
    }
}
