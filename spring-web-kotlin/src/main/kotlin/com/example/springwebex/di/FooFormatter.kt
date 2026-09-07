package com.example.springwebex.di

import org.springframework.stereotype.Component

@Component("fooFormatter")
class FooFormatter : Formatter {
    override fun format(input: String): String {
        return "Foo: $input"
    }
}
