package com.example.springwebex.model.pagenation

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class ResponseBody(
    var pageNum: Int? = null,
    var pageSize: Int? = null,
    var totalCount: Int? = null,
    var data: List<Any>? = null
)
