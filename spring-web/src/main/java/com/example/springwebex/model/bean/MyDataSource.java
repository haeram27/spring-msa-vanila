package com.example.springwebex.model.bean;

import java.time.Duration;
import java.util.List;

import lombok.Getter;

@Getter
public class MyDataSource {

    private final String url;
    private final String username;
    private final String password;
    private final int maxConnection;
    private final Duration timeout;
    private final List<String> options;

    public MyDataSource(String url, String username, String password, int maxConnection,
            Duration timeout, List<String> options) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.maxConnection = maxConnection;
        this.timeout = timeout;
        this.options = options;
    }
}
