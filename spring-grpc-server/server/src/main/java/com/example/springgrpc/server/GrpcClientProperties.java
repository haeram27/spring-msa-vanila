package com.example.springgrpc.server;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("sample.grpc.client")
public class GrpcClientProperties {
    private String host = "localhost";
    private int port = 9090;
    private long timeoutSeconds = 5;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public long getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(long timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
}
