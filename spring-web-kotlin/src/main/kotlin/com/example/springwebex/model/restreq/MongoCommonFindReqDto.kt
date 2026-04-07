package com.example.springwebex.model.restreq

import java.io.Serializable
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class MongoCommonFindReqDto(
    var collectionName: String? = null,
    var timeZone: String? = null,
    var startDate: String? = null,
    var endDate: String? = null,
    var projectKeys: List<String>? = null,
    var pageSize: Int? = null,
    var pageNumber: Int? = null
) : Serializable {
    companion object {
        private const val serialVersionUID = -717033159421164288L
    }
}
