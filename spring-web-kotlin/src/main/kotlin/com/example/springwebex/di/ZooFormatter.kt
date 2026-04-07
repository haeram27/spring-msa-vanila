package com.example.springwebex.di

import org.springframework.stereotype.Component

@Component("zooFormatter")
class ZooFormatter : Formatter {
    override fun format(input: String): String {
        return "Zoo: $input"
    }
}
