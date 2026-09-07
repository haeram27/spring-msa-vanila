package com.example.springwebex.di;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component()
@Primary
public class BarFormatter implements Formatter {

    @Override
    public String toString() {
        return this.getClass().toString();
    }
}
