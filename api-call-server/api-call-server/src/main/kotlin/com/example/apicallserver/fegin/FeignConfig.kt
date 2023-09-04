package com.example.apicallserver.fegin

import feign.Request
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import java.util.concurrent.TimeUnit

class FeignConfig {

    @Bean
    fun requestOptions(
        @Value("\${test.api.connect-timeout}") connTimeout: Long,
        @Value("\${test.api.read-timeout}") readTimeout: Long
    ): Request.Options {
        return Request.Options(connTimeout, TimeUnit.MILLISECONDS, readTimeout, TimeUnit.MILLISECONDS, true)
    }
}