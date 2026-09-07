package com.example.springwebex.di

import org.springframework.stereotype.Component

@Component("barFormatter")
class BarFormatter : Formatter {
    override fun format(input: String): String {
        return "Bar: $input"
    }
}
