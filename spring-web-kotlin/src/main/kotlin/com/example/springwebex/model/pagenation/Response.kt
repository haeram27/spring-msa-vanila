package com.example.springwebex.model.pagenation

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Response(
    var header: ResponseHeader? = null,
    var body: ResponseBody? = null
)
