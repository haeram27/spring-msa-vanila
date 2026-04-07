package com.example.springwebex.model.pagenation

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Server(
    val hostName: String,
    val ip: String,
    val managerId: String,
    val managerName: String,
    val developerId: String,
    val developerName: String,
    val serviceName: String
)
