package com.example.springwebex.model.bean

import java.time.Duration

data class MyDataSource(
    val url: String,
    val username: String,
    val password: String,
    val maxConnection: Int,
    val timeout: Duration? = null,
    val options: List<String>? = null
)
