package com.example.empty.di;

import org.springframework.stereotype.Component;

@Component
public class FooFormatter implements Formatter {

    @Override
    public String toString() {
        return this.getClass().toString();
    }
}
