package com.example.springwebex.di;

import org.springframework.stereotype.Component;

@Component
public class ZooFormatter {
    public String toString() {
        return this.getClass().toString();
    }
}
