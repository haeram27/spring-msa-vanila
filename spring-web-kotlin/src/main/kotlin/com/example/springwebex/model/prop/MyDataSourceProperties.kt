package com.example.springwebex.model.prop

import java.time.Duration
import org.hibernate.validator.constraints.time.DurationMax
import org.hibernate.validator.constraints.time.DurationMin
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty

@Validated
@ConfigurationProperties("my.datasource")
data class MyDataSourceProperties(
    @NotEmpty
    var url: String = "",

    @NotEmpty
    var username: String = "",

    @NotEmpty
    var password: String = "",

    var etc: Etc? = null
) {
    data class Etc(
        @Min(1)
        @Max(999)
        var maxConnection: Int = 1,

        @DurationMin(seconds = 1)
        @DurationMax(seconds = 60)
        var timeout: Duration? = null,

        var options: List<String>? = null
    )
}
