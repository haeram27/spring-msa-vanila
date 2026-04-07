package com.example.springwebex.model.pagenation

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class RequestBody(
    var assetStateCodeList: List<Int>? = null,
    var pageNumber: Int? = null,
    var pageSize: Int? = null
)
