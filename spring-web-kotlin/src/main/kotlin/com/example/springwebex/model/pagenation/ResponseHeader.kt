package com.example.springwebex.model.pagenation

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class ResponseHeader(
    var isSuccessful: Boolean? = null,
    var resultCode: Int? = null,
    var resultMessage: String? = null
)
